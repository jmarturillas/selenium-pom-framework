package com.gengo.automation.tests.CoreTest;

import com.gengo.automation.global.AutomationBase;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * @case Verify that approved jobs have fine status after approval.
 * @reference https://gengo.testrail.com/index.php?/cases/view/40337
 */
public class C40337_6_ApprovedJob extends AutomationBase {

    public C40337_6_ApprovedJob() throws IOException {}

    private String[] itemToTranslates;
    private String[] translatedItems;
    private String[] unitCounts;
    private String excerpt;
    private List<String> excerpts = new ArrayList<>();
    private List<String> jobId;

    @BeforeClass
    public void initFields() throws IOException{
        this.newUser = csv.generateNewUser(var.SHEETNAME_CUSTOMERS, 0);
        excerpt = var.getExcerptNonEnglish(1);
        excerpts.add(excerpt);
        excerpts.add(var.getExcerptEnglish(2));
        excerpts.add(var.getExcerptEnglish(3));

        itemToTranslates  = new String[] {
                var.getTranslatedItem(1),
                var.getTranslatedItem(2),
                var.getTranslatedItem(3),
        };
        unitCounts = new String[] {
                var.getUnitCountTarget(1),
                var.getUnitCountTarget(2),
                var.getUnitCountTarget(3),
        };
        translatedItems = new String[] {
                var.getItemToTranslate(1),
                var.getItemToTranslate(2),
                var.getItemToTranslate(3),
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

    @Test(description = "Text/Pro/Group/Character without HTML tags or Triple brackets", priority = 2)
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

        // Choose source language {Japanese this time}.
        customerOrderLanguagesPage.choooseLanguage(var.getJapaneseTo());
        customerOrderLanguagesPage.clickNextOptions();

        // Order as group.
        customerCheckoutPage.orderAsAGroup();

        // Select 'Business' tier.
        customerCheckoutPage.businessTier(true);

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

        loginPage.loginAccount(var.getTranslatorPro(5), var.getDefaultPassword(), true);

        // Check if the page redirected to contains the elements that should be seen in a Translator Dashboard
        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Navigate to the Jobs Page and look for the recently created job.
        translatorDashboardPage.clickJobsTab();
        translatorJobsPage.findJob(excerpt, jobId);

        // Translate the job and assert that after submission, it redirects to the correct page.
        workbenchPage.translateJobMultiple(translatedItems, excerpts, translatedItems.length);
        assertTrue(page.getCurrentUrl().equals(translatorDashboardPage.TRANSLATOR_DASHBOARD_URL),
                var.getWrongUrlErrMsg());

        // Sign out.
        global.nonAdminSignOut();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
    }

    @Test(priority = 4)
    public void customerApprovesJob() {
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

        global.selectCustomer();

        // Navigate through the orders list and find the job; Assert that the found job is correct.
        global.clickOrdersTab();

        // Goto customer reviewables.
        customerOrdersPage.clickReviewableOption();

        // Approve the job and assert everything is okay.
        customerOrdersPage.approveJobs(excerpts, jobId);

        // Sign out.
        global.nonAdminSignOut();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
    }
}
