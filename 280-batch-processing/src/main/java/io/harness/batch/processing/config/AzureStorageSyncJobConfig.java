package io.harness.batch.processing.config;

import io.harness.batch.processing.ccm.BatchJobType;
import io.harness.batch.processing.reader.SettingAttributeReader;
import io.harness.batch.processing.svcmetrics.BatchJobExecutionListener;
import io.harness.batch.processing.writer.AzureStorageSyncEventWriter;
import io.harness.metrics.service.api.MetricService;

import software.wings.beans.SettingAttribute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AzureStorageSyncJobConfig {
  private static final int BATCH_SIZE = 10;

  @Autowired private JobBuilderFactory jobBuilderFactory;
  @Autowired private StepBuilderFactory stepBuilderFactory;
  @Autowired private MetricService metricService;

  @Bean
  @Qualifier(value = "storageSyncJob")
  public Job storageSyncJob(JobBuilderFactory jobBuilderFactory, Step storageSyncStep) {
    return jobBuilderFactory.get(BatchJobType.SYNC_BILLING_REPORT_AZURE.name())
        .incrementer(new RunIdIncrementer())
        .listener(new BatchJobExecutionListener(metricService))
        .start(storageSyncStep)
        .build();
  }

  // TODO: refactor to use tasklet, dummySettingAttributeReader is not required, like
  // stepBuilderFactory.get("storageSyncStep").tasklet(storageSyncWriter()).build();
  @Bean
  public Step storageSyncStep(StepBuilderFactory stepBuilderFactory, SettingAttributeReader settingAttributeReader) {
    return stepBuilderFactory.get("storageSyncStep")
        .<SettingAttribute, SettingAttribute>chunk(BATCH_SIZE)
        .reader(settingAttributeReader)
        .writer(storageSyncWriter())
        .build();
  }

  @Bean
  public ItemWriter<SettingAttribute> storageSyncWriter() {
    return new AzureStorageSyncEventWriter();
  }
}
