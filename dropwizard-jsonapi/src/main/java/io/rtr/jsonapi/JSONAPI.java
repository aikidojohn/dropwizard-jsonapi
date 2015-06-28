package io.rtr.jsonapi;

import io.rtr.jsonapi.annotation.ApiModel;
import io.rtr.jsonapi.impl.ApiDocumentImpl;
import io.rtr.jsonapi.impl.IncludesResourceObjectImpl;
import io.rtr.jsonapi.impl.ResourceObjectImpl;
import io.rtr.jsonapi.util.EntityUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

public class JSONAPI {

  public static <D> ApiDocumentBuilder<D> document(final ResourceObjectImpl<D> data) {
    return new ApiDocumentBuilder<D>(data);
  }

  public static <D> ApiDocumentBuilder<D> document(final List<ResourceObjectImpl<D>> data) {
    return new ApiDocumentBuilder<D>(data);
  }

  public static <D> ResourceObjectBuilder<D> data(final ResponseData<D> data) {
    return new ResourceObjectBuilder<D>(data);
  }

  public static <D> IncludesResourceObjectBuilders<D> includesData(final ResponseData<D> data) {
    return new IncludesResourceObjectBuilders<D>(data);
  }

  public static class ApiDocumentBuilder<D> {
    private ResourceObjectImpl<D> data;
    private List<ResourceObjectImpl<D>> dataList;

    private final List<Object> includes = Lists.newLinkedList();
    private final Map<String, Object> links = Maps.newHashMap();
    private Object meta;

    public ApiDocumentBuilder(final ResourceObjectImpl<D> data) {
      this.data = data;
    }

    public ApiDocumentBuilder(final List<ResourceObjectImpl<D>> data) {
      this.dataList = data;
    }

    public ApiDocumentBuilder<D> include(final Object include) {
      includes.add(include);
      return this;
    }

    public ApiDocumentBuilder<D> link(final String key, final String link) {
      links.put(key, link);
      return this;
    }

    public ApiDocumentBuilder<D> link(final String key, final JsonLink link) {
      links.put(key, link);
      return this;
    }

    public ApiDocumentBuilder<D> meta(final Object meta) {
      this.meta = meta;
      return this;
    }

    public ApiDocument build(final UriInfo uriInfo) {
      final ApiDocumentImpl doc = new ApiDocumentImpl();
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

  public static class IncludesResourceObjectBuilders<D> {
    private final ResponseData<D> data;
    private final List<Object> includes = Lists.newLinkedList();
    private final Map<String, Object> links = Maps.newHashMap();
    private Object meta;

    public IncludesResourceObjectBuilders(final ResponseData<D> data) {
      this.data = data;
    }

    public IncludesResourceObjectBuilders<D> include(final Object include) {
      includes.add(include);
      return this;
    }

    public IncludesResourceObjectBuilders<D> link(final String key, final String link) {
      links.put(key, link);
      return this;
    }

    public IncludesResourceObjectBuilders<D> link(final String key, final JsonLink link) {
      links.put(key, link);
      return this;
    }

    public IncludesResourceObjectBuilders<D> meta(final Object meta) {
      this.meta = meta;
      return this;
    }

    public IncludesResourceObjectImpl<D> build() {
      final IncludesResourceObjectImpl<D> doc = new IncludesResourceObjectImpl<D>();
      doc.setData(data);
      doc.setType(EntityUtil.getType(data.attributes));

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
    private final ResponseData<D> data;
    private final List<Object> includes = Lists.newLinkedList();
    private final Map<String, Object> links = Maps.newHashMap();
    private Object meta;

    public ResourceObjectBuilder(final ResponseData<D> data) {
      this.data = data;
    }

    public ResourceObjectBuilder<D> include(final Object include) {
      includes.add(include);
      return this;
    }

    public ResourceObjectBuilder<D> link(final String key, final String link) {
      links.put(key, link);
      return this;
    }

    public ResourceObjectBuilder<D> link(final String key, final JsonLink link) {
      links.put(key, link);
      return this;
    }

    public ResourceObjectBuilder<D> meta(final Object meta) {
      this.meta = meta;
      return this;
    }

    public ResourceObjectImpl<D> build() {
      final ResourceObjectImpl<D> doc = new ResourceObjectImpl<D>();
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

    private String getType(final D data) {
      final ApiModel model = data.getClass().getAnnotation(ApiModel.class);
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
