package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Login UI ({@code pros-login}): organization id → Continue → username → Continue → "Use password" → password
 * → Login.
 *
 * <p>After each major click ({@link #clickContinue()}, {@link #clickUsePassword()}, {@link #clickLogin()}), the
 * flow pauses for {@value #DEFAULT_STEP_DELAY_MS} ms so the SPA can settle (override with JVM system property
 * {@link #STEP_DELAY_MS_PROPERTY}). Set the property to {@code 0} to disable.
 */
public final class ProsLoginPage {

    /** Default pause between steps (~2–3 s); override with {@value #STEP_DELAY_MS_PROPERTY}. */
    public static final int DEFAULT_STEP_DELAY_MS = 3000;

    /** JVM flag, e.g. {@code -Dplaywright.step.delay.ms=2000} or {@code 0} to disable. */
    public static final String STEP_DELAY_MS_PROPERTY = "playwright.step.delay.ms";

    private final Page page;

    public ProsLoginPage(Page page) {
        this.page = page;
    }

    public void waitForLoaded() {
        Locator.WaitForOptions longWait = new Locator.WaitForOptions().setTimeout(60_000);
        page.locator("pros-login").waitFor(longWait);
        // Material / custom lib-input often does not expose the mat-label via <label for>,
        // so getByLabel("Organization id") can time out even though the field is visible.
        organizationIdInput().waitFor(longWait);
    }

    /** Organization step text field inside {@code pros-login} (Angular formControlName {@code organization}). */
    public Locator organizationIdInput() {
        return page.locator("pros-login lib-input[formcontrolname='organization'] input");
    }

    public void fillOrganizationId(String organizationId) {
        organizationIdInput().fill(organizationId);
    }

    public void clickContinue() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();
        pauseAfterInteractiveStep();
    }

    /** Primary action on the password step (label {@code Login} on {@code lib-button.submit-button}). */
    public void clickLogin() {
        page.locator("pros-login")
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Login"))
                .click();
        pauseAfterInteractiveStep();
    }

    /** Username step field (Angular {@code formControlName} {@code username}). */
    public Locator usernameInput() {
        return page.locator("pros-login lib-input[formcontrolname='username'] input");
    }

     /** username field */
    public void fillUsername(String usernameOrEmail) {
        usernameInput().fill(usernameOrEmail);
    }

    /** Call after organization Continue when the username form is shown. */
    public void expectUsernameStepVisible() {
        Locator.WaitForOptions longWait = new Locator.WaitForOptions().setTimeout(60_000);
        usernameInput().waitFor(longWait);
    }

    public void clickUsePassword() {
        page.locator("pros-login")
                .getByText("Use password", new Locator.GetByTextOptions().setExact(true))
                .click();
        pauseAfterInteractiveStep();
    }

    /** Password field ({@code formControlName} {@code password}, {@code type="password"}). */
    public Locator passwordInput() {
        return page.locator("pros-login lib-input[formcontrolname='password'] input[type='password']");
    }

    public void fillPassword(String password) {
        passwordInput().fill(password);
    }

    /** Call after "Use password" when the password field is shown. */
    public void expectPasswordStepVisible() {
        Locator.WaitForOptions longWait = new Locator.WaitForOptions().setTimeout(60_000);
        passwordInput().waitFor(longWait);
    }

    /**
     * After a successful Continue, the organization step usually leaves the DOM or stops being visible.
     * Prefer {@link #expectUsernameStepVisible()} when the next step is the username form.
     */
    public void expectOrganizationStepNoLongerVisible() {
        organizationIdInput().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(60_000));
    }

    /**
     * Fixed delay after navigation-style clicks so Angular can render the next step.
     * Disabled when {@value #STEP_DELAY_MS_PROPERTY} is {@code 0} or negative.
     */
    private void pauseAfterInteractiveStep() {
        int ms = stepDelayMillis();
        if (ms <= 0) {
            return;
        }
        page.waitForTimeout(ms);
    }

    private static int stepDelayMillis() {
        String raw = System.getProperty(STEP_DELAY_MS_PROPERTY, String.valueOf(DEFAULT_STEP_DELAY_MS));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_STEP_DELAY_MS;
        }
    }
}
