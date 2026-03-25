package org.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class DynamicControlsPage {

  private static final String PATH = "Dynamic Controls Page.";
  private static final int TIMEOUT = 20;

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/dynamic_controls");
    WebUI.waitForPageLoaded(10);
  }

  public void clickRemoveAddButton() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Remove Add Button"));
  }

  public void waitForCheckboxRemoved() throws Exception {
    WebUI.waitForElementNotPresent(ObjectRepository.findTestObject(PATH + "Checkbox"), TIMEOUT);
  }

  public void waitForCheckboxAdded() throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Checkbox"), TIMEOUT);
  }

  public boolean isCheckboxPresent() throws Exception {
    return WebUI.isElementPresent(ObjectRepository.findTestObject(PATH + "Checkbox"), 5);
  }

  public void clickEnableDisableButton() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Enable Disable Button"));
  }

  public void waitForInputEnabled() throws Exception {
    WebUI.waitForElementClickable(ObjectRepository.findTestObject(PATH + "Input Field"), TIMEOUT);
  }

  public boolean isInputEnabled() throws Exception {
    return WebUI.isElementEnabled(ObjectRepository.findTestObject(PATH + "Input Field"), 5);
  }

  public void typeInInput(String text) throws Exception {
    WebUI.clearAndSetText(ObjectRepository.findTestObject(PATH + "Input Field"), text);
  }

  public String getLoadingMessage() throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Loading Message"), TIMEOUT);
    return WebUI.getText(ObjectRepository.findTestObject(PATH + "Loading Message"));
  }
}
