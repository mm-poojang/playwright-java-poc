package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;

/**
 * Authenticated flows list route {@code …#/home/flows/_all} (MDO FUSE).
 */
public final class FlowsPage {

    private static final int LOAD_TIMEOUT_MS = 60_000;
    private static final Pattern CREATE_NEW_FLOW_LABEL = Pattern.compile("^\\s*Create new flow\\s*$");

    /** Hash route for the all-flows view (path/host may differ per environment). */
    public static final Pattern FLOWS_ALL_URL = Pattern.compile(".*#/home/flows/_all.*");

    private final Page page;

    public FlowsPage(Page page) {
        this.page = page;
    }

    /**
     * Waits for the flows route, then for list UI and table data to finish loading (not URL alone).
     */
    public void expectOnFlowsAllPage() {
        page.waitForURL(FLOWS_ALL_URL, new Page.WaitForURLOptions().setTimeout(LOAD_TIMEOUT_MS));
        expectFlowsListLoaded();
    }

    /**
     * Flows page is interactive: toolbar, filters, table visible, and loading skeletons cleared.
     */
    public void expectFlowsListLoaded() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS);

        flowsHeading().waitFor(wait);
        createNewFlowButton().waitFor(wait);
        page.locator(".filter-container").waitFor(wait);
        page.locator(".filter-container lib-search input[placeholder='Search flows']").waitFor(wait);
        statusFilterChip().waitFor(wait);

        flowsTable().waitFor(wait);
        waitForFlowsTableDataReady(wait);
    }

    /** Table finished loading: at least one row, or skeleton/spinner inside the table is gone. */
    private void waitForFlowsTableDataReady(Locator.WaitForOptions wait) {
        Locator rows = flowsTable().locator("tbody tr.mat-mdc-row");
        Locator tableSkeleton = flowsTable().locator("lib-skeleton");

        long deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (rows.count() > 0) {
                rows.first().waitFor(wait);
                return;
            }
            if (tableSkeleton.count() == 0) {
                flowsTable().locator("tbody").waitFor(wait);
                return;
            }
            if (!tableSkeleton.first().isVisible()) {
                return;
            }
            page.waitForTimeout(250);
        }

        rows.first().waitFor(wait);
    }

    /** After login on any page: wait for top nav, click Flows, then assert flows list is ready. */
    public static FlowsPage openFromPrimaryNav(Page page) {
        PrimaryNavPage primaryNav = new PrimaryNavPage(page);
        primaryNav.expectLoaded();
        primaryNav.clickFlows();

        FlowsPage flows = new FlowsPage(page);
        flows.expectOnFlowsAllPage();
        return flows;
    }

    /**
     * Breadcrumb title {@code Flows} ({@code lib-text-line} / {@code h4.leading}).
     */
    public Locator flowsHeading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Flows"));
    }

    /**
     * {@code pros-new-flow-btn} primary action (not the ellipsis {@code mat-mdc-menu-trigger}).
     * Button label in DOM is {@code "Create new flow "} with trailing space.
     */
    public Locator createNewFlowButton() {
        return page.locator("pros-new-flow-btn div.flow-btn lib-button:not(.mat-mdc-menu-trigger) button.mdo-button");
    }

    public Locator createNewFlowButtonLabel() {
        return page.locator("pros-new-flow-btn div.flow-btn lib-button:not(.mat-mdc-menu-trigger) .mdc-button__label")
                .filter(new Locator.FilterOptions().setHasText(CREATE_NEW_FLOW_LABEL));
    }

    public void expectCreateNewFlowButtonVisible() {
        createNewFlowButton().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public void expectFlowsToolbarVisible() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS);
        flowsHeading().waitFor(wait);
        createNewFlowButton().waitFor(wait);
    }

    public void clickCreateNewFlow() {
        Locator button = createNewFlowButton();
        button.waitFor();
        button.scrollIntoViewIfNeeded();
        button.click();
    }

    /**
     * Status filter chip ({@code lib-chip label="Status:"}) in
     * {@code filter-container}; opens Angular
     * {@code mat-menu}.
     */
    public Locator statusFilterChip() {
        return page.locator("lib-chip[label='Status:']");
    }

    /**
     * Opens the chip menu and chooses {@code All} (matches {@code All} or
     * {@code ALL} menuitem labels).
     */
    public void openStatusMenuAndSelectAll(String type) {
        statusFilterChip().locator("button.mdo-filter-pill").click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName(type.toUpperCase()))
                .or(page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName( type.toUpperCase())))
                .click();
    }

    /** Material table listing flows (Flow / Version / Status columns). */
    public Locator flowsTable() {
        return page.locator("table.mat-mdc-table[role='table']");
    }

    /**
     * Flow column cell whose visible text includes the given project / flow name (e.g. after Status = All).
     */
    public Locator flowColumnCellContaining(String flowProjectName) {
        System.out.println("Looking for flowColumnCellContaining: ");
        return flowsTable().locator("tbody tr.mat-mdc-row td.mat-column-flow")
                .filter(new Locator.FilterOptions().setHasText(flowProjectName))
                .first();
    }

    /**
     * Table row whose Flow column contains {@code flowProjectName}.
     */
    public Locator flowRowContaining(String flowProjectName) {
        return flowColumnCellContaining(flowProjectName)
                .locator("xpath=ancestor::tr[contains(@class,'mat-mdc-row')][1]");
    }

    /** Ellipsis / actions {@code lib-button} in the setting column of that row. */
    public Locator rowActionsButtonForFlow(String flowProjectName) {
        return flowRowContaining(flowProjectName)
                .locator("td.mat-column-setting lib-button.mat-mdc-menu-trigger button.mdo-button")
                .first();
    }

    public void expectFlowNamedInTable(String flowProjectName) {
        System.out.println("Looking for expectFlowNamedInTable: ");
        flowColumnCellContaining(flowProjectName).waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
        System.out.println("Looking for expectFlowNamedInTable1: ");
    }

    /** Clicks the row actions menu trigger ({@code fa-ellipsis-h}) for the matching flow. */
    public void clickRowActionsButtonForFlow(String flowProjectName) {
        Locator button = rowActionsButtonForFlow(flowProjectName);
        button.waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
        button.scrollIntoViewIfNeeded();
        button.click();
    }

    /** Row ellipsis menu ({@code mat-mdc-menu-panel.navigation-menu}) in the CDK overlay. */
    public Locator flowRowActionsMenu() {
        return page.locator(".cdk-overlay-container .mat-mdc-menu-panel.navigation-menu");
    }

    public void expectFlowRowActionsMenuVisible() {
        flowRowActionsMenu().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    /** Chooses {@code Edit flow diagram} from the row actions menu (Publish, View Event logs, …). */
    public void clickEditFlowDiagram() {
        expectFlowRowActionsMenuVisible();
        flowRowActionsMenu()
                .getByRole(AriaRole.MENUITEM, new Locator.GetByRoleOptions().setName("Edit flow diagram"))
                .click();
    }

    // /** Clicks the clickable flow title ({@code lib-text-line}) in the matching row. */
    // public void clickFlowNamedInTable(String flowProjectName) {
    //     flowColumnCellContaining(flowProjectName)
    //             .locator("lib-text-line.cursor, lib-text-line")
    //             .first()
    //             .click();
    // }

    /**
     * Clicks {@code Edit flow diagram} and returns the Fuse Flow Manager tab opened by the app.
     */
    public FuseFlowManagerHomePage openFlowDiagramInNewTab() {
        Page flowManagerTab = page.waitForPopup(this::clickEditFlowDiagram);
        FuseFlowManagerHomePage home = new FuseFlowManagerHomePage(flowManagerTab);
        home.expectLoaded();
        return home;
    }
}