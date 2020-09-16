package io.harness.cvng.client;

import com.google.inject.Singleton;

import io.harness.eraro.ResponseMessage;
import io.harness.rest.RestResponse;
import io.harness.serializer.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Singleton
@Slf4j
public class RequestExecutor {
  // TODO: enhance it to handle exceptions, stacktraces and retries based on response code and exception from manager.
  public <U> U execute(Call<U> request) {
    try {
      Response<U> response = request.clone().execute();
      if (response.isSuccessful()) {
        return response.body();
      } else {
        int code = response.code();
        String errorBody = response.errorBody().string();
        if (code == 500) {
          tryParsingErrorFromRestResponse(code, errorBody);
        }
        throw new ServiceCallException(response.code(), response.message(), errorBody);
      }
    } catch (IOException e) {
      throw new ServiceCallException(e);
    }
  }

  private void tryParsingErrorFromRestResponse(int code, String errorBody) {
    // Try to parse manager response format - io.harness.cvng.exception.GenericExceptionMapper
    // If resource has annotation @ExposeInternalException(withStackTrace = true) we will also get
    // stacktrace from manager.
    // Add @ExposeInternalException(withStackTrace = true) if you want to get stacktrace of failure.
    RestResponse<?> restResponse;
    try {
      restResponse = (RestResponse<?>) JsonUtils.asObject(errorBody, RestResponse.class);
    } catch (RuntimeException e) {
      // ignore if json can not be parsed to RestResponse
      return;
    }
    List<ResponseMessage> responseMessages = restResponse.getResponseMessages();
    if (responseMessages.size() > 0) {
      throw new ServiceCallException(
          code, responseMessages.get(0).getMessage(), responseMessages.get(0).getException());
    }
  }
}
