package software.wings.service.impl.yaml.handler.artifactstream;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;

import software.wings.beans.artifact.JenkinsArtifactStream;
import software.wings.beans.artifact.JenkinsArtifactStreamYaml;
import software.wings.beans.yaml.ChangeContext;

import com.google.inject.Singleton;

/**
 * @author rktummala on 10/09/17
 */
@OwnedBy(CDC)
@Singleton
public class JenkinsArtifactStreamYamlHandler
    extends ArtifactStreamYamlHandler<JenkinsArtifactStreamYaml, JenkinsArtifactStream> {
  @Override
  public JenkinsArtifactStreamYaml toYaml(JenkinsArtifactStream bean, String appId) {
    JenkinsArtifactStreamYaml yaml = JenkinsArtifactStreamYaml.builder().build();
    super.toYaml(yaml, bean);
    yaml.setArtifactPaths(bean.getArtifactPaths());
    yaml.setJobName(bean.getJobname());
    yaml.setMetadataOnly(bean.isMetadataOnly());
    return yaml;
  }

  @Override
  protected JenkinsArtifactStream getNewArtifactStreamObject() {
    return new JenkinsArtifactStream();
  }

  @Override
  protected void toBean(
      JenkinsArtifactStream bean, ChangeContext<JenkinsArtifactStreamYaml> changeContext, String appId) {
    super.toBean(bean, changeContext, appId);
    JenkinsArtifactStreamYaml yaml = changeContext.getYaml();
    bean.setArtifactPaths(yaml.getArtifactPaths());
    bean.setJobname(yaml.getJobName());
    bean.setMetadataOnly(yaml.isMetadataOnly());
  }

  @Override
  public Class getYamlClass() {
    return JenkinsArtifactStreamYaml.class;
  }
}
