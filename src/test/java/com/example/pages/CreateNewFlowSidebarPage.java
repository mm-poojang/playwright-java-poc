package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Side panel opened from {@link FlowsPage#clickCreateNewFlow()}: project name, description, Save.
 */
public final class CreateNewFlowSidebarPage {

    private final Page page;

    public CreateNewFlowSidebarPage(Page page) {
        this.page = page;
    }

    public void expectOpen() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(60_000);
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Create new flow"))
                .waitFor(wait);
        projectNameInput().waitFor(wait);
        projectDescriptionInput().waitFor(wait);
        saveButton().waitFor(wait);
    }

    public Locator projectNameInput() {
        return page.locator("lib-input[formcontrolname='projectName'] input");
    }

    public Locator projectDescriptionInput() {
        return page.locator("lib-textarea[formcontrolname='projectDescription'] textarea");
    }

    /** Save in the create-flow header ({@code .breadcrum-head}), not other Save actions on the app. */
    public Locator saveButton() {
        return page.locator(".breadcrum-head")
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save"));
    }

    public void fillProjectName(String value) {
        projectNameInput().fill(value);
    }

    public void fillProjectDescription(String value) {
        projectDescriptionInput().fill(value);
    }

    public void clickSave() {
        saveButton().click();
    }
}
