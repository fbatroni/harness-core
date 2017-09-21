package software.wings.service.impl;

import static java.util.stream.Collectors.toMap;
import static org.mongodb.morphia.mapping.Mapper.ID_KEY;
import static software.wings.beans.Base.GLOBAL_ENV_ID;
import static software.wings.beans.EntityType.SERVICE;
import static software.wings.beans.EntityType.SERVICE_TEMPLATE;
import static software.wings.beans.Environment.Builder.anEnvironment;
import static software.wings.beans.Environment.EnvironmentType.NON_PROD;
import static software.wings.beans.Environment.EnvironmentType.PROD;
import static software.wings.beans.ErrorCode.INVALID_ARGUMENT;
import static software.wings.beans.ErrorCode.INVALID_REQUEST;
import static software.wings.beans.InformationNotification.Builder.anInformationNotification;
import static software.wings.beans.SearchFilter.Operator.EQ;
import static software.wings.beans.ServiceVariable.DEFAULT_TEMPLATE_ID;
import static software.wings.dl.PageRequest.Builder.aPageRequest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.ConfigFile;
import software.wings.beans.Environment;
import software.wings.beans.ErrorCode;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.Pipeline;
import software.wings.beans.SearchFilter;
import software.wings.beans.Service;
import software.wings.beans.ServiceTemplate;
import software.wings.beans.ServiceVariable;
import software.wings.beans.Setup.SetupStatus;
import software.wings.beans.stats.CloneMetadata;
import software.wings.common.Constants;
import software.wings.common.NotificationMessageResolver.NotificationMessageType;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsPersistence;
import software.wings.exception.WingsException;
import software.wings.service.intfc.ActivityService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.InfrastructureMappingService;
import software.wings.service.intfc.NotificationService;
import software.wings.service.intfc.PipelineService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.ServiceTemplateService;
import software.wings.service.intfc.ServiceVariableService;
import software.wings.service.intfc.SetupService;
import software.wings.service.intfc.WorkflowService;
import software.wings.stencils.DataProvider;
import software.wings.utils.Validator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ValidateOnExecution;

/**
 * Created by anubhaw on 4/1/16.
 */
