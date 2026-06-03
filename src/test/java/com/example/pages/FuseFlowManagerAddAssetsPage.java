package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

/**
 * Fuse Flow Manager library — Add Assets, asset-type picker, and Create Business Process modal.
 */
public final class FuseFlowManagerAddAssetsPage {

    private static final int LOAD_TIMEOUT_MS = 120_000;
    private static final int DEFAULT_POST_OK_DELAY_MS = 3_000;
    private static final Pattern BUSINESS_PROCESS_TITLE = Pattern.compile("^\\s*Business Process\\s*$");
    private static final Pattern OK_BUTTON_LABEL = Pattern.compile("^\\s*Ok\\s*$");

    public static final Pattern ADD_ASSETS_URL =
            Pattern.compile(".*kie-wb\\.jsp#LibraryPerspective.*AddAssetsScreen.*");

    private final Page page;

    public FuseFlowManagerAddAssetsPage(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }

    public void expectLoaded() {
        page.waitForURL(ADD_ASSETS_URL, new Page.WaitForURLOptions().setTimeout(LOAD_TIMEOUT_MS));
        expectAddAssetButtonVisible();
    }

    public void expectAddAssetButtonVisible() {
        addAssetButton().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public Locator addAssetButton() {
        return page.locator("button[data-field='add-asset']");
    }

    public void clickAddAsset() {
        Locator button = addAssetButton();
        button.scrollIntoViewIfNeeded();
        button.click();
    }

    public void expectAssetTypePickerVisible() {
        businessProcessCard().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public void openAssetTypePicker() {
        clickAddAsset();
        expectAssetTypePickerVisible();
    }

    public Locator businessProcessCard() {
        return page.locator("h3.card-pf-title[data-field='title']")
                .filter(new Locator.FilterOptions().setHasText(BUSINESS_PROCESS_TITLE))
                .locator("xpath=ancestor::div[contains(@class,'card-pf-view-select')][1]");
    }

    public void expectBusinessProcessCardVisible() {
        businessProcessCard().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public void selectBusinessProcess() {
        Locator card = businessProcessCard();
        card.scrollIntoViewIfNeeded();
        card.click();
    }

    /**
     * Visible Bootstrap modal — {@code Create new Business Process}.
     * Uses {@code .modal.in} / {@code .modal.show} so a hidden template is not matched.
     */
    public Locator createBusinessProcessModal() {
        return page.locator(".modal.in .modal-content, .modal.show .modal-content")
                .filter(new Locator.FilterOptions().setHasText("Create new Business Process"));
    }

    public Locator businessProcessNameInput() {
        return createBusinessProcessModal().locator("input[data-field='fileNameTextBox']#fileName");
    }

    /**
     * Footer primary button ({@code btn btn-primary}). Label is {@code " Ok"} with a {@code fa-plus} icon,
     * so {@code getByRole(name=Ok)} is unreliable.
     */
    public Locator createBusinessProcessOkButton() {
        return createBusinessProcessModal()
                .locator(".modal-footer button.btn-primary")
                .filter(new Locator.FilterOptions().setHasText(OK_BUTTON_LABEL));
    }

    public void expectCreateBusinessProcessModalVisible() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS);
        createBusinessProcessModal().waitFor(wait);
        businessProcessNameInput().waitFor(wait);
        createBusinessProcessOkButton().waitFor(wait);
    }

    public void fillBusinessProcessName(String name) {
        Locator input = businessProcessNameInput();
        input.click();
        input.fill(name);
    }

    public void clickCreateBusinessProcessOk() {
        Locator ok = createBusinessProcessOkButton();
        Locator.ClickOptions click = new Locator.ClickOptions().setTimeout(LOAD_TIMEOUT_MS);

        ok.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(LOAD_TIMEOUT_MS));
        ok.scrollIntoViewIfNeeded();

        try {
            ok.click(click);
        } catch (RuntimeException firstClickFailed) {
            // Modal backdrop / GWT overlay sometimes intercepts the first click.
            ok.click(click.setForce(true));
        }
    }

    public void waitAfterCreateBusinessProcessOk() {
        int ms = postOkDelayMillis();
        if (ms > 0) {
            page.waitForTimeout(ms);
        }
    }

    public void createBusinessProcessNamed(String name) {
        expectCreateBusinessProcessModalVisible();
        fillBusinessProcessName(name);
        clickCreateBusinessProcessOk();
        waitAfterCreateBusinessProcessOk();
    }

    private static int postOkDelayMillis() {
        String raw = System.getProperty("playwright.step.delay.ms", String.valueOf(DEFAULT_POST_OK_DELAY_MS));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_POST_OK_DELAY_MS;
        }
    }
}
