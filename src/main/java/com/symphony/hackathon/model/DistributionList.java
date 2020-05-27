package com.symphony.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import model.UserInfo;
import org.springframework.data.annotation.Id;
import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistributionList {
    @Id
    private long id;
    private List<UserInfo> users;
    private long owner;
    private String name;
}
