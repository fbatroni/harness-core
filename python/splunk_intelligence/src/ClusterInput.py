import argparse
import json
import logging
import multiprocessing
import sys
import time

import numpy as np
import scipy.sparse as sps
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.feature_extraction.text import TfidfTransformer

from core.distance.JaccardDistance import pairwise_jaccard_similarity
from core.feature.TFIDFVectorizer import TFIDFVectorizer
from core.feature.Tokenizer import Tokenizer
from sources.SplunkFileSource import SplunkFileSource
from sources.SplunkHarnessLoader import SplunkHarnessLoader

format = "%(asctime)-15s %(levelname)s %(message)s"
logging.basicConfig(level=logging.INFO, format=format)
logger = logging.getLogger(__name__)


def create_feature_matrix_worker(job_id, chunk, queue):
    """
    This method takes a list of texts and creates the term frequency
    matrix using the HashingVectorizer. This method will be run in parallel
    for small chunks of texts and the outputs will then be combined. This speeds up the vectorization
    process compared to the TFIDFVectorizer that cannot be run in parallel.

    :param job_id: unique id for this job
    :param chunk: the subset of texts to process
    :param queue: the output is sent to this queue

    """
    vectorizer = HashingVectorizer(
        stop_words='english',
        tokenizer=Tokenizer.default_tokenizer, ngram_range=(1, 1),
        non_negative=True,
        norm=None)
    feature_matrix = vectorizer.transform(np.array(chunk))
    queue.put(dict(job_id=job_id, data=feature_matrix.data, indices=feature_matrix.indices,
                   indptr=feature_matrix.indptr,
                   shape=feature_matrix.shape))
    logger.debug('finished processing chunk ' + str(job_id))


