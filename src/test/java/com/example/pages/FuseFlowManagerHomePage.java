package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.regex.Pattern;

/**
 * Fuse Flow Manager home ({@code HomePerspective}) — Design / Deploy / Manage / Track cards.
 *
 * @see <a href="https://qar.masterdataonline.com/business-central/kie-wb.jsp#HomePerspective%7Corg.kie.workbench.common.screens.home.client.HomePresenter">Fuse Flow Manager home</a>
 */
public final class FuseFlowManagerHomePage {

    private static final int LOAD_TIMEOUT_MS = 120_000;

    /**
     * {@code kie-wb.jsp#HomePerspective|org.kie.workbench.common.screens.home.client.HomePresenter}
     * ({@code %7C} is URL-encoded {@code |}).
     */
    public static final Pattern HOME_URL = Pattern.compile(
            ".*kie-wb\\.jsp#HomePerspective(?:%7C|\\|)org\\.kie\\.workbench\\.common\\.screens\\.home\\.client\\.HomePresenter.*");

    private final Page page;

    public FuseFlowManagerHomePage(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }

    /** Waits for the Home perspective URL and the Design hero card. */
    public void expectLoaded() {
        expectOnHomeScreen();
    }

    public void expectOnHomeScreen() {
        page.waitForURL(HOME_URL, new Page.WaitForURLOptions().setTimeout(LOAD_TIMEOUT_MS));
        expectDesignCardVisible();
    }

    /**
     * Design hero card — {@code div.kie-hero-card#home-action-design[role='button']}.
     */
    public Locator designCard() {
        return page.locator(
                "#home-action-design.kie-hero-card[role='button'][data-field='card']");
    }

    public void expectDesignCardVisible() {
        designCard().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public void expectDesignCardHeading() {
        designCard()
                .locator("h2[data-field='heading']")
                .filter(new Locator.FilterOptions().setHasText("Design"))
                .waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public void clickDesignCard() {
        Locator card = designCard();
        System.out.println("Looking for clickDesignCard: " + card.toString());
        page.waitForTimeout(4000);
        card.click();
    }

    /** Home screen ready → click Design ({@code #home-action-design}). */
    public void selectDesign() {
        // expectOnHomeScreen();
        expectDesignCardHeading();
        clickDesignCard();
    }

    // /** Select Design, then wait for Library Add Assets and the Add Asset button. */
    // public FuseFlowManagerAddAssetsPage openAddAssetsScreen() {
    //     selectDesign();
    //     FuseFlowManagerAddAssetsPage addAssets = new FuseFlowManagerAddAssetsPage(page);
    //     addAssets.expectLoaded();
    //     return addAssets;
    // }
}
