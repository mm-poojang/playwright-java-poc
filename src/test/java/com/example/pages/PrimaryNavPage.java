package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.regex.Pattern;

/**
 * Authenticated shell top navigation ({@code div.nav-root} / {@code mat-nav-list.primary-nav-list}).
 */
public final class PrimaryNavPage {

    private static final Pattern FLOWS_NAV_LABEL = Pattern.compile("^\\s*Flows\\s*$");

    private final Page page;

    public PrimaryNavPage(Page page) {
        this.page = page;
    }

    public Locator navRoot() {
        return page.locator("div.nav-root");
    }

    public Locator primaryNavList() {
        return navRoot().locator("mat-nav-list.primary-nav-list[role='navigation']");
    }

    /**
     * {@code mat-list-item.link-items} for Flows. Material list items often lack a reliable
     * {@code listitem} role/name; label text is {@code " Flows "} inside {@code .mdc-list-item__primary-text}.
     */
    public Locator flowsNavItem() {
        return primaryNavList()
                .locator("mat-list-item.link-items")
                .filter(new Locator.FilterOptions().setHasText(FLOWS_NAV_LABEL));
    }

    /** Visible label span ({@code " Flows "}) inside the nav item. */
    public Locator flowsNavLabel() {
        return primaryNavList()
                .locator("mat-list-item.link-items .mdc-list-item__primary-text")
                .filter(new Locator.FilterOptions().setHasText(FLOWS_NAV_LABEL));
    }

    /** Waits until the post-login top bar and primary nav items are rendered. */
    public void expectLoaded() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(60_000);
        navRoot().waitFor(wait);
        primaryNavList().waitFor(wait);
        page.locator("div.nav-root img.primary-nav-mdo-logo").waitFor(wait);
        flowsNavItem().waitFor(wait);
    }

    public void clickFlows() {
        System.out.println("Flows nav item clicked");
        Locator label = flowsNavLabel();
        label.waitFor();
        label.scrollIntoViewIfNeeded();
        label.click();
    }
}