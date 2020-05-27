package com.symphony.hackathon.web;

import com.symphony.hackathon.model.HashLink;
import com.symphony.hackathon.repository.HashLinkRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class WebController {
    private final HashLinkRepository hashLinkRepository;

    public WebController(HashLinkRepository hashLinkRepository) {
        this.hashLinkRepository = hashLinkRepository;
    }

    @GetMapping("/r/{hash}")
    public void redirect(@PathVariable String hash, HttpServletResponse response) {
        HashLink link = hashLinkRepository.findById(hash).orElse(null);
        if (link == null) {
            response.setStatus(400);
            try {
                response.getWriter().write("Invalid link");
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ignore) {}
            return;
        }
        link.setAccessed(Instant.now());
        hashLinkRepository.save(link);
        response.setHeader("Location", link.getUrl());
        response.setStatus(302);
    }
}
