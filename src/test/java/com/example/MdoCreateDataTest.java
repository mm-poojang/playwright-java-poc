package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.pages.DataPage;
import com.example.support.MdoSession;
import com.example.support.PlaywrightArtifactsExtension;
import com.microsoft.playwright.Page;

@ExtendWith(PlaywrightArtifactsExtension.class)
public class MdoCreateDataTest {
   
    private static final String SEARCH_TERM = "Material Master";
    private DataPage dataPage;

    
    @BeforeEach
    void setup(Page page) {
        dataPage = MdoSession.signInOpenDataPage(page);
    }

    @Test
    void createDataFromMaterialMaster() {
        dataPage.searchData(SEARCH_TERM);
        dataPage.clickNewRecordAndSelectFlow();
        dataPage.addPlantAndWait();
        dataPage.fillFormFields();
        dataPage.uploadFile("DNG", "DemoImage.dng");
        dataPage.uploadFile("DOC","TestDocumentFor FileUpload.doc");
        dataPage.selectRadio01();
        dataPage.submitForm();
    }
}