class ClusterInput(object):
    """
    Calls the input_url given to fetch a set of log messages (collected by the delegate) and
    clusters them so as to generate a set of unique log messages and its count, which is the
    number of occurrences of each unique log message.

    The output is posted back to the Harness server. This will then be sent to the SplunkIntelOptimized
    program for anomaly detection.

    The Harness server will call this program for each node, for each minute. So the output generated will
    be the set of unique messages generated by the node and the number of times it occurred in that minute.



    Sample input is below:

    [{
    "_id" : "P5sXorzKSKyE9A2DmoSi_Q",
    "stateType" : "ELK",
    "workflowId" : "_K-HeAx-QJ265dRnX1qeaA",
    "stateExecutionId" : "6NF-TIRPQ-WhDolSjuHRTg",
    "query" : ".*exception.*",
    "applicationId" : "m9XTWIcnS2OVk-ys0wiX-Q",
    "clusterLabel" : "0",
    "host" : "ip-172-31-12-78",
    "timeStamp" : 1502249940162,
    "count" : 1,
    "logMessage" : "2017-08-08 20:40:38 ERROR ArgumentCheker:12 - argument verification failed\njava.lang.IllegalArgumentException: Please refer to the documentation.\n\tat io.harness.ArgumentCheker.verifyArgument(ArgumentCheker.java:12)\n\tat inside.RequestException.doGet(RequestException.java:52)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:635)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:742)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:199)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:475)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:80)\n\tat org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:624)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:498)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:796)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1366)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n\tat java.lang.Thread.run(Thread.java:748)",
    "logMD5Hash" : "b86ae9aa65ec8c73374068de180042bd",
    "processed" : false,
    "logCollectionMinute" : 1,
    "createdAt" : 1502249940162,
    "lastUpdatedAt" : 1502249940162
    }, ...]

    Process
    -------------

    1.) Break the input texts in chunks of 100
    2.) Create the term frequency matrix for each in parallel using the HashingVectorizer
    3.) Combine the term frequencies into a single matrix and compute the TF-IDF matrix
    4.) Compute pairwise Jaccard similarity
    5.) Create clusters from pairwise Jaccard similarities and the given threshold as follows:
        Let D be the set of documents denoted by d1..di..dj..dn. Let J(di,dj) denote the Jaccard similarity
        between document di and dj
        i.)   start with first document d0. Get all documents dk such that J(d0,dk) > threshold for k -> 0 to n
              Create cluster with document d0 with label = 0 and count = count(dk)
        ii.)  Increment label
        iii.) Find next document di without a cluster assignment. Repeat step i.) for di.

    """

    def __init__(self, options, texts):
        self.tfidf_feature_matrix = None
        self._options = options
        self.texts = texts

    def create_hashing_feature_matrix(self):
        logger.info("creating feature matrix")
        chunk_size = 100
        jobs = []
        queue = multiprocessing.Queue()
        job_id = 0
        for i in xrange(0, len(self.texts), chunk_size):
            chunk = self.texts[i:i + chunk_size]
            # TODO create a job pool
            p = multiprocessing.Process(target=create_feature_matrix_worker, args=(job_id, np.array(chunk), queue,))
            job_id = job_id + 1
            jobs.append(p)
            p.start()
            logger.debug('processing chunk ' + str(job_id))

        result = [0] * len(jobs)
        processed = 0
        while processed < len(jobs):
            # TODO - get blocks forever. Will java kill us?
            val = queue.get()
            mat = sps.csr_matrix((val.get('data'), val.get('indices'), val.get('indptr')), shape=val.get('shape'),
                                 copy=False)
            result[val.get('job_id')] = mat
            processed = processed + 1

        self.tfidf_feature_matrix = sps.vstack(result)
        tfidf_transformer = TfidfTransformer()
        self.tfidf_feature_matrix = tfidf_transformer.fit_transform(self.tfidf_feature_matrix)
        # TODO - apply max_df = 1 to tfidf_feature_matrix
        for j in jobs:
            j.terminate()
        logger.info("done creating feature matrix")

    def create_tfidf_feature_matrix(self):
        """
        For comparison with the HashingVectorizer to evaluate quality.
        This is not used as part of the prod worfklow
        """
        logger.info("creating feature matrix")
        vectorizer = TFIDFVectorizer(Tokenizer.default_tokenizer, 1, 0.99)
        self.tfidf_feature_matrix = vectorizer.fit_transform(np.array(self.texts))
        logger.info("done creating feature matrix")

    def create_clusters(self):
        logger.info("clustering...")
        x = pairwise_jaccard_similarity(self.tfidf_feature_matrix)
        logger.info(x.shape)

        label = 0
        clusters = np.array([-1] * x.shape[0])
        counts = np.array([0] * x.shape[0])
        for i in range(x.shape[0]):
            if clusters[i] == -1:
                cols = np.where(x[i, :] > self._options.sim_threshold)[1]
                clusters[cols] = label
                counts[cols] = len(cols)
                label = label + 1

        logger.debug(clusters)
        logger.debug(np.unique(clusters))
        logger.info("done clustering")
        return clusters, counts

    def run(self):
        start = time.time()
        self.create_hashing_feature_matrix()
        clusters, counts = self.create_clusters()
        logger.info('complete run with time ' + str(time.time() - start) + ' seconds')
        return clusters, counts


def parse(cli_args):
    parser = argparse.ArgumentParser()
    parser.add_argument("--input_url", required=True)
    parser.add_argument("--output_url", required=True)
    parser.add_argument("--auth_token", required=True)
    parser.add_argument("--sim_threshold", type=float, required=True)
    parser.add_argument("--cluster_level", type=int, required=True)
    parser.add_argument("--debug", required=False)

    # unwanted parameters. only here since the java api needs it
    parser.add_argument("--application_id", required=True)
    parser.add_argument("--workflow_id", required=True)
    parser.add_argument("--service_id", required=True)
    parser.add_argument("--state_execution_id", type=str, required=True)
    parser.add_argument("--query", nargs='+', type=str, required=True)
    parser.add_argument("--log_collection_minute", type=int, required=True)
    parser.add_argument("--nodes", nargs='+', type=str, required=True)

    # To debug from file locally
    parser.add_argument("--input_file", required=False)

    return parser.parse_args(cli_args)


def post_to_wings_server(options, results):
    SplunkHarnessLoader.post_to_wings_server(options.output_url, options.auth_token,
                                             json.dumps(results))


def load_from_wings_server(options):
    raw_events = SplunkHarnessLoader.load_from_harness_raw(options.input_url,
                                                           options.auth_token,
                                                           options.application_id,
                                                           options.workflow_id,
                                                           options.state_execution_id,
                                                           options.service_id,
                                                           options.log_collection_minute,
                                                           options.nodes,
                                                           options.query)['resource']
    return parse_data(raw_events, options.cluster_level)


