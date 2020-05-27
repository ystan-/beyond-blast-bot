package com.symphony.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    @Id
    private long id;
    private String title;
    private String template;
    private long owner;
}
