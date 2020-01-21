package software.wings.service.impl.yaml.handler.app;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.eraro.ErrorCode;
import io.harness.exception.WingsException;
import software.wings.beans.NameValuePair;
import software.wings.beans.TemplateExpression;
import software.wings.beans.TemplateExpression.Yaml;
import software.wings.beans.yaml.ChangeContext;
import software.wings.beans.yaml.YamlType;
import software.wings.service.impl.yaml.handler.BaseYamlHandler;
import software.wings.service.impl.yaml.handler.NameValuePairYamlHandler;
import software.wings.service.impl.yaml.handler.YamlHandlerFactory;
import software.wings.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author rktummala on 10/28/17
 */
@Singleton
public class AppDefYamlHandler extends BaseYamlHandler<Yaml, TemplateExpression> {
  @Inject YamlHandlerFactory yamlHandlerFactory;

  private TemplateExpression toBean(ChangeContext<Yaml> changeContext) {
    Yaml yaml = changeContext.getYaml();

    Map<String, Object> properties = Maps.newHashMap();
    if (yaml.getMetadata() != null) {
      List<NameValuePair> nameValuePairList =
          yaml.getMetadata()
              .stream()
              .map(nvpYaml -> NameValuePair.builder().name(nvpYaml.getName()).value(nvpYaml.getValue()).build())
              .collect(toList());
      properties = Utils.toProperties(nameValuePairList);
    }

    return TemplateExpression.builder()
        .expression(yaml.getExpression())
        .fieldName(yaml.getFieldName())
        .metadata(properties)
        .build();
  }

  @Override
  public Yaml toYaml(TemplateExpression bean, String appId) {
    NameValuePairYamlHandler nameValuePairYamlHandler = yamlHandlerFactory.getYamlHandler(YamlType.NAME_VALUE_PAIR);
    List<NameValuePair.Yaml> nameValuePairYamlList =
        Utils.toNameValuePairYamlList(bean.getMetadata(), appId, nameValuePairYamlHandler);

    return Yaml.Builder.aYaml()
        .withExpression(bean.getExpression())
        .withFieldName(bean.getFieldName())
        .withMetadata(nameValuePairYamlList)
        .build();
  }

  @Override
  public TemplateExpression upsertFromYaml(ChangeContext<Yaml> changeContext, List<ChangeContext> changeSetContext) {
    return toBean(changeContext);
  }

  @Override
  public Class getYamlClass() {
    return Yaml.class;
  }

  @Override
  public TemplateExpression get(String accountId, String yamlFilePath) {
    throw new WingsException(ErrorCode.UNSUPPORTED_OPERATION_EXCEPTION);
  }

  @Override
  public void delete(ChangeContext<Yaml> changeContext) {
    // Do nothing
  }
}
