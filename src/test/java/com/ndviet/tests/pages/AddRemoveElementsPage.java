package com.ndviet.tests.pages;

import java.util.List;
import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;

public class AddRemoveElementsPage {

  private static final String PATH = "Add Remove Elements Page.";

  public void navigateTo(String baseUrl) {
    WebUI.navigateToUrl(baseUrl + "/add_remove_elements/");
    WebUI.waitForPageLoaded(10);
  }

  public void addElement() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Add Element Button"));
  }

  public void deleteFirstElement() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "First Delete Button"));
  }

  public int getDeleteButtonCount() throws Exception {
    List<?> elements =
        WebUI.findWebElements(ObjectRepository.findTestObject(PATH + "Delete Buttons"));
    return elements.size();
  }

  public boolean isDeleteButtonPresent() throws Exception {
    return WebUI.isElementPresent(ObjectRepository.findTestObject(PATH + "First Delete Button"), 5);
  }
}
