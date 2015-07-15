package io.rtr.jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.rtr.jsonapi.util.FieldUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ResponseData<T> {

  private static final Logger log = LoggerFactory.getLogger(ResponseData.class);
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
      log.debug("exception while setting Id attribute to null: {}", e);
    }
  }

  @JsonIgnore
  public Boolean isEmpty() {
    List<Field> fields = new ArrayList<>();
    FieldUtil.getAllFields(fields, attributes.getClass());
    for(Field field : fields) {
      field.setAccessible(true);
      try {
        if (field.get(attributes) != null) {
          return false;
        }
      }
      catch (IllegalAccessException e) {
        //we set access to true, so this shouldn't be possible to hit
        log.warn("Hit exception: {}", e);
      }
    }
    return true;
  }


}
