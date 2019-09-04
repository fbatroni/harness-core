{
  "containerDefinitions": [
    {
      "portMappings": [
        {
          "hostPort": 8080,
          "protocol": "tcp",
          "containerPort": 8080
        }
      ],
      "cpu": 1,
      "environment": [
        {
          "name": "ACCOUNT_ID",
          "value": "${accountId}"
        },
        {
          "name": "ACCOUNT_SECRET",
          "value": "${accountSecret}"
        },
        {
          "name": "DELEGATE_CHECK_LOCATION",
          "value": "${delegateCheckLocation}"
        },
        {
          "name": "DELEGATE_STORAGE_URL",
          "value": "${delegateStorageUrl}"
        },
        {
          "name": "DELEGATE_TYPE",
          "value": "ECS"
        },
        {
          "name": "DELEGATE_GROUP_NAME",
          "value": "${delegateGroupName}"
        },
        {
          "name": "DELEGATE_PROFILE",
          "value": "${delegateProfile}"
        },
        {
          "name": "DEPLOY_MODE",
          "value": "${deployMode}"
        },
        {
          "name": "MANAGER_HOST_AND_PORT",
          "value": "${managerHostAndPort}"
        },
<#if CCM_EVENT_COLLECTION??>
        {
          "name": "PUBLISH_TARGET",
          "value": "${publishTarget}"
        },
        {
          "name": "PUBLISH_AUTHORITY",
          "value": "${publishAuthority}"
        },
        {
          "name": "QUEUE_FILE_PATH",
          "value": "${queueFilePath}"
        },
</#if>
        {
          "name": "POLL_FOR_TASKS",
          "value": "false"
        },
        {
          "name": "WATCHER_CHECK_LOCATION",
          "value": "${watcherCheckLocation}"
        },
        {
          "name": "WATCHER_STORAGE_URL",
          "value": "${watcherStorageUrl}"
        }
      ],
      "memory": 6144,
      "image": "${delegateDockerImage}",
      "essential": true,
      ${hostnameForDelegate}
      "name": "ecs-delegate"
    }
  ],
  "memory": "6144",
  "requiresCompatibilities": [
    "EC2"
  ],
  ${networkModeForTask}
  "cpu": "1024",
  "family": "harness-delegate-task-spec"
}