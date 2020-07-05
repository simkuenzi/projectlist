package com.github.simkuenzi.projectlist;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Path("/")
public class Home {
    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;
    @Context
    private UriInfo uriInfo;

    private final TemplateEngine templateEngine;

    public Home(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        ArrayList<Object> projects = new ArrayList<>();
        vars.put("projects", projects);
        try (DirectoryStream<java.nio.file.Path> paths = Files.newDirectoryStream(Paths.get(".").toAbsolutePath()
                .getParent().getParent().getParent())) {
            paths.forEach(p -> {
                if (!p.equals(Paths.get(".").toAbsolutePath().getParent().getParent())) {
                    projects.add(new Project(p.getFileName().toString()));
                }
            });
        }
        return render(vars);
    }

    protected Response render(Map<String, Object> vars) throws Exception {
        Properties versionProps = new Properties();
        versionProps.load(Home.class.getResourceAsStream("version.properties"));
        vars.put("version", versionProps.getProperty("version"));
        WebContext context = new WebContext(servletRequest, servletResponse, servletContext, servletRequest.getLocale(), vars);
        return Response.ok(templateEngine.process("home", context), MediaType.TEXT_HTML_TYPE.withCharset("utf-8")).build();
    }
}
