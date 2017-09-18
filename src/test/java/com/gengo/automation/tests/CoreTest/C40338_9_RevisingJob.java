package com.gengo.automation.tests.CoreTest;

import com.gengo.automation.global.AutomationBase;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @case Revising Job
 * Character to Word, Credits Payment, Standard Tier, File Job, With Glossary
 * @reference https://gengo.testrail.com/index.php?/cases/view/40338
 */
public class C40338_9_RevisingJob extends AutomationBase {

    private final String SOURCE = "Japanese", TARGET = "English";
    private String parentWindow, orderNo, excerpt, jobNo, translatedItem, price, instruction, revisedExcerpt;
    private String[] unitCount, itemToTranslate;
    private HashMap<String, String> glossaryMatches = new HashMap<>(); // contains a hashmap of glossary matches

    @BeforeClass
    public void initFields() throws IOException{
        instruction = var.getCustomerInstruction();
        glossaryMatches.put("言語", "Gengo");
        glossaryMatches.put("テスト", "Test");
        glossaryMatches.put("記録", "Record");
        excerpt = var.getExcerptFile(4);
        revisedExcerpt = var.getExcerptNonEnglish(18);
        translatedItem = var.getFile(82);
        itemToTranslate = new String[] {
                var.getFile(79)
        };
        unitCount = new String[] {
                var.getUnitCountSource(21)
        };
    }

    public C40338_9_RevisingJob() throws IOException {}

    @AfterClass
    public void afterRun() {
        Reporter.log("Done running '" + this.getClass().getSimpleName() + "'", true);
    }

    /**
     * placeAnOrder --- a method for checking the logging in of a customer account,
     * and placing an order with glossary chosen
     * */
    @Test
    public void placeAnOrder() {
        pluginPage.passThisPage();

        // Check if the prominent page elements can be seen on the Home Page
        assertTrue(homePage.checkHomePage());
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        // Logging in a customer account
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getCustomer(27), var.getDefaultPassword());

        // Check if the prominent page elements can be seen on the Customer Dashboard Page
        assertTrue(page.getCurrentUrl().equals(customerDashboardPage.CUSTOMER_DASHBOARD_URL),
                var.getWrongUrlErrMsg());
        assertTrue(customerDashboardPage.checkCustomerDashboard());

        // Click the icon for placing an order
        global.clickOrderTranslationIcon();

        // Place the text to be translated on the order form and check for the word/character count
        customerOrderFormPage.inputItemToTranslate(itemToTranslate, unitCount, itemToTranslate.length, true);

        // Check if source language is auto detected by the system
        assertTrue(page.getCurrentUrl().equals(customerOrderLanguagesPage.ORDERLANGUAGES_URL),
                var.getWrongUrlErrMsg());
        assertTrue(customerOrderLanguagesPage.isSourceAutoDetected(var.getJapaneseFrom()),
                var.getTextNotEqualErrMsg());

        // Set the target language to English
        customerOrderLanguagesPage.choooseLanguage(var.getEnglishTo());
        customerOrderLanguagesPage.clickNextOptions();

        // Choose glossary file to be used
        customerCheckoutPage.addGlossary("ja_to_en");
        page.refresh();

        // Retrieve the total order price for comparison across pages later
        price = customerCheckoutPage.getOrderTotalPrice().getText();

        // Check the 'View Full Quote' page and the generated pdf File
        customerCheckoutPage.clickViewFullQuote();
        parentWindow = switcher.getWindowHandle();
        switcher.switchToPopUp();
        wait.impWait(3);
        assertTrue(page.getCurrentUrl().equals(customerOrderQuotePage.CUSTOMERORDERQUOTE_URL),
                var.getWrongUrlErrMsg());
        customerOrderQuotePage.typeAdress();
        assertTrue(customerOrderQuotePage.getAddressEmbedded().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Check if the price displayed on the full quote page is consistent with total order price
        assertEquals(price, customerCheckoutPage.getFullQuoteTotalPrice().getText(), var.getTextNotEqualErrMsg());

        // Download Quote File
        customerOrderQuotePage.downloadQuote();
        switcher.switchToParentWindow(parentWindow);

        // Add instructions
        customerCheckoutPage.addInstructions(instruction);

        // Place payment via Paypal
               customerCheckoutPage.payWithCredits();

        // Retrieve the order number
        orderNo = customerOrderCompletePage.orderNumber();

        // Check if the price on the order complete page is consistent with the total order price
        //assertEquals(price, customerOrderCompletePage.totalPrice(), var.getTextNotEqualErrMsg());

        // Return to dashboard page
        customerOrderCompletePage.clickGoToDashboard();

        // Go to Customer's Pending Orders Page
        globalPage.goToOrdersPage();
        customerOrdersPage.clickPendingOption();

        // Look for the recently created job
        customerOrdersPage.findOrder(excerpt);

        // Check if the instructions is displayed on the page
        assertTrue(customerOrderDetailsPage.checkInstruction(instruction), var.getElementIsNotDisplayedErrMsg());

        // Check if the price is consistent with the one displayed on the order checkout page
        assertEquals(price, customerOrderDetailsPage.getOrderPrice().getText(), var.getAmountIsNotRightErrMsg());

        // Retrieve Job Number for use later
        jobNo = customerOrderDetailsPage.getJobNumberAvailableJob();

        // Customer sign out
        global.nonAdminSignOut();

        // Check if the redirected page contains the prominent Home Page elements
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        assertTrue(homePage.checkHomePage());
    }

