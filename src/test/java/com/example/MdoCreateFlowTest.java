package com.example;

import com.example.pages.FlowsPage;
import com.example.pages.FuseFlowManagerAddAssetsPage;
import com.example.pages.FuseFlowManagerHomePage;
import com.example.pages.CreateNewFlowSidebarPage;
import com.example.support.MdoSession;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class MdoCreateFlowTest {

    private static final String NEW_FLOW_SAMPLE = "testNewAgain";
    private static final String NEW_BUSINESS_PROCESS_NAME = "Test1";

    @Test
    void opensFlowDiagramAndSelectsBusinessProcessAsset(Page page) {
        FlowsPage flows = navigateToFlowRowActions(page);

        FuseFlowManagerHomePage flowManager = flows.openFlowDiagramInNewTab();

        // Home perspective → Design card (#home-action-design)
        page.waitForTimeout(4000);
        flowManager.expectOnHomeScreen();
        page.waitForTimeout(3000);
        flowManager.selectDesign();

        FuseFlowManagerAddAssetsPage addAssets = new FuseFlowManagerAddAssetsPage(flowManager.page());
        addAssets.expectAddAssetButtonVisible();
        Assertions.assertTrue(addAssets.addAssetButton().isVisible());

        addAssets.openAssetTypePicker();
        addAssets.expectBusinessProcessCardVisible();
        addAssets.selectBusinessProcess();
        addAssets.expectCreateBusinessProcessModalVisible();
        addAssets.fillBusinessProcessName(NEW_BUSINESS_PROCESS_NAME);
        Assertions.assertEquals(
                NEW_BUSINESS_PROCESS_NAME,
                addAssets.businessProcessNameInput().inputValue(),
                "Business Process name field should contain the entered value");
        addAssets.clickCreateBusinessProcessOk();
        addAssets.waitAfterCreateBusinessProcessOk();
    }

    /** Shared steps: login, filter to the flow row, open the row ellipsis menu. */
    private FlowsPage navigateToFlowRowActions(Page page) {
        FlowsPage flows = MdoSession.signInOpenFlowsAll(page);

        // flows.clickCreateNewFlow();
        // CreateNewFlowSidebarPage createFlow = new CreateNewFlowSidebarPage(page);
        // createFlow.expectOpen();

        // createFlow.fillProjectName(NEW_FLOW_SAMPLE);
        // createFlow.fillProjectDescription(NEW_FLOW_SAMPLE);
        // Assertions.assertEquals(
        //         NEW_FLOW_SAMPLE,
        //         createFlow.projectNameInput().inputValue(),
        //         "Project name field should reflect the entered value");
        // Assertions.assertEquals(
        //         NEW_FLOW_SAMPLE,
        //         createFlow.projectDescriptionInput().inputValue(),
        //         "Project description should reflect the entered value");

        // createFlow.clickSave();
        // page.waitForTimeout(3000);

        flows.openStatusMenuAndSelectAll("All");
        flows.openStatusMenuAndSelectAll("not published");

        flows.expectFlowNamedInTable(NEW_FLOW_SAMPLE);
        flows.clickRowActionsButtonForFlow(NEW_FLOW_SAMPLE);
        return flows;
    }
}
