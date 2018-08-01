package com.github.gauravchauhan89.framework.authorisation;

import java.util.Map;

public class RequestObject {
  private Map<String, Object> queryParameter;
  private Map<String, Object> pathParameter;
  private Object requestBody;

  public RequestObject(Map<String, Object> queryParameter,
      Map<String, Object> pathParameter, Object requestBody) {
    this.queryParameter = queryParameter;
    this.pathParameter = pathParameter;
    this.requestBody = requestBody;
  }

  public Map<String, Object> getQueryParameter() {
    return queryParameter;
  }

  public Map<String, Object> getPathParameter() {
    return pathParameter;
  }

  public Object getRequestBody() {
    return requestBody;
  }

  @Override
  public String toString() {
    return "RequestObject{" +
        "queryParameter=" + queryParameter +
        ", pathParameter=" + pathParameter +
        ", requestBody=" + requestBody +
        '}';
  }
}