package com.example;

import com.example.support.PageNavigation;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class GoogleTest {

    @Test
    void testGoogle(Page page) {
        PageNavigation.visit(page, "https://google.com");
        System.out.println("Title: " + page.title());
    }
}
