package com.github.simkuenzi.projectlist;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Application extends javax.ws.rs.core.Application {
    @Context
    private ServletContext servletContext;

    @Override
    public Set<Object> getSingletons() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return new HashSet<>(Collections.singletonList(
                new Home(templateEngine)));
    }
}
