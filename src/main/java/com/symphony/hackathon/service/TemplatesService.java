package com.symphony.hackathon.service;

import com.github.jknack.handlebars.Handlebars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class TemplatesService {
    private final Handlebars handlebars;

    public TemplatesService() {
        this.handlebars = new Handlebars();
    }

    public String compile(String templateName, Object object) {
        try {
            Path path = Paths.get(getClass().getResource("/templates/" + templateName + ".hbs").toURI());
            String template = String.join("\n", Files.readAllLines(path));
            return handlebars.compileInline(template).apply(object);
        } catch (URISyntaxException | IOException e) {
            log.error("Unable to compile template", e);
            return null;
        }
    }

    public String load(String templateName) {
        try {
            Path path = Paths.get(getClass().getResource("/templates/" + templateName + ".hbs").toURI());
            return String.join("\n", Files.readAllLines(path));
        } catch (URISyntaxException | IOException e) {
            log.error("Unable to compile template", e);
            return null;
        }
    }
}
