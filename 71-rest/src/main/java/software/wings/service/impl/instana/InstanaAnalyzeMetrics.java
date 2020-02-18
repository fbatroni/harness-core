package software.wings.service.impl.instana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanaAnalyzeMetrics {
  private List<Item> items;
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Item {
    private Map<String, List<List<Number>>> metrics;
    private String name;
    private long timestamp;
  }
}
