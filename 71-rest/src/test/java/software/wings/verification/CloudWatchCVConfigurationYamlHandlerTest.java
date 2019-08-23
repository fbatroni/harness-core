package software.wings.verification;

import static org.apache.cxf.ws.addressing.ContextUtils.generateUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import io.harness.category.element.UnitTests;
import io.harness.exception.WingsException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.wings.WingsBaseTest;
import software.wings.beans.Application;
import software.wings.beans.Environment;
import software.wings.beans.Service;
import software.wings.beans.SettingAttribute;
import software.wings.beans.yaml.Change;
import software.wings.beans.yaml.ChangeContext;
import software.wings.service.impl.cloudwatch.CloudWatchMetric;
import software.wings.service.impl.yaml.service.YamlHelper;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.CloudWatchService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.verification.CVConfigurationService;
import software.wings.sm.StateType;
import software.wings.verification.cloudwatch.CloudWatchCVServiceConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CloudWatchCVConfigurationYamlHandlerTest extends WingsBaseTest {
  @Mock YamlHelper yamlHelper;
  @Mock CVConfigurationService cvConfigurationService;
  @Mock EnvironmentService environmentService;
  @Mock ServiceResourceService serviceResourceService;
  @Mock AppService appService;
  @Mock SettingsService settingsService;
  @Mock CloudWatchService cloudWatchService;

  CloudWatchCVConfigurationYamlHandler yamlHandler = new CloudWatchCVConfigurationYamlHandler();

  private String envId;
  private String serviceId;
  private String appId;
  private String connectorId;
  private String accountId;

  private String envName = "EnvName";
  private String appName = "AppName";
  private String serviceName = "serviceName";
  private String connectorName = "cloudWatchConnector";
  private String region = "US(West)";

  private Map<String, String> validEC2Instances = new HashMap<String, String>() {
    { put("ValidEC2InstanceName", "ValidEC2InstanceId"); }
  };
  private List<String> validECSCluster = Arrays.asList("ValidCluster1", "ValidCluster2");
  private List<String> validLambdaFunction = Collections.singletonList("ValidLambdaFunction");
  private Set<String> validLoadBalancer = new HashSet<>(Collections.singletonList("ValidLoadBalancer"));

  private Map<String, List<CloudWatchMetric>> loadBalancerMetrics =
      Collections.singletonMap("ValidLoadBalancer", new ArrayList<>());
  private Map<String, List<CloudWatchMetric>> ecsMetrics = Collections.singletonMap("ValidCluster2", new ArrayList<>());
  private Map<String, List<CloudWatchMetric>> lambdaFunctionsMetrics =
      Collections.singletonMap("ValidLambdaFunction", new ArrayList<>());
  private List<String> ec2InstanceNames = new ArrayList<>(validEC2Instances.keySet());
  private List<CloudWatchMetric> ec2Metrics = new ArrayList<>();

  private Map<String, List<CloudWatchMetric>> invalidLoadBalancerMetrics =
      Collections.singletonMap("InvalidLoadBalancer", new ArrayList<>());
  private Map<String, List<CloudWatchMetric>> invalidEcsMetrics =
      Collections.singletonMap("InvalidCluster2", new ArrayList<>());
  private Map<String, List<CloudWatchMetric>> invalidLambdaFunctionsMetrics =
      Collections.singletonMap("InvalidLambdaFunction", new ArrayList<>());
  private List<String> invalidEc2InstanceNames = Collections.singletonList("InvalidEC2Instance");

  @Before
  public void setup() throws Exception {
    accountId = generateUUID();
    envId = generateUUID();
    serviceId = generateUUID();
    appId = generateUUID();
    connectorId = generateUUID();

    MockitoAnnotations.initMocks(this);
    FieldUtils.writeField(yamlHandler, "yamlHelper", yamlHelper, true);
    FieldUtils.writeField(yamlHandler, "cvConfigurationService", cvConfigurationService, true);
    FieldUtils.writeField(yamlHandler, "appService", appService, true);
    FieldUtils.writeField(yamlHandler, "environmentService", environmentService, true);
    FieldUtils.writeField(yamlHandler, "serviceResourceService", serviceResourceService, true);
    FieldUtils.writeField(yamlHandler, "settingsService", settingsService, true);
    FieldUtils.writeField(yamlHandler, "cloudWatchService", cloudWatchService, true);

    Environment env = Environment.Builder.anEnvironment().uuid(envId).name(envName).build();
    when(environmentService.getEnvironmentByName(appId, envName)).thenReturn(env);
    when(environmentService.get(appId, envId)).thenReturn(env);

    Service service = Service.builder().uuid(serviceId).name(serviceName).build();
    when(serviceResourceService.get(appId, serviceId)).thenReturn(service);
    when(serviceResourceService.getServiceByName(appId, serviceName)).thenReturn(service);

    Application app = Application.Builder.anApplication().name(appName).uuid(appId).build();
    when(appService.get(appId)).thenReturn(app);
    when(appService.getAppByName(accountId, appName)).thenReturn(app);

    when(cloudWatchService.getEC2Instances(connectorId, region)).thenReturn(validEC2Instances);
    when(cloudWatchService.getECSClusterNames(connectorId, region)).thenReturn(validECSCluster);
    when(cloudWatchService.getLambdaFunctionsNames(connectorId, region)).thenReturn(validLambdaFunction);
    when(cloudWatchService.getLoadBalancerNames(connectorId, region)).thenReturn(validLoadBalancer);

    SettingAttribute settingAttribute =
        SettingAttribute.Builder.aSettingAttribute().withName(connectorName).withUuid(connectorId).build();
    when(settingsService.getSettingAttributeByName(accountId, connectorName)).thenReturn(settingAttribute);
    when(settingsService.get(connectorId)).thenReturn(settingAttribute);
  }

  private void setBasicInfo(CloudWatchCVServiceConfiguration cvServiceConfiguration) {
    cvServiceConfiguration.setStateType(StateType.CLOUD_WATCH);
    cvServiceConfiguration.setAccountId(accountId);
    cvServiceConfiguration.setServiceId(serviceId);
    cvServiceConfiguration.setConnectorId(connectorId);
    cvServiceConfiguration.setEnvId(envId);
    cvServiceConfiguration.setAppId(appId);
    cvServiceConfiguration.setEnabled24x7(true);
    cvServiceConfiguration.setName("TestCloudWatchConfig");
  }

  private CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml buildYaml() {
    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml =
        CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml.builder()
            .loadBalancerMetrics(loadBalancerMetrics)
            .ecsMetrics(ecsMetrics)
            .lambdaFunctionsMetrics(lambdaFunctionsMetrics)
            .ec2InstanceNames(ec2InstanceNames)
            .ec2Metrics(ec2Metrics)
            .region(region)
            .build();
    yaml.setName("TestCloudWatchConfig");
    yaml.setAccountId(accountId);
    yaml.setServiceName(serviceName);
    yaml.setEnvName(envName);
    yaml.setConnectorName(connectorName);
    yaml.setHarnessApplicationName(appName);
    return yaml;
  }

  @Test
  @Category(UnitTests.class)
  public void testToYaml() {
    final String appId = "appId";
    CloudWatchCVServiceConfiguration cvServiceConfiguration = CloudWatchCVServiceConfiguration.builder()
                                                                  .loadBalancerMetrics(loadBalancerMetrics)
                                                                  .ecsMetrics(ecsMetrics)
                                                                  .lambdaFunctionsMetrics(lambdaFunctionsMetrics)
                                                                  .ec2InstanceNames(ec2InstanceNames)
                                                                  .ec2Metrics(ec2Metrics)
                                                                  .region(region)
                                                                  .build();
    setBasicInfo(cvServiceConfiguration);

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml =
        yamlHandler.toYaml(cvServiceConfiguration, appId);

    assertThat(yaml.getName()).isEqualTo(cvServiceConfiguration.getName());
    assertThat(yaml.getAccountId()).isEqualTo(cvServiceConfiguration.getAccountId());
    assertThat(yaml.getServiceName()).isEqualTo(serviceName);
    assertThat(yaml.getEnvName()).isEqualTo(envName);
    assertThat(yaml.getLoadBalancerMetrics()).isEqualTo(loadBalancerMetrics);
    assertThat(yaml.getEcsMetrics()).isEqualTo(ecsMetrics);
    assertThat(yaml.getLambdaFunctionsMetrics()).isEqualTo(lambdaFunctionsMetrics);
    assertThat(yaml.getEc2InstanceNames()).isEqualTo(ec2InstanceNames);
    assertThat(yaml.getEc2Metrics()).isEqualTo(ec2Metrics);
    assertThat(yaml.getRegion()).isEqualTo(region);
  }

  @Test
  @Category(UnitTests.class)
  public void testUpsert() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    CloudWatchCVServiceConfiguration bean = yamlHandler.upsertFromYaml(changeContext, null);

    assertThat(bean.getName()).isEqualTo(yaml.getName());
    assertThat(bean.getAccountId()).isEqualTo(yaml.getAccountId());
    assertThat(bean.getServiceId()).isEqualTo(serviceId);
    assertThat(bean.getEnvId()).isEqualTo(envId);
    assertThat(bean.getLoadBalancerMetrics()).isEqualTo(loadBalancerMetrics);
    assertThat(bean.getEcsMetrics()).isEqualTo(ecsMetrics);
    assertThat(bean.getLambdaFunctionsMetrics()).isEqualTo(lambdaFunctionsMetrics);
    assertThat(bean.getEc2InstanceNames()).isEqualTo(ec2InstanceNames);
    assertThat(bean.getEc2Metrics()).isEqualTo(ec2Metrics);
    assertThat(bean.getRegion()).isEqualTo(region);
    assertThat(bean.getUuid()).isNotNull();
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void testUpsertEmptyMetrics() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    yaml.setLambdaFunctionsMetrics(new HashMap<>());
    yaml.setEc2InstanceNames(new ArrayList<>());
    yaml.setEcsMetrics(new HashMap<>());
    yaml.setLoadBalancerMetrics(new HashMap<>());
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    yamlHandler.upsertFromYaml(changeContext, null);
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void testUpsertInvalidLambdaFunction() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    yaml.setLambdaFunctionsMetrics(invalidLambdaFunctionsMetrics);
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    yamlHandler.upsertFromYaml(changeContext, null);
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void testUpsertInvalidEC2Instance() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    yaml.setEc2InstanceNames(invalidEc2InstanceNames);
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    yamlHandler.upsertFromYaml(changeContext, null);
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void testUpsertInvalidECSCluster() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    yaml.setEcsMetrics(invalidEcsMetrics);
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    yamlHandler.upsertFromYaml(changeContext, null);
  }

  @Test(expected = WingsException.class)
  @Category(UnitTests.class)
  public void testUpsertInvalidLoadBalancer() {
    when(yamlHelper.getAppId(anyString(), anyString())).thenReturn(appId);
    when(yamlHelper.getEnvironmentId(anyString(), anyString())).thenReturn(envId);
    when(yamlHelper.getNameFromYamlFilePath("TestCloudWatchConfig.yaml")).thenReturn("TestCloudWatchConfig");

    CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml yaml = buildYaml();
    yaml.setLoadBalancerMetrics(invalidLoadBalancerMetrics);
    ChangeContext<CloudWatchCVServiceConfiguration.CloudWatchCVConfigurationYaml> changeContext = new ChangeContext<>();
    Change c = Change.Builder.aFileChange().withAccountId(accountId).withFilePath("TestCloudWatchConfig.yaml").build();
    changeContext.setChange(c);
    changeContext.setYaml(yaml);
    yamlHandler.upsertFromYaml(changeContext, null);
  }
}
