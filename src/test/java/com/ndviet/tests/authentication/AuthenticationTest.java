package com.ndviet.tests.authentication;

import static com.platform.testframework.annotation.TestMetadata.Severity.BLOCKER;
import static com.platform.testframework.annotation.TestMetadata.Severity.CRITICAL;
import static com.platform.testframework.annotation.TestMetadata.Severity.NORMAL;

import com.ndviet.tests.base.BaseTest;
import com.ndviet.tests.pages.LoginPage;
import com.platform.testframework.annotation.TestMetadata;
import org.ndviet.library.WebUI;
import org.testng.annotations.Test;

@TestMetadata(owner = "automation-team-alpha", feature = "Authentication")
public class AuthenticationTest extends BaseTest {

  private final LoginPage loginPage = new LoginPage();

  // -------------------------------------------------------------------------
  // TC001
  // -------------------------------------------------------------------------
  @Test(
      description = "TC001: Valid credentials grant access and show success flash",
      groups = {"smoke", "authentication"})
  @TestMetadata(severity = BLOCKER, story = "Login")
  public void loginWithValidCredentials() throws Exception {
    log.step("Navigate to login page");
    loginPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Submit valid credentials");
    loginPage.login("tomsmith", "SuperSecretPassword!");
    log.endStep();

    log.step("Verify success flash message");
    String flash = loginPage.getFlashMessage();
    softly(
        soft ->
            soft.assertThat(flash)
                .as("Flash message should indicate successful login")
                .contains("You logged into a secure area!"));
    env("flash.message", flash);
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC002
  // -------------------------------------------------------------------------
  @Test(
      description = "TC002: Invalid credentials show an error flash message",
      groups = {"authentication"})
  @TestMetadata(severity = CRITICAL, story = "Login")
  public void loginWithInvalidCredentials() throws Exception {
    log.step("Navigate to login page");
    loginPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Submit invalid credentials");
    loginPage.login("invalidUser", "wrongPassword");
    log.endStep();

    log.step("Verify error flash message");
    String flash = loginPage.getFlashMessage();
    softly(
        soft ->
            soft.assertThat(flash)
                .as("Flash message should indicate invalid credentials")
                .contains("Your username is invalid!"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC003
  // -------------------------------------------------------------------------
  @Test(
      description = "TC003: After successful login, user can logout and is redirected",
      groups = {"smoke", "authentication"})
  @TestMetadata(severity = NORMAL, story = "Logout")
  public void logoutAfterSuccessfulLogin() throws Exception {
    log.step("Login with valid credentials");
    loginPage.navigateTo(BASE_URL);
    loginPage.login("tomsmith", "SuperSecretPassword!");
    log.endStep();

    log.step("Logout");
    loginPage.logout();
    log.endStep();

    log.step("Verify redirect to login page");
    softly(
        soft ->
            soft.assertThat(WebUI.getCurrentUrl())
                .as("URL after logout should contain /login")
                .contains("/login"));
    log.endStep();
  }
}
