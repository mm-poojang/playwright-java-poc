package com.example.pages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public final class DataPage {

    private static final int LOAD_TIMEOUT_MS = 60_000;
    private String materialNumber;
    /** Hash route for the all-flows view (path/host may differ per environment). */
    public static final Pattern DATA_URL = Pattern.compile(".*#/home/list/datatable/\\d+.*");

    public static String objectNumber;
    public static String materialType;

    public static final String TEST_FILE_PATH = "src/test/resources/DemoImage.png";
    
    public final Page page;

    public DataPage(Page page) {
        this.page = page;
    }
    
    private void assertVisible(Locator locator) {
        assertThat(locator).isVisible(
            new LocatorAssertions.IsVisibleOptions()
                .setTimeout((double) LOAD_TIMEOUT_MS)
        );
    }

    public String generate18DigitNumber() {
        Random random = new Random();

        StringBuilder sb = new StringBuilder();

        // first digit should not be 0 (realistic number)
        sb.append(1 + random.nextInt(9));

        for (int i = 1; i < 18; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    public void expectOnDataPage() {
        page.waitForURL(DATA_URL, new Page.WaitForURLOptions().setTimeout(LOAD_TIMEOUT_MS));
        expectDataListLoaded();
    }

     public Locator DataHeading() {
        return page.getByRole(
        AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Data").setExact(true));
    }

    /**
     * Flows page is interactive: toolbar, filters, table visible, and loading skeletons cleared.
     */
    public void expectDataListLoaded() {
        Locator.WaitForOptions wait = new Locator.WaitForOptions().setTimeout(LOAD_TIMEOUT_MS);

        DataHeading().waitFor(wait);
    }


    public Locator searchDataInput() {
        return page.getByPlaceholder("Search data");
    }

    public void searchData(String value) {
        assertThat(DataHeading()).isVisible();
        searchDataInput().fill(value);
        searchDataInput().press("Enter");
        Locator dataset =
            page.getByText(value, new Page.GetByTextOptions().setExact(true));

        // Wait until it appears
        assertThat(dataset).isVisible();

        // Click it
        dataset.click();
    }

    public Locator newRecordButton() {
        return page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("New record")
        );
    }

    public Locator flowWithRejectionStepOption() {
        return page.getByRole(
            AriaRole.MENUITEM,
            new Page.GetByRoleOptions()
                .setName("Flow_with_rejection_step")
                .setExact(true)
        );
    }

    public void clickNewRecordAndSelectFlow() {

        assertThat(newRecordButton()).isVisible();
        newRecordButton().click();

        assertThat(flowWithRejectionStepOption()).isVisible();
        flowWithRejectionStepOption().click();
        waitForCreateRecordPage();
    }

    public Locator materialTypeHeading() {
        return page.getByText(
            "Material Type",
            new Page.GetByTextOptions().setExact(true)
        );
    }

    public Locator plantNode() {
        return page.getByText(
            "plant",
            new Page.GetByTextOptions().setExact(true)
        );
    }

    public void waitForCreateRecordPage() {

        assertVisible(materialTypeHeading());
        assertVisible(plantNode());
    }

    public Locator folderTreeButton() {
        return page
                .getByRole(
                        AriaRole.TREEITEM,
                        new Page.GetByRoleOptions().setName("plant"))
                .locator("lib-button")
                .getByRole(AriaRole.BUTTON);
    }

    public Locator filterDropdown() {
        return page.locator("div.filter-dropdown");
    }

    public Locator availablePlant0001() {
        return page.locator("div.key-field-item")
                .filter(new Locator.FilterOptions()
                        .setHasText("0001 -- Plant0001"))
                .first();
    }

    public Locator addPlant0001Button() {
        return availablePlant0001()
                .locator("button:has(mat-icon[fonticon='fa-plus'])");
    }

    public Locator selectedPlant0001() {
        return page.locator("mat-tree-node")
                .filter(new Locator.FilterOptions().setHasText("Plant:0001 -- Plant0001"));
    }

    public Locator submitButton() {
        return page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Submit"));
    }

    public void addPlantAndWait() {

        assertVisible(folderTreeButton());
        // Click the folder tree icon
        folderTreeButton().click();

        assertVisible(filterDropdown());

        // Verify Plant 0001 is available
        assertVisible(availablePlant0001());
        // Click the + icon for Plant0001
        addPlant0001Button().click();

        assertVisible(selectedPlant0001());
        // Verify that the plant has been added to the hierarchy
        assertVisible(submitButton());
    }   

    public Locator materialTypeDropdown() {
        return page.locator("pros-transaction-dropdown")
            .filter(new Locator.FilterOptions()
                .setHas(page.getByText("Material Type")));
    }

    public Locator materialTypeInput() {
        return materialTypeDropdown()
            .locator("input[role='combobox']");
    }

    public Locator materialTypeArrow() {
        return materialTypeDropdown().locator("mat-icon.fa-angle-down");
    }

    public Locator descriptionField() {
        return page.locator("div.f-col.mdo-field")
            .filter(new Locator.FilterOptions()
            .setHas(page.getByText("Description Testing")));
    }

    public Locator descriptionInput() {
        return descriptionField().locator("input[type='text']");
    }

    public void fillNextField() {
        Locator input = descriptionInput();

        assertThat(input).isVisible();
        input.fill("Test description POC");
    }


    public void openMaterialTypeDropdown() {
        materialTypeArrow().click();
        page.waitForSelector("mat-option, .mat-mdc-option, [role='option']",
            new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    public void selectMaterialType(String value) {
        materialTypeInput().click();
        materialTypeInput().fill(value);
        materialType=value;
        Locator option = page.locator("[role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(value))
                .first();

        option.waitFor();
        assertThat(option).isVisible();
        option.click();
    }

    public Locator materialDescriptionInput() {
        return page.locator("pros-transaction-input")
            .filter(new Locator.FilterOptions()
            .setHas(page.getByText("Material Description")))
            .locator("input[type='text']");
    }    

    public void fillFormFields() {
        openMaterialTypeDropdown();
        selectMaterialType("ERSA -- Spare Part");
        fillNextField();
        selectLotSize("EX");

    }

    public void uploadFile(String label, String fileName) {

        Path path = Paths.get("src/test/resources/" + fileName)
            .toAbsolutePath();

        Locator input = page.locator("pros-transaction-attachment")
            .filter(new Locator.FilterOptions()
            .setHas(page.getByText(label)))
            .locator("input[type='file']")
            .first();

        input.setInputFiles(path);
        System.out.println("File uploaded: " + path.toString());
    }

    public Locator radioButtonField() {
        return page.locator("pros-transaction-radio-group")
                .filter(new Locator.FilterOptions()
                        .setHas(page.locator("p").filter(
                                new Locator.FilterOptions().setHasText("Radio button"))))
                .first();
    }

    public Locator radio01() {
        return radioButtonField()
                .locator("input[type='radio'][value='01']");
    }

    public void selectRadio01() {
        assertThat(radio01()).isVisible();
        radio01().check();
    }

    public String enterMaterialDescriptionNumber() {
        String randomNumber = generate18DigitNumber();
        materialNumber = randomNumber; // store for later use
        Locator input = materialDescriptionInput();
        input.fill(randomNumber);
        System.out.println("Material Number Entered: " + randomNumber);
        return materialNumber;
    }

    public void submitForm() {
        assertVisible(submitButton());
        submitButton().click();
    }

    public Locator lotSizeDropdown() {
        return page.locator("pros-transaction-dropdown")
                .filter(new Locator.FilterOptions()
                        .setHas(page.getByText("Lot Size")));
    }

    public Locator lotSizeInput() {
        return lotSizeDropdown()
                .locator("input[role='combobox']");
    }

    public void selectLotSize(String value) {
        lotSizeInput().click();
        lotSizeInput().fill(value);

        Locator option = page.locator("mat-option,[role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(value))
                .first();

        option.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        option.click();
    }

    public Locator searchInput() {
        return page.locator("div.filters-list")
                .locator("lib-search input[placeholder='Search']")
                .first();
    }

    public Locator searchResult(String value) {
        return page.locator("mat-card.global-search mark")
                .filter(new Locator.FilterOptions().setHasText(value));
    }

    public Locator applyButton() {
        return page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Apply")
        );
    }

    public Locator searchResultRow(String searchTerm) {
        return page.locator("mat-card.global-search tr")
                .filter(new Locator.FilterOptions().setHasText(searchTerm));
    }

    public String getObjectNumber(String searchTerm) {
        Locator row = searchResultRow(searchTerm);

        assertVisible(row);
        String objectNumber = row
                .locator("td")
                .first()
                .locator("lib-text-line")
                .textContent()
                .trim();

        return objectNumber;
    }

    public Locator objectNumberInGrid(String objectNumber) {
        return page.locator(
            "td.mat-column-OBJECTNUMBER lib-text-line"
        ).filter(
            new Locator.FilterOptions().setHasText(objectNumber)
        );
    }

    public String verifyObjectNumberInGrid(String objectNumber) {
        Locator objectNumberCell = objectNumberInGrid(objectNumber);

        assertVisible(objectNumberCell);

        System.out.println("Object Number found in grid: " + objectNumber);

        return objectNumber;
    }

    public void searchAndSelect(String searchTerm) {
        searchInput().click();
        searchInput().fill(searchTerm);
        String materialNumber = getObjectNumber(searchTerm);

        Locator result = searchResult(searchTerm);

        assertVisible(result);
        result.click();
        assertVisible(applyButton());
        applyButton().click();
        System.out.println("Apply button Clicked");

        objectNumber = verifyObjectNumberInGrid(materialNumber);
    }

   public Locator rowByObjectNumber(String objectNumber) {
        return page.locator("tr")
                .filter(new Locator.FilterOptions().setHasText(objectNumber));
    }

    public Locator summaryMenuItem() {
        return page.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName("Summary"));
    }

    public Locator rejectFlowButton() {
        return page.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName("3Reject flow"));
    }

    public void clickEllipsisForObject(String objectNumber) {

        Locator row = page.locator("tr")
                .filter(new Locator.FilterOptions().setHasText(objectNumber));

        row.locator("mat-icon[fonticon='fa-ellipsis-h']")
                .click();

        System.out.println("Clicked ellipsis for Object Number: " + objectNumber);
    }

    public void openSummaryAndRejectFlow() {
        // Click 3 dots
        clickEllipsisForObject(objectNumber);

        // Click Summary
        assertVisible(summaryMenuItem());
        summaryMenuItem().click();

        // Click 3Reject flow
        assertVisible(rejectFlowButton());
        rejectFlowButton().click();

        assertVisible(materialTypeDropdown());
        System.out.println("Reject Flow popup loaded successfully");
    }

    public void validateSummaryDetails() {

        String actualMaterialNumber =
            materialDescriptionInput().inputValue().trim();

        String actualMaterialType =
            materialTypeInput().inputValue().trim();

        assertEquals(
            materialNumber.trim(),
            actualMaterialNumber,
            "Material Number mismatch"
        );

        assertEquals(
            materialType.trim(),
            actualMaterialType,
            "Material Type mismatch"
        );

        System.out.println("Material Number Validated: " + actualMaterialNumber);
        System.out.println("Material Type Validated: " + actualMaterialType);
    }

}
