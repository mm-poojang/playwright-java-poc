package com.example;

import com.example.support.PageNavigation;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MdoFuseFlowsTest {

    private static final String MDO_FUSE_FLOWS_URL =
            "https://lt.masterdataonline.com/ui/en/index.html#/home/flows/_all";

    @Test
    void opensMdoFuseFlowsAllPage() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false));

            Page page = browser.newPage();
            PageNavigation.visit(page, MDO_FUSE_FLOWS_URL);

            String title = page.title();
            Assertions.assertFalse(title.isBlank(), "Page should expose a non-empty title after load");

            browser.close();
        }
    }
}
