package org.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class LoginPage {

  private static final String PATH = "Login Page.";

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/login");
  }

  public void login(String username, String password) throws Exception {
    WebUI.clearAndSetText(ObjectRepository.findTestObject(PATH + "Username"), username);
    WebUI.clearAndSetText(ObjectRepository.findTestObject(PATH + "Password"), password);
    WebUI.click(ObjectRepository.findTestObject(PATH + "Login Button"));
    WebUI.waitForPageLoaded(15);
  }

  public String getFlashMessage() throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Flash Message"), 10);
    return WebUI.getText(ObjectRepository.findTestObject(PATH + "Flash Message"));
  }

  public void logout() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Logout Button"));
    WebUI.waitForPageLoaded(10);
  }
}
