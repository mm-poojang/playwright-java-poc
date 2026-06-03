package com.example.support;

import org.junit.jupiter.api.Assertions;

/** Shared MDO UI test URL, account fields, and password resolution (never store password in code). */
public final class MdoTestConfig {

    public static final String ENTRY_URL =
            "https://fuse-int.masterdataonline.com/ngx-auth/en/index.html#/auth/login";

    public static final String ORG_ID = "marcusone";
    public static final String USERNAME = "auto_initiator@yopmail.com";

    private MdoTestConfig() {}

    /**
     * Returns password from {@code MDO_LOGIN_PASSWORD} or {@code -Dmdo.login.password}. Fails the test
     * if missing (see README).
     */
    public static String requirePassword() {
        String fromEnv = System.getenv("MDO_LOGIN_PASSWORD");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProp = System.getProperty("mdo.login.password");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp;
        }
        Assertions.fail(
                """
                Missing MDO login password. See README.md (MDO login test credentials).""");
        return "";
    }
}
