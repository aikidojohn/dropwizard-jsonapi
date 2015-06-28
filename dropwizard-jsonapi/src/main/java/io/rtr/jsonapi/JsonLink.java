package io.rtr.jsonapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public class JsonLink {

  private final String self;
  private final String related;
  private final List<Linkage> linkage;
  private final Object meta;

  @JsonCreator
  public JsonLink(@JsonProperty("self") final String self, @JsonProperty("related") final String related,
      @JsonProperty("linkage") final List<Linkage> linkage, @JsonProperty("meta") final Object meta) {
    super();
    this.self = self;
    this.related = related;
    this.linkage = linkage;
    this.meta = meta;
  }

  public JsonLink(final String self, final String related, final List<Linkage> linkage) {
    this(self, related, linkage, null);
  }

  public String getSelf() {
    return self;
  }

  public String getRelated() {
    return related;
  }

  public List<Linkage> getLinkage() {
    return linkage;
  }

  public Object getMeta() {
    return meta;
  }
}
