package org.ndviet.tests.alerts;

import static com.platform.testframework.annotation.TestMetadata.Severity.CRITICAL;
import static com.platform.testframework.annotation.TestMetadata.Severity.NORMAL;

import org.ndviet.tests.base.BaseTest;
import org.ndviet.tests.pages.JSAlertsPage;
import com.platform.testframework.annotation.TestMetadata;
import org.testng.annotations.Test;

@TestMetadata(owner = "automation-team-alpha", feature = "Alerts and Popups")
public class AlertsAndPopupsTest extends BaseTest {

  private final JSAlertsPage alertsPage = new JSAlertsPage();

  // -------------------------------------------------------------------------
  // TC009
  // -------------------------------------------------------------------------
  @Test(
      description = "TC009: Trigger JS Alert, accept it, and verify result message",
      groups = {"smoke", "alerts"})
  @TestMetadata(severity = CRITICAL, story = "JS Alert")
  public void acceptJsAlert() throws Exception {
    log.step("Navigate to JS Alerts page");
    alertsPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Trigger alert and accept");
    alertsPage.triggerAlertAndAccept();
    log.endStep();

    log.step("Verify result text indicates alert accepted");
    String result = alertsPage.getResultText();
    softly(
        soft ->
            soft.assertThat(result)
                .as("Result should confirm JS Alert was accepted")
                .contains("You successfuly clicked an alert"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC010
  // -------------------------------------------------------------------------
  @Test(
      description = "TC010: Trigger JS Confirm, dismiss it, and verify result shows 'Cancel'",
      groups = {"alerts"})
  @TestMetadata(severity = NORMAL, story = "JS Confirm")
  public void dismissJsConfirm() throws Exception {
    log.step("Navigate to JS Alerts page");
    alertsPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Trigger confirm and dismiss");
    alertsPage.triggerConfirmAndDismiss();
    log.endStep();

    log.step("Verify result shows Cancel");
    String result = alertsPage.getResultText();
    softly(
        soft ->
            soft.assertThat(result)
                .as("Result should show that confirm was cancelled")
                .contains("You clicked: Cancel"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC011
  // -------------------------------------------------------------------------
  @Test(
      description = "TC011: Trigger JS Confirm, accept it, and verify result shows 'Ok'",
      groups = {"alerts"})
  @TestMetadata(severity = NORMAL, story = "JS Confirm")
  public void acceptJsConfirm() throws Exception {
    log.step("Navigate to JS Alerts page");
    alertsPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Trigger confirm and accept");
    alertsPage.triggerConfirmAndAccept();
    log.endStep();

    log.step("Verify result shows Ok");
    String result = alertsPage.getResultText();
    softly(
        soft ->
            soft.assertThat(result)
                .as("Result should show that confirm was accepted")
                .contains("You clicked: Ok"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC012
  // -------------------------------------------------------------------------
  @Test(
      description = "TC012: Trigger JS Prompt, enter text, accept, and verify result",
      groups = {"alerts"})
  @TestMetadata(severity = NORMAL, story = "JS Prompt")
  public void submitJsPrompt() throws Exception {
    String promptInput = "Automation Test Input";

    log.step("Navigate to JS Alerts page");
    alertsPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Trigger prompt, enter text, and accept");
    alertsPage.triggerPromptEnterTextAndAccept(promptInput);
    log.endStep();

    log.step("Verify result contains entered text");
    String result = alertsPage.getResultText();
    softly(
        soft ->
            soft.assertThat(result)
                .as("Result should echo the entered prompt text")
                .contains(promptInput));
    log.endStep();
  }
}
