package com.symphony.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import model.UserInfo;
import org.springframework.data.annotation.Id;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistributionList {
    @Id
    private long id;
    private List<UserInfo> users;
}
