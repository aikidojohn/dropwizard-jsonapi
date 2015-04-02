package io.rtr.jsonapi.impl;

import io.rtr.jsonapi.ApiDocument;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiDocumentImpl<D, M> implements ApiDocument<D, M> {
	
	private D data;
	private M meta;
	private List<Object> included;
	private Map<String, Object> links;
	
	@Override
	public D getData() {
		return data;
	}

	@Override
	public M getMeta() {
		return meta;
	}
	
	@Override
	public List<Object> getIncluded() {
		return included;
	}
	
	@Override
	public Map<String, Object> getLinks() {
		return links;
	}

	public void setData(D data) {
		this.data = data;
	}

	public void setMeta(M meta) {
		this.meta = meta;
	}

	public void setIncluded(List<Object> included) {
		this.included = included;
	}

	public void setLinks(Map<String, Object> links) {
		this.links = links;
	}
}
