package com.example.support;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

/**
 * Shared navigation for UI tests so URL visits and wait strategy stay consistent.
 */
public final class PageNavigation {

    private PageNavigation() {
    }

    /**
     * Opens {@code url} and waits until DOM content is loaded (works well for SPAs and hash routes).
     */
    public static void visit(Page page, String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }
}
