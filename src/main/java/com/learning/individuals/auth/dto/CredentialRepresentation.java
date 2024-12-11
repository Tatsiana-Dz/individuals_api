package com.learning.individuals.auth.dto;

import lombok.Data;

@Data
public class CredentialRepresentation {

    public static final String SECRET = "secret";
    public static final String PASSWORD = "password";
    public static final String TOTP = "totp";
    public static final String HOTP = "hotp";
    public static final String KERBEROS = "kerberos";

    private String id;
    private String type;
    private String userLabel;
    private Long createdDate;
    private String secretData;
    private String credentialData;
    private Integer priority;

    private String value;

    // only used when updating a credential.  Might set required action
    protected Boolean temporary;
}