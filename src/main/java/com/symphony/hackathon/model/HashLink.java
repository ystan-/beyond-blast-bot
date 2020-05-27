package com.symphony.hackathon.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Data
@Builder
public class HashLink {
    @Id
    String id;
    String url;
    Instant accessed;
}
