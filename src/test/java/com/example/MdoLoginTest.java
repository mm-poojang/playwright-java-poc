package com.example;

import com.example.pages.ProsLoginPage;
import com.example.support.PageNavigation;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class MdoLoginTest {

    /** Deep link redirects unauthenticated users to the pros-login organization step. */
    private static final String MDO_ENTRY_URL =
            "https://lt.masterdataonline.com/ui/en/index.html#/home/flows/_all";

    private static final String MDO_ORG_ID = "AutoQa";
    private static final String MDO_USERNAME = "automate_approver_1_qar@prospecta.com";

    // env password. TODO -
    private static String mdoLoginPassword() {
        String fromEnv = System.getenv("MDO_LOGIN_PASSWORD");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProp = System.getProperty("mdo.login.password");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp;
        }
        return "";
    }

    @Test
    void submitsOrganizationUsernameAndChoosesPasswordLogin(Page page) {
        String password = mdoLoginPassword();
        Assertions.assertFalse(
                password.isBlank(),
                """
                See README.md (MDO login test credentials).""");

        PageNavigation.visit(page, MDO_ENTRY_URL);

        ProsLoginPage login = new ProsLoginPage(page);
        login.waitForLoaded();

        login.fillOrganizationId(MDO_ORG_ID);
        Assertions.assertEquals(
                MDO_ORG_ID,
                login.organizationIdInput().inputValue(),
                "Organization id field should contain the value sent by the test");

        login.clickContinue();
        login.expectUsernameStepVisible();

        login.fillUsername(MDO_USERNAME);
        Assertions.assertEquals(
                MDO_USERNAME,
                login.usernameInput().inputValue(),
                "Username field should contain the value sent by the test");

        login.clickContinue();
        login.clickUsePassword();

        login.expectPasswordStepVisible();
        login.fillPassword(password);
        Assertions.assertEquals(
                password,
                login.passwordInput().inputValue(),
                "Password field should contain the value sent by the test");

        login.clickLogin();
    }
}