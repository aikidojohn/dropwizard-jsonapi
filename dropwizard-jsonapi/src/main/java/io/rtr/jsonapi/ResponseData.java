package io.rtr.jsonapi;

import io.rtr.jsonapi.util.FieldUtil;

import java.lang.reflect.Field;

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
    setId(attributes);
    return attributes;
  }
  public void setAttributes(T attributes) {
    this.attributes = attributes;
  }
  private void setId(Object obj) {
    try {
      Field field = FieldUtil.findDeclaredField(obj, "id");
      field.setAccessible(true);
      field.set(obj, null);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }


}
