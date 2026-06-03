package com.example;

import com.example.pages.FlowsPage;
import com.example.support.MdoSession;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class MdoLoginTest {

    @Test
    void submitsOrganizationUsernameAndChoosesPasswordLogin(Page page) {
        FlowsPage flows = MdoSession.signInOpenFlowsAll(page);

        Assertions.assertTrue(flows.flowsHeading().isVisible(), "Flows title should be visible");
        Assertions.assertTrue(
                flows.createNewFlowButton().isVisible(), "Create new flow button should be visible");
    }
}
