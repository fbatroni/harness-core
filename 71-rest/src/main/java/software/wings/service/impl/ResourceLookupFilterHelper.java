package software.wings.service.impl;

import static io.harness.beans.PageRequest.PageRequestBuilder.aPageRequest;
import static io.harness.beans.SearchFilter.Operator.AND;
import static io.harness.beans.SearchFilter.Operator.ELEMENT_MATCH;
import static io.harness.beans.SearchFilter.Operator.EQ;
import static io.harness.beans.SearchFilter.Operator.IN;
import static io.harness.beans.SearchFilter.Operator.OR;
import static io.harness.data.structure.EmptyPredicate.isEmpty;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.exception.WingsException.USER;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.inject.Singleton;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.harness.beans.PageRequest;
import io.harness.beans.SearchFilter;
import io.harness.beans.SearchFilter.Operator;
import io.harness.eraro.ErrorCode;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.HarnessTagFilter;
import software.wings.beans.HarnessTagFilter.TagFilterCondition;
import software.wings.beans.ResourceLookup;
import software.wings.beans.ResourceLookup.ResourceLookupKeys;
import software.wings.beans.ResourceLookupFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class ResourceLookupFilterHelper {
  private static final String NAME = "name";
  private static final String VALUE = "value";
  private static final String QUERY_KEY = "query";
  private static final String TAGS = "tags";
  private static final String TAGS_NAME = "tags.name";

  public void addResourceLookupFiltersToPageRequest(PageRequest<ResourceLookup> pageRequest, String filter) {
    ResourceLookupFilter resourceLookupFilter = convertToResourceLookupFilter(filter);
    if (resourceLookupFilter == null) {
      return;
    }

    List<String> appIds = resourceLookupFilter.getAppIds();
    if (isNotEmpty(appIds)) {
      pageRequest.addFilter(ResourceLookupKeys.appId, IN, appIds.toArray());
    }

    List<String> resourceTypes = resourceLookupFilter.getResourceTypes();
    if (isNotEmpty(resourceTypes)) {
      pageRequest.addFilter(ResourceLookupKeys.resourceType, IN, resourceTypes.toArray());
    }

    HarnessTagFilter harnessTagFilter = resourceLookupFilter.getHarnessTagFilter();
    if (harnessTagFilter == null || isEmpty(harnessTagFilter.getConditions())) {
      return;
    }

    List<SearchFilter> searchFilters = new ArrayList<>();
    for (TagFilterCondition tagFilterCondition : harnessTagFilter.getConditions()) {
      addSearchFilterFromTagFilterCondition(tagFilterCondition, searchFilters);
    }

    pageRequest.addFilter(
        prepareSearchFilter(QUERY_KEY, harnessTagFilter.isMatchAll() ? AND : OR, searchFilters.toArray()));
  }

  private void addSearchFilterFromTagFilterCondition(
      TagFilterCondition tagFilterCondition, List<SearchFilter> searchFilters) {
    Operator operator = tagFilterCondition.getOperator();

    if (operator == null) {
      throw new InvalidRequestException("Operator cannot be empty in tagFilterCondition");
    }

    switch (operator) {
      case IN:
        SearchFilter nameSearchFilter = prepareSearchFilter(NAME, EQ, new Object[] {tagFilterCondition.getName()});
        SearchFilter valueSearchFilter = prepareSearchFilter(VALUE, IN, tagFilterCondition.getValues().toArray());
        PageRequest pageRequest = aPageRequest().build();
        pageRequest.addFilter(nameSearchFilter);
        pageRequest.addFilter(valueSearchFilter);

        searchFilters.add(prepareSearchFilter(TAGS, ELEMENT_MATCH, new Object[] {pageRequest}));
        break;

      case EXISTS:
        SearchFilter existsSearchFilter =
            prepareSearchFilter(TAGS_NAME, EQ, new Object[] {tagFilterCondition.getName()});
        searchFilters.add(prepareSearchFilter(QUERY_KEY, AND, new Object[] {existsSearchFilter}));
        break;

      default:
        throw new InvalidRequestException("Unhandled operator type: " + operator);
    }
  }

  private SearchFilter prepareSearchFilter(String fieldName, Operator op, Object[] fieldValues) {
    return SearchFilter.builder().fieldName(fieldName).op(op).fieldValues(fieldValues).build();
  }

  private ResourceLookupFilter convertToResourceLookupFilter(String filter) {
    if (isBlank(filter)) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      return mapper.readValue(filter, ResourceLookupFilter.class);
    } catch (IOException e) {
      logger.info("Exception " + e);
      String errorMsg = "Failed to deserialize json into ResourceLookupFilter";
      throw new WingsException(ErrorCode.GENERAL_ERROR, errorMsg, USER).addParam("message", errorMsg);
    }
  }
}
