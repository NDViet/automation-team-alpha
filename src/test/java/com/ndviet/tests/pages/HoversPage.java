package com.ndviet.tests.pages;

import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class HoversPage {

  private static final String PATH = "Hovers Page.";

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/hovers");
    WebUI.waitForPageLoaded(10);
  }

  public void hoverOverFigure(int index) throws Exception {
    WebUI.moveToElement(ObjectRepository.findTestObject(PATH + "Figure " + index));
  }

  public String getCaptionText(int index) throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Caption " + index), 5);
    return WebUI.getText(ObjectRepository.findTestObject(PATH + "Caption " + index));
  }

  public boolean isCaptionVisible(int index) throws Exception {
    return WebUI.isElementVisible(ObjectRepository.findTestObject(PATH + "Caption " + index), 5);
  }
}
