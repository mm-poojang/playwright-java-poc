package com.example.support;

import com.example.pages.FlowsPage;
import com.example.pages.PrimaryNavPage;
import com.example.pages.ProsLoginPage;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;

/** Full sign-in through {@code pros-login} until the flows list toolbar is ready. */
public final class MdoSession {

    private MdoSession() {}

    public static FlowsPage signInOpenFlowsAll(Page page) {
        String password = MdoTestConfig.requirePassword();

        PageNavigation.visit(page, MdoTestConfig.ENTRY_URL);

        ProsLoginPage login = new ProsLoginPage(page);
        login.waitForLoaded();

        login.fillOrganizationId(MdoTestConfig.ORG_ID);
        Assertions.assertEquals(
                MdoTestConfig.ORG_ID,
                login.organizationIdInput().inputValue(),
                "Organization id field should contain the value sent by the test");

        login.clickContinue();
        login.expectUsernameStepVisible();

        login.fillUsername(MdoTestConfig.USERNAME);
        Assertions.assertEquals(
                MdoTestConfig.USERNAME,
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
        login.tenantSelection();

        PrimaryNavPage primaryNav = new PrimaryNavPage(page);
        primaryNav.expectLoaded();
        System.out.println("Primary nav loaded");
        page.waitForTimeout(6000);
        primaryNav.clickFlows();
        System.out.println("Flows clicked");
        page.waitForTimeout(4000);


        FlowsPage flows = new FlowsPage(page);
        flows.expectOnFlowsAllPage();
        return flows;
    }
}
