package com.ndviet.tests.dynamic;

import static com.platform.testframework.annotation.TestMetadata.Severity.CRITICAL;
import static com.platform.testframework.annotation.TestMetadata.Severity.NORMAL;

import com.ndviet.tests.base.BaseTest;
import com.ndviet.tests.pages.AddRemoveElementsPage;
import com.ndviet.tests.pages.DynamicControlsPage;
import com.platform.testframework.annotation.TestMetadata;
import org.ndviet.library.WebUI;
import org.testng.annotations.Test;

@TestMetadata(owner = "automation-team-alpha", feature = "Dynamic Content")
public class DynamicContentTest extends BaseTest {

  private final AddRemoveElementsPage addRemovePage = new AddRemoveElementsPage();
  private final DynamicControlsPage dynamicControlPage = new DynamicControlsPage();

  // -------------------------------------------------------------------------
  // TC013
  // -------------------------------------------------------------------------
  @Test(
      description = "TC013: Clicking 'Add Element' creates a Delete button",
      groups = {"smoke", "dynamic"})
  @TestMetadata(severity = CRITICAL, story = "Add/Remove Elements")
  public void addElement() throws Exception {
    log.step("Navigate and add one element");
    addRemovePage.navigateTo(BASE_URL);
    addRemovePage.addElement();
    log.endStep();

    log.step("Verify a Delete button appeared");
    softly(
        soft -> {
          try {
            soft.assertThat(addRemovePage.isDeleteButtonPresent())
                .as("A Delete button should appear after adding")
                .isTrue();
            soft.assertThat(addRemovePage.getDeleteButtonCount())
                .as("Exactly one Delete button should exist")
                .isEqualTo(1);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC014
  // -------------------------------------------------------------------------
  @Test(
      description = "TC014: After adding an element, deleting it removes the Delete button",
      groups = {"dynamic"})
  @TestMetadata(severity = NORMAL, story = "Add/Remove Elements")
  public void addAndRemoveElement() throws Exception {
    log.step("Navigate, add, then remove element");
    addRemovePage.navigateTo(BASE_URL);
    addRemovePage.addElement();
    addRemovePage.deleteFirstElement();
    log.endStep();

    log.step("Verify no Delete buttons remain");
    softly(
        soft -> {
          try {
            soft.assertThat(addRemovePage.isDeleteButtonPresent())
                .as("No Delete buttons should remain after removal")
                .isFalse();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC015
  // -------------------------------------------------------------------------
  @Test(
      description = "TC015: Dynamic Controls – clicking Remove dismisses the checkbox via AJAX",
      groups = {"smoke", "dynamic"})
  @TestMetadata(severity = CRITICAL, story = "Dynamic Controls")
  public void dynamicControlsRemoveCheckbox() throws Exception {
    log.step("Navigate to Dynamic Controls page");
    dynamicControlPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Verify checkbox is initially present");
    softly(
        soft -> {
          try {
            soft.assertThat(dynamicControlPage.isCheckboxPresent())
                .as("Checkbox should be present on page load")
                .isTrue();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    log.step("Click Remove and wait for checkbox to disappear");
    dynamicControlPage.clickRemoveAddButton();
    dynamicControlPage.waitForCheckboxRemoved();
    log.endStep();

    log.step("Verify checkbox is gone and message shown");
    String msg = dynamicControlPage.getLoadingMessage();
    softly(
        soft -> {
          try {
            soft.assertThat(dynamicControlPage.isCheckboxPresent())
                .as("Checkbox should be removed")
                .isFalse();
            soft.assertThat(msg)
                .as("Confirmation message should appear")
                .containsIgnoringCase("It's gone!");
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC016
  // -------------------------------------------------------------------------
  @Test(
      description = "TC016: Dynamic Controls – clicking Enable makes the input editable",
      groups = {"dynamic"})
  @TestMetadata(severity = NORMAL, story = "Dynamic Controls")
  public void dynamicControlsEnableInput() throws Exception {
    log.step("Navigate to Dynamic Controls page");
    dynamicControlPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Click Enable and wait for input to become active");
    dynamicControlPage.clickEnableDisableButton();
    dynamicControlPage.waitForInputEnabled();
    log.endStep();

    log.step("Verify input is enabled and can receive text");
    softly(
        soft -> {
          try {
            soft.assertThat(dynamicControlPage.isInputEnabled())
                .as("Input should be enabled after clicking Enable")
                .isTrue();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    dynamicControlPage.typeInInput("Hello Dynamic Controls");
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC017
  // -------------------------------------------------------------------------
  @Test(
      description =
          "TC017: Dynamic Loading Example 1 – hidden element appears after Start is clicked",
      groups = {"smoke", "dynamic"})
  @TestMetadata(severity = CRITICAL, story = "Dynamic Loading")
  public void dynamicLoadingHiddenElement() throws Exception {
    log.step("Navigate to Dynamic Loading example 1");
    WebUI.navigateToUrl(BASE_URL + "/dynamic_loading/1");
    WebUI.waitForPageLoaded(10);
    log.endStep();

    log.step("Click Start");
    WebUI.click(
        org.ndviet.library.TestObject.ObjectRepository.findTestObject(
            "Dynamic Loading Page.Start Button"));
    log.endStep();

    log.step("Wait for finish element to appear");
    WebUI.waitForElementVisible(
        org.ndviet.library.TestObject.ObjectRepository.findTestObject(
            "Dynamic Loading Page.Finish Element"),
        20);
    log.endStep();

    log.step("Verify finish element text");
    String text =
        WebUI.getText(
            org.ndviet.library.TestObject.ObjectRepository.findTestObject(
                "Dynamic Loading Page.Finish Element"));
    softly(
        soft ->
            soft.assertThat(text)
                .as("Finish text should say 'Hello World!'")
                .isEqualTo("Hello World!"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC018
  // -------------------------------------------------------------------------
  @Test(
      description = "TC018: Dynamic Loading Example 2 – element rendered after Start is clicked",
      groups = {"dynamic"})
  @TestMetadata(severity = NORMAL, story = "Dynamic Loading")
  public void dynamicLoadingRenderedElement() throws Exception {
    log.step("Navigate to Dynamic Loading example 2");
    WebUI.navigateToUrl(BASE_URL + "/dynamic_loading/2");
    WebUI.waitForPageLoaded(10);
    log.endStep();

    log.step("Click Start");
    WebUI.click(
        org.ndviet.library.TestObject.ObjectRepository.findTestObject(
            "Dynamic Loading Page.Start Button"));
    log.endStep();

    log.step("Wait for element to be rendered");
    WebUI.waitForElementVisible(
        org.ndviet.library.TestObject.ObjectRepository.findTestObject(
            "Dynamic Loading Page.Finish Element"),
        20);
    log.endStep();

    log.step("Verify rendered element text");
    String text =
        WebUI.getText(
            org.ndviet.library.TestObject.ObjectRepository.findTestObject(
                "Dynamic Loading Page.Finish Element"));
    softly(
        soft ->
            soft.assertThat(text)
                .as("Finish text should say 'Hello World!'")
                .isEqualTo("Hello World!"));
    log.endStep();
  }
}
