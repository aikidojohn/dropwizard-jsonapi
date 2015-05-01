package io.rtr.jsonapi.filter;

import io.rtr.jsonapi.annotation.ApiResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.HandlerConstructor;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.MethodHandler;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.ResourceModelComponent;
import org.glassfish.jersey.server.model.ResourceModelVisitor;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.uri.PathPattern;
import org.glassfish.jersey.uri.UriTemplate;

import com.google.common.collect.Maps;

public class ResourceMappingContext {
	private ExtendedResourceContext erc;

    private Map<Class<?>, Mapping> mappings;
    private Map<Class<?>, Mapping> mappingsByModel;

    public ResourceMappingContext(@Context ExtendedResourceContext erc) {
        this.erc = erc;
    }

    public Mapping getMapping(Class<?> resource) {
        buildMappings();
        return mappings.get(resource);
    }
    
    public Mapping getMapping(String resourceName) {
    	buildMappings();
    	Class<?> resource = getClassByName(resourceName);
    	if (resource == null) {
    		return null;
    	}
        return mappings.get(resource);
    }
    
    public Mapping getMappingByModel(Class<?> model) {
    	buildMappings();
    	return mappingsByModel.get(model);
    }

    private Class<?> getClassByName(String resourceName) {
    	try {
			return Class.forName(resourceName);
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't find resource class " + resourceName);
			return null;
		}
    }
    
    private void buildMappings() {
        if (mappings != null) {
            return;
        }
        mappings = new HashMap<>();
        mappingsByModel = new HashMap<>();

        erc.getResourceModel().accept(new ResourceModelVisitor() {

            Deque<PathPattern> stack = new LinkedList<>();

            private void processComponents(ResourceModelComponent component) {

                List<? extends ResourceModelComponent> components = component.getComponents();
                if (components != null) {
                    for (ResourceModelComponent rc : components) {
                        rc.accept(this);
                    }
                }
            }

            @Override
            public void visitInvocable(Invocable invocable) {
//            	System.out.println(invocable);
                processComponents(invocable);
            }

            @Override
            public void visitRuntimeResource(RuntimeResource runtimeResource) {
//            	System.out.println(runtimeResource);
                processComponents(runtimeResource);
            }

            @Override
            public void visitResourceModel(ResourceModel resourceModel) {
//            	System.out.println(resourceModel);
                processComponents(resourceModel);
            }

            @Override
            public void visitResourceHandlerConstructor(HandlerConstructor handlerConstructor) {
//            	System.out.println(handlerConstructor);
                processComponents(handlerConstructor);
            }

            @Override
            public void visitMethodHandler(MethodHandler methodHandler) {
//            	System.out.println(methodHandler);
                processComponents(methodHandler);
            }

            @Override
            public void visitChildResource(Resource resource) {
            	System.out.println("Child Resource: " + resource.getPath());
            	System.out.println("Parent: " + resource.getParent());
            	System.out.println("Path: " + resource.getPath());
            	System.out.println("Template Var: " + resource.getPathPattern().getTemplate().getTemplateVariables());
            	UriTemplate template = resource.getPathPattern().getTemplate();
            	if (template.getNumberOfTemplateVariables() == 1) {
            		Class root = getLikelyRoot(resource.getParent());
            		if (root != null) {
            			addMappingPath(root, resource.getPath());
            			for (ResourceMethod method : resource.getAllMethods()) {
            				if ("GET".equals(method.getHttpMethod())) {
            					addMappingMethod(root, resource.getPath(), method.getInvocable().getDefinitionMethod());
            				}
            			}
            		}
            	}
                visitResourceIntl(resource, false);
            }

            @Override
            public void visitResource(Resource resource) {
            	System.out.println("Resource: " + resource.getPath());
                visitResourceIntl(resource, true);
            }

            private Class getLikelyRoot(Resource resource) {
            	Class likelyToBeRoot = null;
                for (Class next : resource.getHandlerClasses()) {
                    if (!(Inflector.class.isAssignableFrom(next))) {
                        likelyToBeRoot = next;
                    }
                }
                return likelyToBeRoot;
            }
            
            private void visitResourceIntl(Resource resource, boolean isRoot) {
                try {
                    stack.addLast(resource.getPathPattern());
                    processComponents(resource);

                    if (isRoot) {
                        Class likelyToBeRoot = getLikelyRoot(resource);

                        if (likelyToBeRoot != null) {
                        	addMappingRoot(likelyToBeRoot, getTemplate());
                        }
                    }
                } finally {
                    stack.removeLast();
                }
            }

            @Override
            public void visitResourceMethod(ResourceMethod resourceMethod) {
            	System.out.println("Resource Method: " + resourceMethod);
                if (resourceMethod.isExtended()) {
                    return;
                }
                System.out.println("Method definition: " + resourceMethod.getInvocable().getDefinitionMethod());

                if (ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR.equals(resourceMethod.getType())) {
                    if (resourceMethod.getInvocable() != null) {
                        Invocable i = resourceMethod.getInvocable();

                        final Type type = i.getResponseType();
                        final String template = getTemplate();

                        addMappingRoot((Class)type, template);

                        // Process sub resources ?

                        Resource.Builder builder = Resource
                                .builder(i.getRawResponseType());
                        if (builder == null) {
                            // for example in the case the return type of the sub resource locator is Object
                            builder = Resource.builder().path(resourceMethod.getParent().getPath());
                        }
                        Resource subResource = builder.build();

                        visitChildResource(subResource);
                    }
                }

                processComponents(resourceMethod);
            }

            private void addMappingRoot(Class type, String rootTemplate) {
            	Mapping mapping = mappings.get(type);
            	if (mapping == null) {
            		mapping = new Mapping();
            		mappings.put(type, mapping);
            	}
            	mapping.setRootPathTemplate(rootTemplate);
            	
            	Class modelClass = getModelClass(type);
            	if (modelClass != null && !mappingsByModel.containsKey(modelClass)) {
            		mappingsByModel.put(modelClass, mapping);
            	}
            }
            
            private void addMappingPath(Class type, String template) {
            	Mapping mapping = mappings.get(type);
            	if (mapping == null) {
            		mapping = new Mapping();
            		mappings.put(type, mapping);
            	}
            	mapping.addPathTemplate(template);
            	
            	Class modelClass = getModelClass(type);
            	if (modelClass != null && !mappingsByModel.containsKey(modelClass)) {
            		mappingsByModel.put(modelClass, mapping);
            	}
            }
            
            private void addMappingMethod(Class type, String template, Method method) {
            	Mapping mapping = mappings.get(type);
            	if (mapping == null) {
            		mapping = new Mapping();
            		mappings.put(type, mapping);
            	}
            	mapping.addPathMethod(template, method);
            	
            	Class modelClass = getModelClass(type);
            	if (modelClass != null && !mappingsByModel.containsKey(modelClass)) {
            		mappingsByModel.put(modelClass, mapping);
            	}
            }
            
            private Class getModelClass(Class<?> resourceClass) {
            	ApiResource resource = resourceClass.getDeclaredAnnotation(ApiResource.class);
            	if (resource == null) {
            		return null;
            	}
            	return resource.model();
            }
            
            private String getTemplate() {
                final StringBuilder template = new StringBuilder();
                for (PathPattern pp : stack) {
                    String ppTemplate = pp.getTemplate().getTemplate();

                    int tlength = template.length();
                    if (tlength > 0) {
                        if (template.charAt(tlength - 1) == '/') {
                            if (ppTemplate.startsWith("/")) {
                                template.append(ppTemplate, 1, ppTemplate.length());
                            } else {
                                template.append(ppTemplate);
                            }
                        } else {
                            if (ppTemplate.startsWith("/")) {
                                template.append(ppTemplate);
                            } else {
                                template.append("/");
                                template.append(ppTemplate);
                            }
                        }
                    } else {
                        template.append(ppTemplate);
                    }

                }
                return template.toString();
            }
        });

    }
    
    
    public static class Mapping {
    	private String rootPath;
    	private final Map<String, String> paths = Maps.newHashMap();
    	private final Map<String, Method> pathMethods = Maps.newHashMap();
    	
