package com.gengo.automation.tests.CoreTest;

import com.gengo.automation.global.AutomationBase;
import org.openqa.selenium.By;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * @case Tests that the interaction with the left panel information is stable.
 * @reference https://gengo.testrail.com/index.php?/cases/view/40346
 */
public class C40346_1_LeftPanelInformation extends AutomationBase {

    public C40346_1_LeftPanelInformation() throws IOException {}

    private String[] itemToTranslates;
    private String[] translatedItems;
    private String[] unitCounts;
    private String excerpt;
    private List<String> excerpts = new ArrayList<>();
    private List<String> jobId;

    private By criteria = By.xpath("//textarea[@placeholder='Write a comment in English']");

    @BeforeClass
    public void initFields() throws IOException{
        this.newUser = csv.generateNewUser(var.SHEETNAME_CUSTOMERS, 0);
        excerpt = var.getExcerptEnglish(10);
        excerpts.add(excerpt);

        itemToTranslates  = new String[] {
                var.getItemToTranslate(10),
        };
        unitCounts = new String[] {
                var.getUnitCountSource(10),
        };
        translatedItems = new String[] {
                var.getTranslatedItem(10),
        };
    }

    @AfterClass
    public void afterRun() throws IOException {
        csv.writeValue(this.newUser, var.SHEETNAME_CUSTOMERS, 0);
        Reporter.log("Done running '" + this.getClass().getSimpleName() + "'", true);
    }

    @Test(description = "Use a new account as conditional case for database reset", priority = 1)
    public void createCustomerAccount() {
        globalMethods.createNewCustomer(this.newUser, true);
    }

    @Test(description = "Non-grouped job.", priority = 2)
    public void placeAnOrder() throws IOException {
        pluginPage.passThisPage();

        // Assert that homepage 'SIGN IN' link is displayed.
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        homePage.clickSignInButton();

        // Check that after clicking the 'SIGN IN' link, the URL is right and 'SIGN UP' button is displayed as well.
        assertTrue(page.getCurrentUrl().equals(loginPage.LOGIN_PAGE_URL),
                var.getWrongUrlErrMsg());
        assertTrue(loginPage.getSignUpBtn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        loginPage.loginAccount(this.newUser, var.getDefaultPassword());

        // Assert that page title is present in the DOM.
        assertTrue(global.getPageTitle().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Assert that the order icon is present for ordering process.
        assertTrue(global.getOrderTranslationIcon().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Select 'Customer' account, navigate through the order form and assert that the redirected page is correct.
        global.selectCustomer();
        global.clickOrderTranslationIcon();
        assertTrue(page.getCurrentUrl().contains
                (customerOrderFormPage.ORDER_FORM_URL), var.getWrongUrlErrMsg());

        // Place the order in the text area.
        customerOrderFormPage.inputItemToTranslate(itemToTranslates, unitCounts, itemToTranslates.length, false);

        // Assert that the page url after submitting is correct and
        // assert that the source language is auto detected and English.
        assertTrue(page.getCurrentUrl().equals
                (customerOrderLanguagesPage.ORDERLANGUAGES_URL), var.getWrongUrlErrMsg());
        assertTrue(customerOrderLanguagesPage
                .isSourceAutoDetected(), var.getTextNotEqualErrMsg());

        // Choose target language {Japanese this time}.
        customerOrderLanguagesPage.choooseLanguage(var.getJapaneseTo());
        customerOrderLanguagesPage.clickNextOptions();

        page.refresh();

        // Place payment via existing credits and assert that the placement of order is successful.
        customerCheckoutPage.clickPayNowAndConfirm(false, false, false, false, false);
        assertTrue(page.getCurrentUrl().contains
                (customerOrderCompletePage.ORDERCOMPLETE_URL), var.getWrongUrlErrMsg());

        // Go back to the customer dashboard and assert that customer dashboard is the redirected page.
        customerOrderCompletePage.clickGoToDashboard();
        assertTrue(customerDashboardPage.getCustomerDashboardText().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Navigate to 'Orders' page and find the Job ID.
        global.goToOrdersPage();
        customerOrdersPage.clickPendingOption();
        assertTrue(customerOrdersPage.getPendingHeader().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Extract the job id from pending jobs.
        jobId = customerOrdersPage.extractJobIdFromPendingJobs(excerpts);

        // Sign out
        global.nonAdminSignOut();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
    }

    @Test(priority = 3)
    public void pickUpByTranslator() throws IOException {
        pluginPage.passThisPage();

        // Assert that homepage 'SIGN IN' link is displayed.
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        homePage.clickSignInButton();

        // Check that after clicking the 'SIGN IN' link, the URL is right and 'SIGN UP' button is displayed as well.
        assertTrue(page.getCurrentUrl().equals(loginPage.LOGIN_PAGE_URL),
                var.getWrongUrlErrMsg());
        assertTrue(loginPage.getSignUpBtn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        loginPage.loginAccount(var.getTranslatorStandard(5), var.getDefaultPassword(), true);

        // Check if the page redirected to contains the elements that should be seen in a Translator Dashboard
        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Navigate to the Jobs Page and look for the recently created job.
        global.clickJobsTab();
        translatorJobsPage.findJob(excerpt, jobId);
        assertTrue(workbenchPage.getStartTranslateMainButton()
                .isDisplayed(), var.getElementIsNotDisplayedErrMsg());

        // Close the workbench modal; Assert that the job comment icon is displayed.
        workbenchPage.closeWorkbenchModal();
        assertTrue(workbenchPage.getJobCommentButton()
                .isDisplayed(), var.getElementIsNotDisplayedErrMsg());

        // Play around the left panel and check if the comments area is visible.
        workbenchPage.clickJobCommentButton(true);
        assertTrue(workbenchPage.getWorkbenchCommentsTextArea()
                .isDisplayed(), var.getElementIsNotDisplayedErrMsg());
        workbenchPage.clickJobCommentButton(false);
        wait.untilElementNotVisible(criteria);
        workbenchPage.clickJobCommentButton(true);
        assertTrue(workbenchPage.getWorkbenchCommentsTextArea()
                .isDisplayed(), var.getElementIsNotDisplayedErrMsg());

        // Add translator's comment.
        workbenchPage.addTranslatorsComment(var.getTranslatorComment(), false);

        // Translate the job.
        workbenchPage.translateJob(translatedItems[0], false);
        assertTrue(page.getCurrentUrl().equals(translatorDashboardPage.TRANSLATOR_DASHBOARD_URL),
                var.getWrongUrlErrMsg());

        // Sign out
        global.nonAdminSignOut();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
    }
}
