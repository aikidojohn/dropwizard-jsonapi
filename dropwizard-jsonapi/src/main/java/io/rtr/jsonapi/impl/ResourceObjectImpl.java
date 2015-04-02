package io.rtr.jsonapi.impl;

import io.rtr.jsonapi.ResourceObject;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceObjectImpl<T> {
	@JsonUnwrapped
	private T data;
	
	private Object meta;
	private List<ResourceObject<?>> included;
	private Map<String, Object> links;
	
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public Object getMeta() {
		return meta;
	}
	public void setMeta(Object meta) {
		this.meta = meta;
	}
	public List<ResourceObject<?>> getIncluded() {
		return included;
	}
	public void setIncluded(List<ResourceObject<?>> included) {
		this.included = included;
	}
	public Map<String, Object> getLinks() {
		return links;
	}
	public void setLinks(Map<String, Object> links) {
		this.links = links;
	}
	
	

}
