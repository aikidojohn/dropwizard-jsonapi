package io.rtr.jsonapi;

import io.rtr.jsonapi.util.FieldUtil;

import java.lang.reflect.Field;

public class ResponseData {
  String type;
  String id;
  Object attributes;

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public Object getAttributes() {
    setIdAttribute(attributes);
    return attributes;
  }

  public void setAttributes(final Object attributes) {
    this.attributes = attributes;
  }

  private void setIdAttribute(final Object obj) {
    try {
      final Field field = FieldUtil.findDeclaredField(obj, "id");
      field.setAccessible(true);
      field.set(obj, null);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

}
