package com.learning.individuals.auth.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserRepresentation {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
    private Long createdTimestamp;
    private Boolean enabled;
    private List<String> groups;
    private List<CredentialRepresentation> credentials;

}

