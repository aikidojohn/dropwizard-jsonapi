package io.rtr.dropwizard.jsonapi.sample.resources;

import io.rtr.jsonapi.ApiDocument;
import io.rtr.jsonapi.JSONAPI;
import io.rtr.jsonapi.Link;
import io.rtr.jsonapi.Linkage;
import io.rtr.jsonapi.impl.ResourceObjectImpl;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

@Path("articles")
@Produces("application/vnd.api+json")
@Consumes("application/vnd.api+json")
public class SampleResource {
	
	@Context
	private UriInfo uriInfo;
	
	@GET
	public Response getArticles() {
		Article article = sampleArticle();
		Person author = sampleAuthor();

		Link<Void> authorLink = new Link<Void>(uriInfo.getAbsolutePath().toString() + "/1/links/author", 
				uriInfo.getAbsolutePath().toString() + "/1/author", 
				Lists.newArrayList(new Linkage("people", "2")));

		ResourceObjectImpl<Person> authorObj = JSONAPI.data(author).build();
		ResourceObjectImpl<Article> articleObj = JSONAPI.data(article).link("author", authorLink).build();
		
		List<ResourceObjectImpl<Article>> articles = Lists.newArrayList(articleObj);
		ApiDocument doc = JSONAPI.document(articles).link("self", uriInfo.getAbsolutePath().toString()).include(authorObj).build();
		return Response.ok(doc).build();
	}
	
	@GET
	@Path("test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTest() {
		System.out.println("Hello test");
		return Response.ok().build();
	}

	private Article sampleArticle() {
		Article article = new Article();
		article.setId("1");
		article.setTitle("My Amazing Article about JSON API");
		article.setBody("bla bla bla");
		return article;
	}
	
	private Person sampleAuthor() {
		Person person = new Person();
		person.setId("2");
		person.setName("John Hite");
		person.setCompany("Rent the Runway");
		return person;
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class Article {
		private String id;
		private String type = "articles";
		private String title;
		private String body;
		
		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class Person {
		private String id;
		private String type = "people";
		private String name;
		private String company;
		
		public String getId() {
			return id;
		}

		public String getType() {
			return type;
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

		public void setType(String type) {
			this.type = type;
		}
	}
		
}