def parse_data(raw_events, level):
    if level == 1:
        # print(self.raw_events)
        texts = [event.get('logMessage') for event in raw_events]
        logger.info('# of L0 messages = ' + str(len(texts)))
        return raw_events, texts
    else:
        texts = []
        clusters = {}
        for event in raw_events:
            if event.get('host') not in clusters:
                clusters[event.get('host')] = set()
            if event.get('clusterLabel') not in clusters[event.get('host')]:
                clusters[event.get('host')].add(event.get('clusterLabel'))
                texts.append(event.get('logMessage'))
        logger.info('# of L1 raw messages = ' + str(len(raw_events)))
        logger.info('# of L1 messages = ' + str(len(texts)))
        return raw_events, texts


def create_response(raw_events, clusters, counts, level):
    results = []
    if level == 1:
        visited_clusters = set()
        for i in range(len(clusters)):
            if clusters[i] not in visited_clusters:
                raw_events[i]['clusterLabel'] = clusters[i]
                raw_events[i]['count'] = counts[i]
                results.append(dict(query=raw_events[i].get('query'),
                                    clusterLabel=raw_events[i].get('clusterLabel'),
                                    host=raw_events[i].get('host'),
                                    timeStamp=raw_events[i].get('timeStamp'),
                                    count=raw_events[i].get('count'),
                                    logMessage=raw_events[i].get('logMessage'),
                                    logCollectionMinute=raw_events[i].get('logCollectionMinute')))
                visited_clusters.add(clusters[i])
        logger.info('# of L1 returned = ' + str(len(results)))
        return results
    else:
        i = 0
        clusters_dict = {}
        for m, event in enumerate(raw_events):
            if event.get('host') not in clusters_dict:
                clusters_dict[event.get('host')] = {}
            if event.get('clusterLabel') not in clusters_dict[event.get('host')]:
                clusters_dict[event.get('host')][event.get('clusterLabel')] = clusters[i]
                i = i + 1

            event['clusterLabel'] = clusters_dict[event.get('host')][event.get('clusterLabel')]
            results.append(dict(query=event.get('query'),
                                    clusterLabel=event.get('clusterLabel'),
                                    host=event.get('host'),
                                    timeStamp=event.get('timeStamp'),
                                    count=event.get('count'),
                                    logMessage=event.get('logMessage'),
                                    logCollectionMinute=event.get('logCollectionMinute')))
        logger.info('# of L2 returned = ' + str(len(results)))
        return results


def load_from_file(input_file, level):
    """
    Useful for running program from a file input for local debugging.
    This is not used in the prod workflow
    """
    if level == 1:
        all_events = SplunkFileSource.load_data(input_file)
        raw_events = []
        count = 0
        for idx, event in enumerate(all_events):
            if event.get('logCollectionMinute') == 1:
                count += 1
                raw_events.append(event)
        return parse_data(raw_events, level)
    elif level == 2:
        raw_events = SplunkFileSource.load_data(input_file)
        count = 0
        for idx, event in enumerate(raw_events):
            if event.get('logCollectionMinute') == 1:
                count += 1
                raw_events.append(event)
        return parse_data(raw_events, level)


def run_debug(options):
    start = time.time()
    raw_events, texts = load_from_file(options.input_file, options.cluster_level)
    ci = ClusterInput(options, texts)
    ci.create_hashing_feature_matrix()
    clusters, counts = ci.create_clusters()
    create_response(raw_events, clusters, counts, options.cluster_level)
    logger.info('complete run with time ' + str(time.time() - start) + ' seconds')


def main(args):
    logger.info(args)
    options = parse(args[1:])
    options.query = ' '.join(options.query)
    logger.info(options)

    logger.info("Running cluster level " + str(options.cluster_level))

    if options.cluster_level != 1 and options.cluster_level != 2:
        logger.error("Unknown cluster level " + str(options.cluster_level) + " . only level 1 and 2 supported")
        sys.exit(-1)

    if options.debug:
        run_debug(options)
    else:
        raw_events, texts = load_from_wings_server(options)
        if raw_events is None or len(raw_events) == 0:
            logger.warn("No inputs to cluster")
            sys.exit(2)
        cluster_input = ClusterInput(options, texts)
        clusters, counts = cluster_input.run()
        results = create_response(raw_events, clusters, counts, options.cluster_level)
        post_to_wings_server(options, results)


if __name__ == "__main__":
    main(sys.argv)
