package com.septangle.momosachiblog.constant;

public class Constants {
    public static final String device = "MomosachiBlog";

    private static final String authorizationKey = device + "-AuthorizationKey";

    private static final String duplicateRequestKey = device + "-DuplicateRequestKey";

    public static String getAuthorizationKey(String key) {
        return authorizationKey + key;
    }
    public static String getDuplicateRequestKey(String key) {
        return duplicateRequestKey + key;
    }

}
