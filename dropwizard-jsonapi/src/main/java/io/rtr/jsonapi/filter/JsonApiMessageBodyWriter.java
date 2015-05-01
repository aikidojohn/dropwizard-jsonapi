package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.annotation.FieldFilterMixIn;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * This exists only to add a Jackson MixIn to support JSON API field filtering.
 * 
 * @author jhite
 *
 */
@Provider
@Produces("application/vnd.api+json")
public class JsonApiMessageBodyWriter extends JacksonJsonProvider {
	private static final MediaType JSONAPI_MEDIATYPE = MediaType.valueOf("application/vnd.api+json");
	
	@Context
	private UriInfo uriInfo;
	
	@Override
	public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
		final ObjectMapper mapper = super.locateMapper(type, mediaType);
		//Necessary for field filtering
		System.out.println("Adding field filter mixin");
		mapper.addMixInAnnotations(Object.class, FieldFilterMixIn.class);
		return mapper;
	}

	@Override
    protected boolean hasMatchingMediaType(MediaType mediaType)
    {
        if (mediaType != null) {
            JSONAPI_MEDIATYPE.equals(mediaType);
        }
        return false;
    }
	

}
