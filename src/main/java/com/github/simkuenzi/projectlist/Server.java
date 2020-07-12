package com.github.simkuenzi.projectlist;

import io.javalin.Javalin;
import io.javalin.core.compression.CompressionStrategy;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {

        int port = Integer.parseInt(System.getProperty("com.github.simkuenzi.http.port", "9000"));
        String context = System.getProperty("com.github.simkuenzi.http.context", "/");

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/com/github/simkuenzi/projectlist/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        JavalinThymeleaf.configure(templateEngine);

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("com/github/simkuenzi/projectlist/static/");
            config.contextPath = context;

            // Got those errors on the apache proxy with compression enabled. Related to the Issue below?
            // AH01435: Charset null not supported.  Consider aliasing it?, referer: http://pi/one-egg/
            // AH01436: No usable charset information; using configuration default, referer: http://pi/one-egg/
            config.compressionStrategy(CompressionStrategy.NONE);
        })

                // Workaround for https://github.com/tipsy/javalin/issues/1016
                // Aside from mangled up characters the wrong encoding caused apache proxy to fail on style.css.
                // Apache error log: AH01385: Zlib error -2 flushing zlib output buffer ((null))
                .before(ctx -> {
                    if (ctx.res.getCharacterEncoding().equals("utf-8")) {
                        ctx.res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    }
                })

                .start(port);

        app.get("/", ctx -> ctx.render("home.html", model()));
    }

    private static Map<String, Object> model() throws IOException {
        Map<String, Object> vars = new HashMap<>();
        Properties versionProps = new Properties();
        versionProps.load(com.github.simkuenzi.projectlist.Server.class.getResourceAsStream("version.properties"));
        vars.put("version", versionProps.getProperty("version"));

        ArrayList<Object> projects = new ArrayList<>();
        vars.put("projects", projects);

        Path pathFile = Path.of(System.getProperty("user.home"), "projectlist");
        if (Files.exists(pathFile)) {
            List<String> lines = Files.readAllLines(pathFile);
            Path projectsPath = Path.of(lines.size() > 0 ? lines.get(0) : "");
            if (Files.exists(projectsPath)) {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(projectsPath)) {
                    paths.forEach(p -> {
                        if (!p.getFileName().toString().equals("projectlist")) {
                            projects.add(new Project(p.getFileName().toString()));
                        }
                    });
                }
            } else {
                log.log(Level.WARNING, String.format("Path '%s' configured in %s does not exist.", projectsPath, pathFile));
            }
        }
        return vars;
    }
}
