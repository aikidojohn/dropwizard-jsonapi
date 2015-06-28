package io.rtr.dropwizard.jsonapi.sample.resources;

import io.rtr.dropwizard.jsonapi.sample.models.Article;
import io.rtr.dropwizard.jsonapi.sample.models.Person;

import org.glassfish.jersey.linking.InjectLinkNoFollow;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("sample")
@Produces({ "application/vnd.api+json", MediaType.APPLICATION_JSON })
@Consumes({ "application/vnd.api+json", MediaType.APPLICATION_JSON })
public class SampleResource {

  @InjectLinkNoFollow
  @Context
  private UriInfo uriInfo;

  @GET
  @Path("test")
  public Response getTest() {
    /*final Article article = sampleArticle();
    final Person author = sampleAuthor();

    final JsonLink authorLink = new JsonLink(uriInfo.getAbsolutePath().toString() + "/1/links/author", uriInfo.getAbsolutePath().toString()
        + "/1/author", Lists.newArrayList(new Linkage("people", "2")));
    final ResponseData<Person> personData = new ResponseData<>();
    personData.setAttributes(author);
    personData.setType(author.getClass().getTypeName());
    personData.setId(author.getId());
    final ResourceObjectImpl<Person> authorObj = JSONAPI.data(personData).build();
    final ResponseData<Article> articleData = new ResponseData<>();
    articleData.setAttributes(article);
    articleData.setType(author.getClass().getTypeName());
    articleData.setId(author.getId());
    final ResourceObjectImpl<Article> articleObj = JSONAPI.data(articleData).link("author", authorLink).build();

    final List<ResourceObjectImpl<Article>> articles = Lists.newArrayList(articleObj);
    final ApiDocument doc = JSONAPI.document(articles).link("self", uriInfo.getAbsolutePath().toString()).include(authorObj).build(uriInfo);
    return Response.ok(doc).build();*/
    return Response.ok().build();
  }

  private Article sampleArticle() {
    final Article article = new Article();
    article.setId("1");
    article.setTitle("My Amazing Article about JSON API");
    article.setBody("bla bla bla");
    return article;
  }

  private Person sampleAuthor() {
    final Person person = new Person();
    person.setId("2");
    person.setName("John Hite");
    person.setCompany("Rent the Runway");
    return person;
  }

}
