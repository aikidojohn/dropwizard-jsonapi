package io.rtr.jsonapi.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.rtr.jsonapi.ResponseData;

import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class IncludesResourceObjectImpl<T> {
	@JsonUnwrapped
	private ResponseData<T> data;
	private String type;
	
	private Object meta;
	private List<Object> included;
	private Map<String, Object> links;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ResponseData<T> getData() {
		return data;
	}
	public void setData(ResponseData<T> data) {
		this.data = data;
	}
	public Object getMeta() {
		return meta;
	}
	public void setMeta(Object meta) {
		this.meta = meta;
	}
	public List<Object> getIncluded() {
		return included;
	}
	public void setIncluded(List<Object> included) {
		this.included = included;
	}
	public Map<String, Object> getLinks() {
		return links;
	}
	public void setLinks(Map<String, Object> links) {
		this.links = links;
	}
}
