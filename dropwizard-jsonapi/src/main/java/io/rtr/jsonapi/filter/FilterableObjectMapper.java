package io.rtr.jsonapi.filter;

import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Produces("application/vnd.api+json")
public class FilterableObjectMapper implements MethodInterceptor {
	
	private ObjectMapper mapper;
	private UriInfo uriInfo;
	
	public static ObjectMapper newInstance(ObjectMapper mapper, UriInfo info) {
		FilterableObjectMapper proxy = new FilterableObjectMapper(mapper, info);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ObjectMapper.class);
		enhancer.setCallback(proxy);
		return (ObjectMapper)enhancer.create();
	}
	
	public FilterableObjectMapper(ObjectMapper mapper, UriInfo uriInfo) {
		this.mapper = mapper;
		this.uriInfo = uriInfo;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (method.getName() == "writer") {
			System.out.println("!!!!!!!My Object Writer!!!!!!");
			if (uriInfo != null) {
				MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
				List<String> fields = params.get("fields");
				System.out.println(fields);
			}
			return mapper.writer();
		}
		return proxy.invokeSuper(obj, args);
	}
	

	/*@Override
	public ObjectWriter writer() {
		System.out.println("!!!!!!!!!!!!!My Object Writer!!!!!!!!");
		return super.writer();
	}*/
}