@ValidateOnExecution
@Singleton
public class EnvironmentServiceImpl implements EnvironmentService, DataProvider {
  @Inject private WingsPersistence wingsPersistence;
  @Inject private ServiceTemplateService serviceTemplateService;
  @Inject private ExecutorService executorService;
  @Inject private WorkflowService workflowService;
  @Inject private SetupService setupService;
  @Inject private NotificationService notificationService;
  @Inject private ActivityService activityService;
  @Inject private PipelineService pipelineService;
  @Inject private InfrastructureMappingService infrastructureMappingService;
  @Inject private ServiceVariableService serviceVariableService;
  @Inject private ServiceResourceService serviceResourceService;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * {@inheritDoc}
   */
  @Override
  public PageResponse<Environment> list(PageRequest<Environment> request, boolean withSummary) {
    PageResponse<Environment> pageResponse = wingsPersistence.query(Environment.class, request);
    if (withSummary) {
      pageResponse.getResponse().forEach(environment -> {
        try {
          addServiceTemplates(environment);
        } catch (Exception e) {
          logger.error("Failed to add service templates to environment {} ", environment, e);
        }
      });
    }
    return pageResponse;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Environment get(String appId, String envId, boolean withSummary) {
    Environment environment = wingsPersistence.get(Environment.class, appId, envId);
    if (environment == null) {
      throw new WingsException(INVALID_ARGUMENT, "args", "Environment doesn't exist");
    }
    if (withSummary) {
      addServiceTemplates(environment);
    }
    return environment;
  }

  @Override
  public Environment get(@NotEmpty String appId, @NotEmpty String envId, @NotNull SetupStatus status) {
    Environment environment = get(appId, envId, true);
    if (status == SetupStatus.INCOMPLETE) {
      environment.setSetup(setupService.getEnvironmentSetupStatus(environment));
    }
    return environment;
  }

  @Override
  public boolean exist(@NotEmpty String appId, @NotEmpty String envId) {
    return wingsPersistence.createQuery(Environment.class)
               .field("appId")
               .equal(appId)
               .field(ID_KEY)
               .equal(envId)
               .getKey()
        != null;
  }

  private void addServiceTemplates(Environment environment) {
    PageRequest<ServiceTemplate> pageRequest = new PageRequest<>();
    pageRequest.addFilter("appId", environment.getAppId(), SearchFilter.Operator.EQ);
    pageRequest.addFilter("envId", environment.getUuid(), EQ);
    environment.setServiceTemplates(serviceTemplateService.list(pageRequest, false, false).getResponse());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> getData(String appId, String... params) {
    PageRequest<Environment> pageRequest = new PageRequest<>();
    pageRequest.addFilter("appId", appId, EQ);
    return list(pageRequest, false).stream().collect(toMap(Environment::getUuid, Environment::getName));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Environment save(Environment environment) {
    environment = wingsPersistence.saveAndGet(Environment.class, environment);
    serviceTemplateService.createDefaultTemplatesByEnv(environment);
    notificationService.sendNotificationAsync(
        anInformationNotification()
            .withAppId(environment.getAppId())
            .withNotificationTemplateId(NotificationMessageType.ENTITY_CREATE_NOTIFICATION.name())
            .withNotificationTemplateVariables(
                ImmutableMap.of("ENTITY_TYPE", "Environment", "ENTITY_NAME", environment.getName()))
            .build());
    return environment;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Environment update(Environment environment) {
    String description = environment.getDescription() == null ? "" : environment.getDescription();
    ImmutableMap<String, Object> paramMap = ImmutableMap.of(
        "name", environment.getName(), "environmentType", environment.getEnvironmentType(), "description", description);

    wingsPersistence.updateFields(Environment.class, environment.getUuid(), paramMap);
    return wingsPersistence.get(Environment.class, environment.getAppId(), environment.getUuid());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(String appId, String envId) {
    Environment environment = wingsPersistence.get(Environment.class, appId, envId);
    if (environment == null) {
      throw new WingsException(INVALID_ARGUMENT, "args", "Environment doesn't exist");
    }
    ensureEnvironmentSafeToDelete(environment);
    delete(environment);
  }

  private void ensureEnvironmentSafeToDelete(Environment environment) {
    List<Pipeline> pipelines = pipelineService.listPipelines(
        aPageRequest()
            .withLimit(PageRequest.UNLIMITED)
            .addFilter("appId", EQ, environment.getAppId())
            .addFilter("pipelineStages.pipelineStageElements.properties.envId", EQ, environment.getUuid())
            .build());

    if (pipelines.size() > 0) {
      List<String> pipelineNames = pipelines.stream().map(Pipeline::getName).collect(Collectors.toList());
      throw new WingsException(INVALID_REQUEST, "message",
          String.format("Environment is referenced by %s pipeline%s [%s].", pipelines.size(),
              pipelines.size() == 1 ? "" : "s", Joiner.on(", ").join(pipelineNames)));
    }
  }

  private void delete(Environment environment) {
    boolean deleted = wingsPersistence.delete(environment);

    if (deleted) {
      executorService.submit(() -> serviceTemplateService.deleteByEnv(environment.getAppId(), environment.getUuid()));
      executorService.submit(
          () -> workflowService.deleteWorkflowByEnvironment(environment.getAppId(), environment.getUuid()));
      executorService.submit(() -> activityService.deleteByEnvironment(environment.getAppId(), environment.getUuid()));
      notificationService.sendNotificationAsync(
          anInformationNotification()
              .withAppId(environment.getAppId())
              .withNotificationTemplateId(NotificationMessageType.ENTITY_DELETE_NOTIFICATION.name())
              .withNotificationTemplateVariables(
                  ImmutableMap.of("ENTITY_TYPE", "Environment", "ENTITY_NAME", environment.getName()))
              .build());
    }
  }

  @Override
  public void deleteByApp(String appId) {
    List<Environment> environments =
        wingsPersistence.createQuery(Environment.class).field("appId").equal(appId).asList();
    environments.forEach(this ::delete);
  }

  @Override
  public void createDefaultEnvironments(String appId) {
    save(anEnvironment().withAppId(appId).withName(Constants.DEV_ENV).withEnvironmentType(NON_PROD).build());
    save(anEnvironment().withAppId(appId).withName(Constants.QA_ENV).withEnvironmentType(NON_PROD).build());
    save(anEnvironment().withAppId(appId).withName(Constants.PROD_ENV).withEnvironmentType(PROD).build());
  }

  @Override
  public List<Environment> getEnvByApp(String appId) {
    return wingsPersistence.createQuery(Environment.class).field("appId").equal(appId).asList();
  }

  @Override
  public Environment cloneEnvironment(String appId, String envId, CloneMetadata cloneMetadata) {
    Validator.notNullCheck("cloneMetadata", cloneMetadata);
    Validator.notNullCheck("environment", cloneMetadata.getEnvironment());
    if (cloneMetadata.getTargetAppId() == null) {
      logger.info("Cloning environment envId {}  within the same appId {}", envId, appId);
      String envName = cloneMetadata.getEnvironment().getName();
      String description = cloneMetadata.getEnvironment().getDescription();
      if (envId == null) {
        envId = cloneMetadata.getEnvironment().getUuid();
      }
      Environment sourceEnvironment = get(appId, envId, true);
      Environment clonedEnvironment = sourceEnvironment.clone();
      clonedEnvironment.setName(envName);
      clonedEnvironment.setDescription(description);

      // Create environment
      clonedEnvironment = save(clonedEnvironment);

      // Copy templates
      List<ServiceTemplate> serviceTemplates = sourceEnvironment.getServiceTemplates();
      if (serviceTemplates != null) {
        for (ServiceTemplate serviceTemplate : serviceTemplates) {
          ServiceTemplate clonedServiceTemplate = serviceTemplate.clone();
          clonedServiceTemplate.setEnvId(clonedEnvironment.getUuid());

          clonedServiceTemplate = serviceTemplateService.save(clonedServiceTemplate);
          serviceTemplate =
              serviceTemplateService.get(appId, serviceTemplate.getEnvId(), serviceTemplate.getUuid(), true, true);
          if (serviceTemplate != null) {
            // Clone Infrastructure Mappings
            List<InfrastructureMapping> infrastructureMappings = serviceTemplate.getInfrastructureMappings();
            if (infrastructureMappings != null) {
              for (InfrastructureMapping infrastructureMapping : infrastructureMappings) {
                infrastructureMapping.setUuid(null);
                infrastructureMapping.setEnvId(clonedEnvironment.getUuid());
                infrastructureMapping.setServiceTemplateId(clonedServiceTemplate.getUuid());
                infrastructureMappingService.save(infrastructureMapping);
              }
            }
            // Clone Service Variable
            cloneServiceVariables(
                clonedEnvironment, clonedServiceTemplate, serviceTemplate.getServiceVariables(), null, null);
            // Clone Service Variable overrides
            cloneServiceVariables(
                clonedEnvironment, clonedServiceTemplate, serviceTemplate.getServiceVariablesOverrides(), null, null);
            // Clone Service Config Files
            cloneConfigFiles(
                clonedEnvironment, clonedServiceTemplate, serviceTemplate.getServiceConfigFiles(), null, null);
            // Clone Service Config File overrides
            cloneConfigFiles(
                clonedEnvironment, clonedServiceTemplate, serviceTemplate.getConfigFilesOverrides(), null, null);
          }
        }
      }
      logger.info("Cloning environment envId {}  within the same appId {} success", envId, appId);
    } else {
      String targetAppId = cloneMetadata.getTargetAppId();
      logger.info("Cloning environment from appId {} to appId {}", appId, targetAppId);
      Map<String, String> serviceMapping = cloneMetadata.getServiceMapping();
      Validator.notNullCheck("serviceMapping", serviceMapping);
      validateServiceMapping(appId, targetAppId, serviceMapping);

      String envName = cloneMetadata.getEnvironment().getName();
      String description = cloneMetadata.getEnvironment().getDescription();
      if (envId == null) {
        envId = cloneMetadata.getEnvironment().getUuid();
      }
      Environment sourceEnvironment = get(appId, envId, true);
      Environment clonedEnvironment = sourceEnvironment.clone();
      clonedEnvironment.setName(envName);
      clonedEnvironment.setDescription(description);
      clonedEnvironment.setAppId(targetAppId);

      // Create environment
      clonedEnvironment = save(clonedEnvironment);

      // Copy templates
      List<ServiceTemplate> serviceTemplates = sourceEnvironment.getServiceTemplates();
      if (serviceTemplates != null) {
        for (ServiceTemplate serviceTemplate : serviceTemplates) {
          ServiceTemplate clonedServiceTemplate = serviceTemplate.clone();
          clonedServiceTemplate.setAppId(targetAppId);
          clonedServiceTemplate.setEnvId(clonedEnvironment.getUuid());
          String serviceId = serviceTemplate.getServiceId();
          String targetServiceId = serviceMapping.get(serviceId);

          clonedServiceTemplate.setServiceId(targetServiceId);

          clonedServiceTemplate = serviceTemplateService.save(clonedServiceTemplate);
          serviceTemplate =
              serviceTemplateService.get(appId, serviceTemplate.getEnvId(), serviceTemplate.getUuid(), true, true);
          if (serviceTemplate != null) {
            // Clone Infrastructure Mappings
            List<InfrastructureMapping> infrastructureMappings = serviceTemplate.getInfrastructureMappings();
            if (infrastructureMappings != null) {
              for (InfrastructureMapping infrastructureMapping : infrastructureMappings) {
                infrastructureMapping.setUuid(null);
                infrastructureMapping.setAppId(clonedServiceTemplate.getAppId());
                infrastructureMapping.setServiceId(targetServiceId);
                infrastructureMapping.setEnvId(clonedEnvironment.getUuid());
                infrastructureMapping.setServiceTemplateId(clonedServiceTemplate.getUuid());
                infrastructureMappingService.save(infrastructureMapping);
              }
            }
            // Clone Service Variable
            cloneServiceVariables(clonedEnvironment, clonedServiceTemplate, serviceTemplate.getServiceVariables(),
                targetAppId, targetServiceId);
            // Clone Service Variable overrides
            cloneServiceVariables(clonedEnvironment, clonedServiceTemplate,
                serviceTemplate.getServiceVariablesOverrides(), targetAppId, targetServiceId);
            // Clone Service Config Files
            cloneConfigFiles(clonedEnvironment, clonedServiceTemplate, serviceTemplate.getServiceConfigFiles(),
                targetAppId, targetServiceId);
            // Clone Service Config File overrides
            cloneConfigFiles(clonedEnvironment, clonedServiceTemplate, serviceTemplate.getConfigFilesOverrides(),
                targetAppId, targetServiceId);
          }
        }
      }
    }
    return null;
  }

  private void cloneServiceVariables(Environment clonedEnvironment, ServiceTemplate clonedServiceTemplate,
      List<ServiceVariable> serviceVariables, String targetAppId, String targetServiceId) {
    if (serviceVariables != null) {
      for (ServiceVariable serviceVariable : serviceVariables) {
        ServiceVariable clonedServiceVariable = serviceVariable.clone();
        if (targetAppId != null) {
          serviceVariable.setAppId(targetAppId);
        }
        if (!clonedServiceVariable.getEnvId().equals(GLOBAL_ENV_ID)) {
          clonedServiceVariable.setEnvId(clonedEnvironment.getUuid());
        }
        if (!clonedServiceVariable.getTemplateId().equals(DEFAULT_TEMPLATE_ID)) {
          clonedServiceVariable.setTemplateId(clonedServiceTemplate.getUuid());
        }
        if (clonedServiceVariable.getEntityType().equals(SERVICE_TEMPLATE)) {
          clonedServiceVariable.setEntityId(clonedServiceTemplate.getUuid());
        }
        if (clonedServiceVariable.getEntityType().equals(SERVICE)) {
          if (targetServiceId != null) {
            clonedServiceVariable.setEntityId(targetServiceId);
          }
        }
        serviceVariableService.save(clonedServiceVariable);
      }
    }
  }

  private void cloneConfigFiles(Environment clonedEnvironment, ServiceTemplate clonedServiceTemplate,
      List<ConfigFile> configFiles, String targetAppId, String targetServiceId) {
    if (configFiles != null) {
      for (ConfigFile configFile : configFiles) {
        ConfigFile clonedConfigFile = configFile.clone();
        if (targetAppId != null) {
          clonedConfigFile.setAppId(targetAppId);
        }
        if (!clonedConfigFile.getEnvId().equals(GLOBAL_ENV_ID)) {
          clonedConfigFile.setEnvId(clonedEnvironment.getUuid());
        }
        if (!clonedConfigFile.getTemplateId().equals(DEFAULT_TEMPLATE_ID)) {
          clonedConfigFile.setTemplateId(clonedServiceTemplate.getUuid());
        }
        if (clonedConfigFile.getEntityType().equals(SERVICE_TEMPLATE)) {
          clonedConfigFile.setEntityId(clonedServiceTemplate.getUuid());
        }
        if (clonedConfigFile.getEntityType().equals(SERVICE)) {
          if (targetServiceId != null) {
            clonedConfigFile.setEntityId(targetServiceId);
          }
        }
      }
    }
  }

  /**
   * Validates whether service id and mapped service are of same type
   * @param serviceMapping
   */
  private void validateServiceMapping(String appId, String targetAppId, Map<String, String> serviceMapping) {
    if (serviceMapping != null) {
      Set<String> serviceIds = serviceMapping.keySet();
      for (String serviceId : serviceIds) {
        String targetServiceId = serviceMapping.get(serviceId);
        if (serviceId != null && targetServiceId != null) {
          Service oldService = serviceResourceService.get(appId, serviceId, false);
          Validator.notNullCheck("service", oldService);
          Service newService = serviceResourceService.get(targetAppId, targetServiceId, false);
          Validator.notNullCheck("targetService", newService);
          if (oldService.getArtifactType() != null
              && !oldService.getArtifactType().equals(newService.getArtifactType())) {
            throw new WingsException(ErrorCode.INVALID_REQUEST, "message",
                "Target service  [" + oldService.getName() + " ] is not compatible with service ["
                    + newService.getName() + "]");
          }
        }
      }
    }
  }
}