    /**
     * translatorFindJob --- a method wherein a translator signs in and looks for the recently
     * created job and opens the workbench file page
     * */
    @Test(dependsOnMethods = "placeAnOrder")
    public void translatorFindJob() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        // Log in a translator account
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getTranslatorPro(4), var.getDefaultPassword());

        // Check if the page redirected to contains the elements that should be seen in a Translator Dashboard
        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Navigate to the Jobs Page and look for the recently created job
        translatorDashboardPage.clickJobsTab();
        translatorJobsPage.findJob(excerpt);
    }

    /**
     * translatorOpenJob --- a method which checks the layout of the workbench and checks the glossary section
     * */
    @Test(dependsOnMethods = "translatorFindJob")
    public void translatorOpenJob() {
        parentWindow = switcher.getWindowHandle();

        // Check if instruction is viewable by the translator
        assertTrue(workbenchFilePage.checkComment(instruction), var.getElementIsNotDisplayedErrMsg());

        // Check if the Hint Modal box for Glossary is displayed
        workbenchFilePage.checkGlossaryHint();

        // Check if the page opened contains the correct details for the Collection Glossary matches before picking up job
        assertTrue(workbenchFilePage.checkGlossaryPage(jobNo, glossaryMatches, SOURCE, TARGET), var.getElementIsNotDisplayedErrMsg());

        // Switch to main tab
        switcher.switchToParentWindow(parentWindow);

        // Download and start job
        workbenchFilePage.downloadFile();
        parentWindow = switcher.getWindowHandle();

        // Check if the page opened contains the correct details for the Collection Glossary matches after picking up job
        assertTrue(workbenchFilePage.checkGlossaryPage(jobNo, glossaryMatches, SOURCE, TARGET), var.getElementIsNotDisplayedErrMsg());

        // Switch back to main window
        switcher.switchToParentWindow(parentWindow);

        // Upload translated file and submit
        workbenchFilePage.translateFileJobWithGlossary(translatedItem);
        page.refresh();
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * customerApprove --- this method has the customer approve the job
     * */
    @Test(dependsOnMethods = "translatorOpenJob")
    public void customerRequestCorrection() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getCustomer(27), var.getDefaultPassword());
        global.selectCustomer();
        assertTrue(customerDashboardPage.checkCustomerDashboard());
        assertTrue(page.getCurrentUrl().equals(customerDashboardPage.CUSTOMER_DASHBOARD_URL),
                var.getWrongUrlErrMsg());
        globalPage.goToOrdersPage();
        customerOrdersPage.clickReviewableOption();
        customerOrdersPage.findOrder(excerpt);

        // Job is approved
        customerOrderDetailsPage.requestCorrection(var.getCustomerCorrection(), true, true, true);
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * translatorOpenJob --- a method which checks the layout of the workbench and checks the glossary section
     * */
    @Test(dependsOnMethods = "customerRequestCorrection")
    public void translatorRevise() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        // Log in a translator account
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getTranslatorPro(4), var.getDefaultPassword());

        // Check if the page redirected to contains the elements that should be seen in a Translator Dashboard
        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Navigate to the Jobs Page and look for the recently created job
        translatorDashboardPage.clickJobsTab();
        translatorJobsPage.findJob(revisedExcerpt);

        // Upload translated file and submit
        workbenchFilePage.translateFileJobWithGlossary(translatedItem);
        page.refresh();
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * customerApprove --- this method has the customer approve the job
     * */
    @Test(dependsOnMethods = "translatorRevise")
    public void customerApprove() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getCustomer(27), var.getDefaultPassword());
        global.selectCustomer();
        assertTrue(customerDashboardPage.checkCustomerDashboard());
        assertTrue(page.getCurrentUrl().equals(customerDashboardPage.CUSTOMER_DASHBOARD_URL),
                var.getWrongUrlErrMsg());
        globalPage.goToOrdersPage();
        customerOrdersPage.clickReviewableOption();
        customerOrdersPage.findOrder(excerpt);

        // Job is approved
        customerOrderDetailsPage.approveJob();
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * checkEmail --- this method logs in the on the email of the customer
     * and checks if the emails for order received, job review, flag, and
     * approval are visible
     * */
    @Test(dependsOnMethods = "customerApprove")
    public void checkEmail() {
        page.launchUrl(var.getGmailUrl());
        assertTrue(gmailSignInEmailPage.getTxtBoxEmail().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Log in on Gmail account
        gmailSignInEmailPage.inputEmail(var.getGmailEmail());
        gmailSignInPasswordPage.inputPasswordAndSubmit(var.getGmailPassword());

        // Check if the emails for Order Received, Review, and Approval are received
        assertTrue(gmailInboxPage.checkOrderReceived(orderNo));
        assertTrue(gmailInboxPage.checkJobForReview(jobNo));
        assertTrue(gmailInboxPage.checkJobApproved(jobNo));
    }
}
