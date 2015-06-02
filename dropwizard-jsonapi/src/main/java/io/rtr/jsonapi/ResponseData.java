package io.rtr.jsonapi;

public class ResponseData<T> {
  String type;
  String id;
  T attributes;
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
  public T getAttributes() {
    return attributes;
  }
  public void setAttributes(T attributes) {
    this.attributes = attributes;
  }
}
