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
    private static final int OK_CLICK_TIMEOUT_MS = 4_000;
    private static final int OK_CLICK_INTERVAL_MS = 4_000;
    private static final int CANVAS_SETTLE_MS = 10_000;
    private static final Pattern BUSINESS_PROCESS_TITLE = Pattern.compile("^\\s*Business Process\\s*$");

    public static final Pattern ADD_ASSETS_URL =
            Pattern.compile(".*kie-wb\\.jsp#LibraryPerspective.*AddAssetsScreen.*");

    private final Page page;

    public FuseFlowManagerAddAssetsPage(Page page) {
        this.page = page;
    }

    public void expectAddAssetButtonVisible() {
        addAssetButton().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
    }

    public Locator addAssetButton() {
        return page.locator("button[data-field='add-asset']");
    }

    public void openAssetTypePicker() {
        Locator button = addAssetButton();
        button.scrollIntoViewIfNeeded();
        button.click();
        businessProcessCard().waitFor(new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS));
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
     * Visible {@code div.modal-content} for {@code Create new Business Process} (matches provided HTML).
     */
    private Locator createBusinessProcessModal() {
        return page.locator("div.modal-content")
                .filter(new Locator.FilterOptions()
                        .setHas(page.locator("h4.modal-title")
                                .filter(new Locator.FilterOptions().setHasText("Create new Business Process"))))
                .filter(new Locator.FilterOptions().setVisible(true))
                .last();
    }

    public Locator businessProcessNameInput() {
        return createBusinessProcessModal().locator("input[data-field='fileNameTextBox']#fileName");
    }

    /**
     * {@code .modal-footer} primary button: {@code <button class="btn btn-primary"><i class="fa fa-plus"></i> Ok</button>}.
     */
    private Locator createBusinessProcessOkButton() {
        return createBusinessProcessModal()
                .locator(".modal-footer div button.btn-primary");
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
        expectCreateBusinessProcessModalVisible();
        createBusinessProcessOkButton().click();
        createBusinessProcessModal().focus();
        createBusinessProcessOkButton().press("Enter");
    }

    /**
     * Clicks Ok every {@value OK_CLICK_INTERVAL_MS} ms while the loading row is visible.
     */
    private int clickOkEveryIntervalUntilLoaderHidden() {
        Locator ok = createBusinessProcessOkButton();
        Locator.ClickOptions click = new Locator.ClickOptions()
                .setTimeout(OK_CLICK_TIMEOUT_MS)
                .setNoWaitAfter(true);

        ok.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(LOAD_TIMEOUT_MS));
        ok.scrollIntoViewIfNeeded();

        int okClickCount = 0;
        long deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            if (ok.isVisible()) {
                okClickCount += performOkClick(ok, click, okClickCount + 1);
            }

            if (!isCreateFlowLoaderVisible()) {
                System.out.println(
                        "[FuseFlowManager] Loader not visible after " + okClickCount + " Ok click(s)");
                return okClickCount;
            }

            System.out.println(
                    "[FuseFlowManager] Loader still visible — waiting "
                            + OK_CLICK_INTERVAL_MS
                            + " ms before next Ok click");
            page.waitForTimeout(OK_CLICK_INTERVAL_MS);
        }

        throw new IllegalStateException(
                "Loading spinner still visible after " + okClickCount + " Ok click(s) within " + LOAD_TIMEOUT_MS + " ms");
    }

    private int performOkClick(Locator ok, Locator.ClickOptions click, int attemptNumber) {
        try {
            ok.click(click);
            System.out.println(
                    "[FuseFlowManager] Create Business Process Ok click #" + attemptNumber + " (normal)");
            return 1;
        } catch (RuntimeException firstClickFailed) {
            System.out.println(
                    "[FuseFlowManager] Create Business Process Ok click #" + attemptNumber
                            + " normal failed: "
                            + firstClickFailed.getMessage());
            ok.click(click.setForce(true));
            System.out.println(
                    "[FuseFlowManager] Create Business Process Ok click #" + attemptNumber + " (force)");
            return 1;
        }
    }

    private boolean isCreateFlowLoaderVisible() {
        Locator modalLoading = createBusinessProcessModal()
                .locator("div.well:has(.spinner.spinner-lg):has-text('Loading...')");
        if (modalLoading.count() > 0 && modalLoading.isVisible()) {
            return true;
        }
        Locator pageLoading = page.locator("div.well:has(.spinner.spinner-lg):has-text('Loading...')");
        return pageLoading.count() > 0 && pageLoading.isVisible();
    }

    /**
     * Post-Ok: page loading finishes → open BPMN canvas → pause 10s.
     */
    public void waitAfterCreateBusinessProcessOk(String businessProcessName) {
        waitForPageLoadingToFinish();
        openCanvas(businessProcessName);
        page.waitForTimeout(CANVAS_SETTLE_MS);
    }

    private void waitForPageLoadingToFinish() {
        Locator loading = page.locator("div.well:has(.spinner.spinner-lg):has-text('Loading...')");
        loading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(LOAD_TIMEOUT_MS));
        loading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(LOAD_TIMEOUT_MS));
    }

    private void openCanvas(String businessProcessName) {
        Pattern name = Pattern.compile(
                "^\\s*" + Pattern.quote(businessProcessName) + "(?:\\.bpmn)?\\s*$",
                Pattern.CASE_INSENSITIVE);
        Locator asset = page.locator("[data-field='name'], span.name, td, a")
                .filter(new Locator.FilterOptions().setHasText(name))
                .first();

        asset.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(LOAD_TIMEOUT_MS));
        asset.scrollIntoViewIfNeeded();
        asset.dblclick(new Locator.DblclickOptions().setTimeout(LOAD_TIMEOUT_MS));

        page.locator("div.ORYX_Editor canvas, #oryx_canvas canvas, canvas")
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(LOAD_TIMEOUT_MS));
    }
}
