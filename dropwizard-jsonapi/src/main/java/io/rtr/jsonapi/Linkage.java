package io.rtr.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Linkage {
  private final String type;
  private final String id;

  @JsonCreator
  public Linkage(@JsonProperty("type") final String type, @JsonProperty("id") final String id) {
    super();
    this.type = type;
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }
}
