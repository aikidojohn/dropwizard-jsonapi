package io.rtr.jsonapi;

import java.util.List;

public class Error {

	private String id;
	private String href;
	private String status;
	private String code;
	private String title;
	private String detail;
	private List<String> links;
	private List<String> paths;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public List<String> getLinks() {
		return links;
	}
	public void setLinks(List<String> links) {
		this.links = links;
	}
	public List<String> getPaths() {
		return paths;
	}
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
}
