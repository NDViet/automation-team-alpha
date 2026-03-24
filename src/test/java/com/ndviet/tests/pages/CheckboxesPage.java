package com.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class CheckboxesPage {

  private static final String PATH = "Checkboxes Page.";

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/checkboxes");
    WebUI.waitForPageLoaded(10);
  }

  public boolean isCheckbox1Checked() throws Exception {
    return WebUI.isChecked(ObjectRepository.findTestObject(PATH + "Checkbox 1"));
  }

  public boolean isCheckbox2Checked() throws Exception {
    return WebUI.isChecked(ObjectRepository.findTestObject(PATH + "Checkbox 2"));
  }

  public void checkCheckbox1() throws Exception {
    WebUI.check(ObjectRepository.findTestObject(PATH + "Checkbox 1"));
  }

  public void uncheckCheckbox2() throws Exception {
    WebUI.uncheck(ObjectRepository.findTestObject(PATH + "Checkbox 2"));
  }
}
