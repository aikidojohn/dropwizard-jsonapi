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
    setIdAttribute(attributes);
    return attributes;
  }
  public void setAttributes(T attributes) {
    this.attributes = attributes;
  }
  private void setIdAttribute(Object obj) {
    try {
      Field field = FieldUtil.findDeclaredField(obj, "id");
      field.setAccessible(true);
      field.set(obj, null);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NullPointerException e) {
      //catch a NPE here if we have a null attributes section on purpose due to no values in the attributes section
      e.printStackTrace();
    }
  }


}
