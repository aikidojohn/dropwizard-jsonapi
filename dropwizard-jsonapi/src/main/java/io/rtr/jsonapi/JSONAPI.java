package io.rtr.jsonapi;

import io.rtr.jsonapi.impl.ApiDocumentImpl;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JSONAPI {

	public static <D> ApiDocumentBuilder<D> document(D data) {
		return new ApiDocumentBuilder<D>(data);
	}
	
	public static <D> ResourceObjectBuilder<D> data(D data) {
		return new ResourceObjectBuilder<D>(data);
	}
	
	public static class ResourceLinkBuilder<D> {
		private D linkObject;
		private String self;
		private String id;
		private String type;
		
		public ResourceLinkBuilder(D object) {
			this.linkObject = object;
			populateIdAndType();
		}
		
		public String build(UriInfo uriInfo) {
			this.self = uriInfo.getBaseUri().resolve(type + "/"  + id).toString();
			return  self;
		}
		
		private void populateIdAndType() {
			Field idField = null;
			Field typeField = null; 
			try {
				idField = linkObject.getClass().getDeclaredField("id");
			} catch (NoSuchFieldException | SecurityException e) {
			}
			
			try {
				typeField = linkObject.getClass().getDeclaredField("type");
			} catch (NoSuchFieldException | SecurityException e) {
			}
			
			if (idField == null || idField.getType() != String.class) {
				throw new IllegalArgumentException("Link object missing a String id field");
			}
			if (typeField == null || typeField.getType() != String.class) {
				throw new IllegalArgumentException("Link object missing a String type field");
			}
			idField.setAccessible(true);
			typeField.setAccessible(true);
			try {
				this.id = (String)idField.get(linkObject);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to read id field of link object");
			}
			try {
				this.type = (String)typeField.get(linkObject);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to read id field of link object");
			}
		}
	}
	
	public static class RelationshipLinkBuilder<D> {
		private D linkObject;
		private String self;
		private String related;
		private Linkage firstLinkage;
		private List<Linkage> restLinkage;
		private String id;
		private String type;
		private String property;
		private String dataType;
		
		public RelationshipLinkBuilder(D object, String dataType, String property) {
			this.linkObject = object;
			this.property = property;
			populateIdAndType();
		}
		
		public RelationshipLinkBuilder<D> withSelf() {
			this.self = type + "/"  + id + "/links/" + property;
			return this;
		}
		
		public RelationshipLinkBuilder<D> withRelated() {
			this.self = type + "/"  + id + "/" + property;
			return this;
		}
		
		private void populateIdAndType() {
			Field idField = null;
			Field typeField = null; 
			try {
				idField = linkObject.getClass().getDeclaredField("id");
			} catch (NoSuchFieldException | SecurityException e) {
			}
			
			try {
				typeField = linkObject.getClass().getDeclaredField("type");
			} catch (NoSuchFieldException | SecurityException e) {
			}
			
			if (idField == null || idField.getType() != String.class) {
				throw new IllegalArgumentException("Link object missing a String id field");
			}
			if (typeField == null || typeField.getType() != String.class) {
				throw new IllegalArgumentException("Link object missing a String type field");
			}
			idField.setAccessible(true);
			typeField.setAccessible(true);
			try {
				this.id = (String)idField.get(linkObject);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to read id field of link object");
			}
			try {
				this.type = (String)typeField.get(linkObject);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to read id field of link object");
			}
		}
	}
	public static class ApiDocumentBuilder<D> {
		private D data;
		private List<Object> includes = Lists.newLinkedList();
		private Map<String, Object> links = Maps.newHashMap();
		private Object meta;
		
		public ApiDocumentBuilder(D data) {
			this.data = data;
		}
		
		public ApiDocumentBuilder<D> include(Object include) {
			includes.add(include);
			return this;
		}
		
		public ApiDocumentBuilder<D> link(String key, String link) {
			links.put(key, link);
			return this;
		}
		
		public ApiDocumentBuilder<D> link(String key, Link<?> link) {
			links.put(key, link);
			return this;
		}
		
		
		public ApiDocumentBuilder<D> meta(Object meta) {
			this.meta = meta;
			return this;
		}
		
		public ApiDocument<D, ?> build() {
			ApiDocumentImpl<D, Object> doc = new ApiDocumentImpl<D, Object>();
			doc.setData(data);
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
		private D data;
		private List<ResourceObject<?>> includes = Lists.newLinkedList();
		private Map<String, Object> links = Maps.newHashMap();
		private Object meta;
		
		public ResourceObjectBuilder(D data) {
			this.data = data;
		}
		
		public ResourceObjectBuilder<D> include(ResourceObject<?> include) {
			includes.add(include);
			return this;
		}
		
		public ResourceObjectBuilder<D> link(String key, String link) {
			links.put(key, link);
			return this;
		}
		
		public ResourceObjectBuilder<D> link(String key, Link<?> link) {
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
}
