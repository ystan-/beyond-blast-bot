package com.symphony.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    @Id
    private long id;
    private String name;
    private String template;
    private String url;
    private long owner;
}
