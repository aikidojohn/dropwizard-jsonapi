package io.rtr.jsonapi;

import io.rtr.jsonapi.annotation.ApiModel;
import io.rtr.jsonapi.impl.ApiDocumentImpl;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JSONAPI {

	public static <D> ApiDocumentBuilder<D> document(ResourceObjectImpl<D> data) {
		return new ApiDocumentBuilder<D>(data);
	}
	
	public static <D> ApiDocumentBuilder<D> document(List<ResourceObjectImpl<D>> data) {
		return new ApiDocumentBuilder<D>(data);
	}
	
	public static <D> ResourceObjectBuilder<D> data(ResponseData<D> data) {
		return new ResourceObjectBuilder<D>(data);
	}
	
	public static class ApiDocumentBuilder<D> {
		private ResourceObjectImpl<D> data;
		private List<ResourceObjectImpl<D>> dataList;
		
		private List<Object> includes = Lists.newLinkedList();
		private Map<String, Object> links = Maps.newHashMap();
		private Object meta;
		
		public ApiDocumentBuilder(ResourceObjectImpl<D> data) {
			this.data = data;
		}
		
		public ApiDocumentBuilder(List<ResourceObjectImpl<D>> data) {
			this.dataList = data;
		}
		
		public ApiDocumentBuilder<D> include(Object include) {
			includes.add(include);
			return this;
		}
		
		public ApiDocumentBuilder<D> link(String key, String link) {
			links.put(key, link);
			return this;
		}
		
		public ApiDocumentBuilder<D> link(String key, JsonLink link) {
			links.put(key, link);
			return this;
		}
		
		
		public ApiDocumentBuilder<D> meta(Object meta) {
			this.meta = meta;
			return this;
		}
		
		public ApiDocument build(UriInfo uriInfo) {
			ApiDocumentImpl doc = new ApiDocumentImpl();
			if (data != null) {
				doc.setData(data);
			} else {
				doc.setData(dataList);
			}
			
			if (!includes.isEmpty()) {
				doc.setIncluded(includes);
			}
			if (!links.isEmpty()) {
				doc.setLinks(links);
			}
			if (meta != null) {
				doc.setMeta(meta);
			}
			return doc;
		}
	}
	
	
	public static class ResourceObjectBuilder<D> {
		private ResponseData<D> data;
		private List<Object> includes = Lists.newLinkedList();
		private Map<String, Object> links = Maps.newHashMap();
		private Object meta;
		
		public ResourceObjectBuilder(ResponseData<D> data) {
			this.data = data;
		}
		
		public ResourceObjectBuilder<D> include(Object include) {
			includes.add(include);
			return this;
		}
		
		public ResourceObjectBuilder<D> link(String key, String link) {
			links.put(key, link);
			return this;
		}
		
		public ResourceObjectBuilder<D> link(String key, JsonLink link) {
			links.put(key, link);
			return this;
		}
		
		public ResourceObjectBuilder<D> meta(Object meta) {
			this.meta = meta;
			return this;
		}
		
		public ResourceObjectImpl<D> build() {
			ResourceObjectImpl<D> doc = new ResourceObjectImpl<D>();
			doc.setData(data);
			doc.setType(getType(data.attributes));
			
			if (!includes.isEmpty()) {
				doc.setIncluded(includes);
			}
			if (!links.isEmpty()) {
				doc.setRelationships(links);
			}
			if (meta != null) {
				doc.setMeta(meta);
			}
			return doc;
		}
		
		private String getType(D data) {
			ApiModel model = data.getClass().getAnnotation(ApiModel.class);
			if (model != null) {
				String type = model.value();
				if ("undefined".equals(type)) {
					type = model.type();
				}
				if ("undefined".equals(type)) {
					return null;
				}
				return type;
			}
			return null;
		}
	}
}
