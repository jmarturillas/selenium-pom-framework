package com.gengo.automation.tests.CoreTest;

import com.gengo.automation.global.AutomationBase;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @case Create new customer account - EMAIL
 * @reference https://gengo.testrail.com/index.php?/cases/view/40334
 */
public class C40334_1_CreateCustomerAccount extends AutomationBase {

    public C40334_1_CreateCustomerAccount() throws IOException {}

    @BeforeClass
    public void initField() {
        this.newUser = signUpPage.newCustomer();
    }

    @AfterClass
    public void afterRun() throws IOException {
        Reporter.log("Done running '" + this.getClass().getSimpleName() + "'", true);
    }

    @Test
    public void createNewCustomerAccount() {
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

        loginPage.clickSignUpButton();

        // Check that sign up page URL is correct.
        assertTrue(page.getCurrentUrl().equals(signUpPage.SIGNUP_PAGE_URL),
                var.getWrongUrlErrMsg());

        // Check that the links for Terms & Conditions and Privacy Policy
        assertEquals(signUpPage.termsAndConditionsLink(), signUpPage.TERMS_AND_CONDITIONS_URL, var.getWrongUrlErrMsg());
        assertEquals(signUpPage.privacyPolicyLink(), signUpPage.PRIVACY_POLICY_URL, var.getWrongUrlErrMsg());

        // Assert elements behave as expected.
        assertTrue(signUpPage.getParentDropDown().getText().equals("Customer"),
                var.getTextNotEqualErrMsg());
        assertFalse(signUpPage.getCreateAccountBtn().isEnabled(),
                var.getElementIsEnabledErrMsg());
        assertFalse(signUpPage.getFacebookLink().isEnabled(),
                var.getElementIsEnabledErrMsg());
        assertFalse(signUpPage.getGoogleLink().isEnabled(),
                var.getElementIsEnabledErrMsg());

        signUpPage.clickDropDownButton();
        assertTrue(signUpPage.getOptionTranslator().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        signUpPage.clickTranslatorOption();

        // Assert elements behave as expected after clicking the sign up dropdown button for translator as option.
        assertFalse(signUpPage.getCreateAccountBtn().isEnabled(),
                var.getElementIsEnabledErrMsg());
        assertFalse(signUpPage.getFacebookLink().isEnabled(),
                var.getElementIsEnabledErrMsg());
        assertFalse(signUpPage.getGoogleLink().isEnabled(),
                var.getElementIsEnabledErrMsg());

        signUpPage.clickDropDownButton();
        assertTrue(signUpPage.getOptionCustomer().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        signUpPage.clickCustomerOption();
        signUpPage.tickCheckBoxAgree();

        // Assert elements behave as expected after clicking the sign up dropdown button for customer as option.
        assertTrue(signUpPage.getCreateAccountBtn().isEnabled(),
                var.getElementIsNotEnabledErrMsg());
        assertTrue(signUpPage.getFacebookLink().isEnabled(),
                var.getElementIsNotEnabledErrMsg());
        assertTrue(signUpPage.getGoogleLink().isEnabled(),
                var.getElementIsNotEnabledErrMsg());

        // Submits the form without anything in it.
        signUpPage.clickCreateAccountButton();

        assertTrue(signUpPage.getFailedToSignUpEmptyFieldsMsg().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Submits the form with email but without password
        signUpPage.fillOutSignUpForm(this.newUser, "");
        signUpPage.clickCreateAccountButton();

        assertTrue(signUpPage.getFailedToSignUpEmptyFieldsMsg().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Submits the form without email but with password
        signUpPage.fillOutSignUpForm("", var.getDefaultPassword());
        signUpPage.clickCreateAccountButton();

        assertTrue(signUpPage.getFailedToSignUpEmptyFieldsMsg().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Fill out the form and submit.
        signUpPage.fillOutSignUpForm(this.newUser, var.getDefaultPassword());

        signUpPage.clickCreateAccountButton();

        assertTrue(page.getCurrentUrl().contains(successRegistrationPage.SUCCESS_PAGE_URL),
                var.getWrongUrlErrMsg());
        assertTrue(successRegistrationPage.getSuccessPageGengoLogo().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        assertTrue(successRegistrationPage.getSuccessPageBody().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Go to Gmail account
        page.launchUrl(var.getGmailUrl());

        assertTrue(gmailSignInEmailPage.getTxtBoxEmail().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        gmailSignInEmailPage.inputEmail(var.getGmailEmail());

        gmailSignInPasswordPage.inputPasswordAndSubmit(var.getGmailPassword());

        assertTrue(gmailInboxPage.getActivateYourGengoEmail().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        gmailInboxPage.clickActivateGengoEmail();

        assertTrue(gmailActivateGengoPage.getActivateAccountBtn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Activate the account
        gmailActivateGengoPage.clickActivateAccount();

        assertTrue(customerOrderFormPage.getOrderFormTextArea().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Go back to Gmail account and click 'Welcome to Gengo' email.
        page.launchUrl(var.getGmailUrl());

        assertTrue(gmailInboxPage.getGmailComposeBtn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        gmailInboxPage.clickWelcomeToGengoEmail();

        assertTrue(gmailWelcomeToGengoPage.getOrderTranslationBtn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        gmailWelcomeToGengoPage.clickOrderTranslation();

        assertTrue(page.getCurrentUrl().equals(customerOrderFormPage.ORDER_FORM_URL),
                var.getWrongUrlErrMsg());

        // Logout and sign in again.
        global.nonAdminSignOut();

        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        pluginPage.passThisPage();

        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        homePage.clickSignInButton();

        assertTrue(page.getCurrentUrl().equals(loginPage.LOGIN_PAGE_URL),
                var.getWrongUrlErrMsg());

        loginPage.loginAccount(this.newUser, var.getDefaultPassword());

        assertTrue(customerDashboardPage.getCustomerDashboardText().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        assertTrue(customerDashboardPage.checkCustomerDashboard(), var.getElementIsNotDisplayedErrMsg());
    }
}
