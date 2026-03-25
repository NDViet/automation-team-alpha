package org.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class DropdownPage {

  private static final String PATH = "Dropdown Page.";

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/dropdown");
    WebUI.waitForPageLoaded(10);
  }

  public void selectByText(String optionText) throws Exception {
    WebUI.selectOptionByText(ObjectRepository.findTestObject(PATH + "Dropdown"), optionText, false);
  }

  public void selectByIndex(int index) throws Exception {
    WebUI.selectOptionByIndex(ObjectRepository.findTestObject(PATH + "Dropdown"), index);
  }

  public String getSelectedOptionText() throws Exception {
    return WebUI.getSelectedOptionText(ObjectRepository.findTestObject(PATH + "Dropdown"));
  }
}
