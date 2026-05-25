package com.example;

import com.example.support.PageNavigation;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;

public class GoogleTest {

    @Test
    void testGoogle() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(false)
            );

            Page page = browser.newPage();

            PageNavigation.visit(page, "https://google.com");

            System.out.println("Title: " + page.title());

            browser.close();
        }
    }
}