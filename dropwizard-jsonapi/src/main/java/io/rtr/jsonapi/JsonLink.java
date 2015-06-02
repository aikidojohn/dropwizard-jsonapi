package io.rtr.jsonapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class JsonLink {

	private final String self;
	private final String related;
	private final List<Linkage> linkage;
	private final Object meta;
	
	@JsonCreator
	public JsonLink(@JsonProperty("self") String self, @JsonProperty("related") String related, @JsonProperty("linkage") List<Linkage> linkage,
			@JsonProperty("meta") Object meta) {
		super();
		this.self = self;
		this.related = related;
		this.linkage = linkage;
		this.meta = meta;
	}

	public JsonLink(String self, String related, List<Linkage> linkage) {
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
