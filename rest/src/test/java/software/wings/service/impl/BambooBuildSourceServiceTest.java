package software.wings.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static software.wings.beans.SettingAttribute.Builder.aSettingAttribute;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import software.wings.WingsBaseTest;
import software.wings.beans.BambooConfig;
import software.wings.beans.DelegateTask.SyncTaskContext;
import software.wings.beans.Service;
import software.wings.beans.SettingAttribute;
import software.wings.beans.SettingAttribute.Category;
import software.wings.beans.artifact.ArtifactStreamType;
import software.wings.beans.artifact.BambooArtifactStream;
import software.wings.delegatetasks.DelegateProxyFactory;
import software.wings.dl.WingsPersistence;
import software.wings.helpers.ext.jenkins.BuildDetails;
import software.wings.helpers.ext.jenkins.JobDetails;
import software.wings.rules.RealMongo;
import software.wings.service.intfc.BambooBuildService;
import software.wings.service.intfc.BuildSourceService;
import software.wings.utils.ArtifactType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by rsingh on 10/9/17.
 */
public class BambooBuildSourceServiceTest extends WingsBaseTest {
  private String accountId;
  private String appId;
  private SettingAttribute settingAttribute;
  private ArtifactStreamType streamType = ArtifactStreamType.BAMBOO;
  @Mock private DelegateProxyFactory delegateProxyFactory;
  @Inject private BuildSourceService buildSourceService;
  @Inject private WingsPersistence wingsPersistence;
  @Inject private BambooBuildService bambooBuildService;

  @Before
  public void setup() {
    accountId = UUID.randomUUID().toString();
    appId = UUID.randomUUID().toString();
    MockitoAnnotations.initMocks(this);
    when(delegateProxyFactory.get(Mockito.anyObject(), Mockito.any(SyncTaskContext.class)))
        .thenReturn(bambooBuildService);
    setInternalState(buildSourceService, "delegateProxyFactory", delegateProxyFactory);
    settingAttribute = aSettingAttribute()
                           .withName("bamboo")
                           .withCategory(Category.CONNECTOR)
                           .withAccountId(accountId)
                           .withValue(BambooConfig.builder()
                                          .accountId(accountId)
                                          .bambooUrl("http://ec2-34-205-16-35.compute-1.amazonaws.com:8085/")
                                          .username("wingsbuild")
                                          .password("0db28aa0f4fc0685df9a216fc7af0ca96254b7c2".toCharArray())
                                          .build())
                           .build();
    wingsPersistence.save(settingAttribute);
  }

  @Test
  @RealMongo
  public void getJobs() {
    Set<JobDetails> jobs = buildSourceService.getJobs(appId, settingAttribute.getUuid(), null);
    assertTrue(jobs.size() > 0);
  }

  @Test
  @RealMongo
  public void getPlans() {
    Map<String, String> plans = buildSourceService.getPlans(appId, settingAttribute.getUuid(), streamType.name());
    assertTrue(plans.size() > 0);
  }

  @Test
  @RealMongo
  public void getPlansWithType() {
    Service service =
        Service.Builder.aService().withAppId(appId).withArtifactType(ArtifactType.WAR).withName("Some service").build();
    wingsPersistence.save(service);
    Map<String, String> plans =
        buildSourceService.getPlans(appId, settingAttribute.getUuid(), service.getUuid(), streamType.name());
    assertTrue(plans.size() > 0);
  }

  @Test
  @RealMongo
  public void getArtifactPaths() {
    Set<String> artifactPaths =
        buildSourceService.getArtifactPaths(appId, "TOD-TOD", settingAttribute.getUuid(), null, streamType.name());
    assertTrue(artifactPaths.size() > 0);
    assertTrue(artifactPaths.contains("artifacts/todolist.war"));
  }

  @Test
  @RealMongo
  public void getBuilds() {
    Service service =
        Service.Builder.aService().withAppId(appId).withArtifactType(ArtifactType.WAR).withName("Some service").build();
    wingsPersistence.save(service);
    BambooArtifactStream artifactStream = new BambooArtifactStream();
    artifactStream.setJobname("TOD-TOD");
    artifactStream.setArtifactPaths(Collections.singletonList("artifacts/todolist.war"));
    artifactStream.setServiceId(service.getUuid());
    artifactStream.setAppId(appId);
    wingsPersistence.save(artifactStream);

    List<BuildDetails> builds =
        buildSourceService.getBuilds(appId, artifactStream.getUuid(), settingAttribute.getUuid());
    assertTrue(builds.size() > 0);
  }

  @Test
  @RealMongo
  public void getLastSuccessfulBuild() {
    Service service =
        Service.Builder.aService().withAppId(appId).withArtifactType(ArtifactType.WAR).withName("Some service").build();
    wingsPersistence.save(service);
    BambooArtifactStream artifactStream = new BambooArtifactStream();
    artifactStream.setJobname("TOD-TOD");
    artifactStream.setArtifactPaths(Collections.singletonList("artifacts/todolist.war"));
    artifactStream.setServiceId(service.getUuid());
    artifactStream.setAppId(appId);
    wingsPersistence.save(artifactStream);

    BuildDetails build =
        buildSourceService.getLastSuccessfulBuild(appId, artifactStream.getUuid(), settingAttribute.getUuid());
    assertNotNull(build);
  }
}
