package io.rtr.jsonapi;

import java.util.HashMap;

public class JsonAPIRequest {
  public Data data;
  public Data getData() {
    return data;
  }
  public void setData(Data data) {
    this.data = data;
  }
  public class Data {
    String type;
    String id;
    HashMap<String, Object> attributes;
    public String getType() {
      return type;
    }
    public void setType(String type) {
      this.type = type;
    }
    public String getId() {
      return id;
    }
    public void setId(String id) {
      this.id = id;
    }
    public HashMap<String, Object> getAttributes() {
      return attributes;
    }
    public void setAttributes(HashMap<String, Object>  attributes) {
      this.attributes = attributes;
    }
  }
}
