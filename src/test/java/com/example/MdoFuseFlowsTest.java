package com.example;

import com.example.support.PageNavigation;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class MdoFuseFlowsTest {

    private static final String MDO_FUSE_FLOWS_URL =
            "https://lt.masterdataonline.com/ui/en/index.html#/home/flows/_all";

    @Test
    void opensMdoFuseFlowsAllPage(Page page) {
        PageNavigation.visit(page, MDO_FUSE_FLOWS_URL);

        String title = page.title();
        Assertions.assertFalse(title.isBlank(), "Page should expose a non-empty title after load");
    }
}
