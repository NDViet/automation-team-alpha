package org.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class JSAlertsPage {

  private static final String PATH = "JS Alerts Page.";
  private static final int ALERT_TIMEOUT = 5;

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/javascript_alerts");
    WebUI.waitForPageLoaded(10);
  }

  public void triggerAlertAndAccept() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Alert Button"));
    WebUI.acceptAlert(ALERT_TIMEOUT);
  }

  public void triggerConfirmAndDismiss() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Confirm Button"));
    WebUI.dismissAlert(ALERT_TIMEOUT);
  }

  public void triggerConfirmAndAccept() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Confirm Button"));
    WebUI.acceptAlert(ALERT_TIMEOUT);
  }

  public void triggerPromptEnterTextAndAccept(String text) throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Prompt Button"));
    WebUI.setAlertText(text, ALERT_TIMEOUT);
    WebUI.acceptAlert(ALERT_TIMEOUT);
  }

  public String getResultText() throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Result Text"), 5);
    return WebUI.getText(ObjectRepository.findTestObject(PATH + "Result Text"));
  }
}
