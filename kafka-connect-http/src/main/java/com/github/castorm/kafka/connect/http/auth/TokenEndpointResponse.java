package com.github.castorm.kafka.connect.http.auth;

/* This POJO should be removed and replaced with an anonymous type or some string-based regex wizardry, so that the extension can be generic */

public class TokenEndpointResponse {
    public String accessToken;
    public int expiresIn;

}