    	public Mapping() {
    	}
    	
    	public String getRootPathTemplate() {
    		return rootPath;
    	}
    	
    	public void setRootPathTemplate(String root) {
    		rootPath = root;
    	}
    	
    	public String getPathTemplate(String key) {
    		String path = paths.get(key);
    		if (path == null) {
    			return null;
    		}
    		String seperator = "/";
    		if (path.startsWith("/")) {
    			seperator = "";
    		}
    		return rootPath + seperator + path;
    	}
    	
    	public Method getPathMethod(String key) {
    		return pathMethods.get(key);
    	}
    	
    	public Object getValue(Object resource, String key, Object... args) {
    		final Method accessor = pathMethods.get(key);
    		if (accessor == null) {
    			return null;
    		}
    		
    		try {
				return accessor.invoke(resource, args);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				System.out.println("Failed to invoke resource method. " + e);
			}
    		
    		return null;
    	}
    	
    	public void addPathTemplate(String key, String path) {
    		paths.put(key, path);
    	}
    	
    	public void addPathTemplate(String path) {
    		String key = "self";
    		int index = path.lastIndexOf('/');
    		if (index > 0 && index < path.length() - 1) {
    			key = path.substring(path.lastIndexOf('/') + 1);
    		}
    		paths.put(key, path);
    	}
    	
    	public void addPathMethod(String path, Method method) {
    		String key = "self";
    		int index = path.lastIndexOf('/');
    		if (index > 0 && index < path.length() - 1) {
    			key = path.substring(path.lastIndexOf('/') + 1);
    		}
    		pathMethods.put(key, method);
    	}
    	
    	public Iterable<String> getKeys() {
    		return paths.keySet();
    	}
    	
    	@Override
    	public String toString() {
    		final StringBuilder sb = new StringBuilder();
    		sb.append("{root:");
    		sb.append(rootPath);
    		
    		paths.forEach((k, v) -> {sb.append(", "); sb.append(k); sb.append(":"); sb.append(v);});
    		sb.append("}");
    		return sb.toString();
    	}
    }
}
