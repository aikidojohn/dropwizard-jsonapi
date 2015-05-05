package io.rtr.dropwizard.jsonapi.sample.models;

import io.rtr.jsonapi.annotation.ApiModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(type = "people", id = "id")
@JsonInclude(Include.NON_NULL)
public class Person {
	private String id;
	private String name;
	private String company;
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setId(String id) {
		this.id = id;
	}
}