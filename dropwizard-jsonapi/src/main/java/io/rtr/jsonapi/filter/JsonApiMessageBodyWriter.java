package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.annotation.FieldFilterMixIn;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

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
		mapper.addMixInAnnotations(Object.class, FieldFilterMixIn.class);
		return mapper;
	}
	
	
	/*@Override
	protected JsonEndpointConfig _configForWriting(ObjectMapper mapper,
			Annotation[] annotations, Class<?> defaultView) {
		System.out.println("!!!!!My Writer!!!!!!!");
		mapper.addMixInAnnotations(Object.class, FieldFilterMixIn.class);
		ObjectWriter w = mapper.writer();
		
		return _configForWriting(w, annotations);
	}

	@Override
    protected JsonEndpointConfig _configForWriting(ObjectWriter writer,
        Annotation[] annotations) {
        return JsonEndpointConfig.forWriting(writer, annotations, _jsonpFunctionName);
    }*/
	

	@Override
    protected boolean hasMatchingMediaType(MediaType mediaType)
    {
        if (mediaType != null) {
            JSONAPI_MEDIATYPE.equals(mediaType);
        }
        /* Not sure if this can happen; but it seems reasonable
         * that we can at least produce JSON without media type?
         */
        return true;
    }
	

}
