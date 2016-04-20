package software.wings.core.ssh.executors;

import static software.wings.common.UUIDGenerator.getUUID;

import org.junit.Test;

/**
 * Created by anubhaw on 2/5/16.
 */
public class SSHSudoExecutorTest {
  @Test
  public void testExecute() throws Exception {
    SSHSessionConfig config = new SSHSessionConfig.SSHSessionConfigBuilder()
                                  .executionID(getUUID())
                                  .host("localhost")
                                  .port(2222)
                                  .user("osboxes")
                                  .password("osboxes.org")
                                  .sudoUserName("vagrant")
                                  .sudoUserPassword("osboxes.org")
                                  .build();

    SSHExecutor executor = SSHExecutorFactory.getExecutor(config);
    executor.execute("sudo su - vagrant  -c 'ls && whoami'");
  }
}
