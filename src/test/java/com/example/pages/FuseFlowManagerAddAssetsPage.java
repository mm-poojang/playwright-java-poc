package com.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

/**
 * Fuse Flow Manager library — Add Assets, asset-type picker, and Create Business Process modal.
 */
public final class FuseFlowManagerAddAssetsPage {

    private static final int LOAD_TIMEOUT_MS = 120_000;
    private static final int OK_CLICK_TIMEOUT_MS = 4_000;
    private static final int OK_CLICK_INTERVAL_MS = 4_000;
    private static final int SUBMISSION_POLL_MS = 3_000;
    private static final int GWT_SETTLE_MS = 300;
    private static final int GWT_KEY_DELAY_MS = 30;
    private static final Pattern BUSINESS_PROCESS_TITLE = Pattern.compile("^\\s*Business Process\\s*$");

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
        input.press("ControlOrMeta+A");
        input.press("Backspace");
        input.pressSequentially(
                name,
                new Locator.PressSequentiallyOptions().setDelay(GWT_KEY_DELAY_MS));
        input.press("Tab");
        page.waitForTimeout(GWT_SETTLE_MS);
    }

    public void clickCreateBusinessProcessOk() {
        expectCreateBusinessProcessModalVisible();
        Locator ok = createBusinessProcessOkButton();
        ok.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(LOAD_TIMEOUT_MS));
        ok.scrollIntoViewIfNeeded();

        if (trySubmitCreateBusinessProcess("Enter on name field", this::submitViaNameFieldEnter)
                || trySubmitCreateBusinessProcess("physical mouse click", () -> submitViaMouseClick(ok))
                || trySubmitCreateBusinessProcess("Space on focused Ok", () -> submitViaKeyboard(ok, "Space"))
                || trySubmitCreateBusinessProcess("Enter on focused Ok", () -> submitViaKeyboard(ok, "Enter"))
                || trySubmitCreateBusinessProcess("Playwright locator click", () -> submitViaLocatorClick(ok))
                || trySubmitCreateBusinessProcess("dispatched pointer events", () -> submitViaDispatchEvents(ok))) {
            waitForSubmissionToFinish();
            return;
        }

        throw new IllegalStateException(
                "Create Business Process Ok did not start submission — all strategies failed");
    }

    private boolean trySubmitCreateBusinessProcess(String label, Runnable strategy) {
        if (isCreateFlowSubmissionStarted()) {
            return true;
        }
        System.out.println("[FuseFlowManager] Trying submit strategy: " + label);
        strategy.run();
        if (waitForCreateFlowSubmissionStarted(SUBMISSION_POLL_MS)) {
            System.out.println("[FuseFlowManager] Submission started via: " + label);
            return true;
        }
        System.out.println("[FuseFlowManager] Strategy did not start submission: " + label);
        return false;
    }

    /** GWT modals often wire the default action to Enter while the name field has focus. */
    private void submitViaNameFieldEnter() {
        Locator input = businessProcessNameInput();
        input.click();
        page.waitForTimeout(GWT_SETTLE_MS);
        input.press("Enter");
    }

    /** Bypasses locator click hit-testing; uses raw mouse coordinates on the Ok button. */
    private void submitViaMouseClick(Locator ok) {
        BoundingBox box = ok.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Ok button has no bounding box");
        }
        double x = box.x + box.width / 2;
        double y = box.y + box.height / 2;
        page.mouse().move(x, y);
        page.mouse().down();
        page.waitForTimeout(50);
        page.mouse().up();
    }

    private void submitViaKeyboard(Locator ok, String key) {
        ok.focus();
        page.waitForTimeout(GWT_SETTLE_MS);
        page.keyboard().press(key);
    }

    private void submitViaLocatorClick(Locator ok) {
        ok.click(new Locator.ClickOptions()
                .setTimeout(OK_CLICK_TIMEOUT_MS)
                .setNoWaitAfter(true));
    }

    private void submitViaDispatchEvents(Locator ok) {
        ok.dispatchEvent("mousedown");
        ok.dispatchEvent("mouseup");
        ok.dispatchEvent("click");
    }

    private boolean isCreateFlowSubmissionStarted() {
        if (isCreateFlowLoaderVisible()) {
            return true;
        }
        return createBusinessProcessModal().count() == 0
                || !createBusinessProcessModal().isVisible();
    }

    private boolean waitForCreateFlowSubmissionStarted(int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isCreateFlowSubmissionStarted()) {
                return true;
            }
            page.waitForTimeout(200);
        }
        return isCreateFlowSubmissionStarted();
    }

    private void waitForSubmissionToFinish() {
        long deadline = System.currentTimeMillis() + LOAD_TIMEOUT_MS;
        while (isCreateFlowLoaderVisible() && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(OK_CLICK_INTERVAL_MS);
        }
        if (isCreateFlowLoaderVisible()) {
            clickOkEveryIntervalUntilLoaderHidden();
        }
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
            ok.click(new Locator.ClickOptions()
                    .setTimeout(OK_CLICK_TIMEOUT_MS)
                    .setNoWaitAfter(true)
                    .setForce(true));
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
         page.waitForTimeout(LOAD_TIMEOUT_MS);
    }
}
