package org.ndviet.tests.navigation;

import static com.platform.testframework.annotation.TestMetadata.Severity.CRITICAL;
import static com.platform.testframework.annotation.TestMetadata.Severity.NORMAL;

import org.ndviet.tests.base.BaseTest;
import org.ndviet.tests.pages.HoversPage;
import com.platform.testframework.annotation.TestMetadata;
import org.ndviet.library.TestObject.ObjectRepository;
import org.ndviet.library.WebUI;
import org.testng.annotations.Test;

@TestMetadata(owner = "automation-team-alpha", feature = "Navigation and Windows")
public class NavigationAndWindowsTest extends BaseTest {

  private final HoversPage hoversPage = new HoversPage();

  // -------------------------------------------------------------------------
  // TC019
  // -------------------------------------------------------------------------
  @Test(
      description = "TC019: Clicking 'Click Here' opens a new browser window",
      groups = {"smoke", "navigation"})
  @TestMetadata(severity = CRITICAL, story = "Multiple Windows")
  public void openNewWindowAndVerifyTitle() throws Exception {
    log.step("Navigate to Multiple Windows page");
    WebUI.navigateToUrl(BASE_URL + "/windows");
    WebUI.waitForPageLoaded(10);
    log.endStep();

    log.step("Click link to open new window");
    WebUI.click(ObjectRepository.findTestObject("Multiple Windows Page.Click Here Link"));
    WebUI.waitForNumberOfWindowsToBe(2, 10);
    log.endStep();

    log.step("Switch to new window and verify title");
    WebUI.switchToLatestWindow();
    WebUI.waitForPageLoaded(10);
    String title = WebUI.getTitle();
    softly(
        soft ->
            soft.assertThat(title)
                .as("New window title should contain 'New Window'")
                .containsIgnoringCase("New Window"));
    log.endStep();

    log.step("Close new window and switch back");
    WebUI.closeCurrentWindow();
    WebUI.switchToWindowByIndex(0);
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC020
  // -------------------------------------------------------------------------
  @Test(
      description = "TC020: After opening a new window, the original window is still accessible",
      groups = {"navigation"})
  @TestMetadata(severity = NORMAL, story = "Multiple Windows")
  public void switchBetweenWindows() throws Exception {
    log.step("Navigate to Multiple Windows page");
    WebUI.navigateToUrl(BASE_URL + "/windows");
    WebUI.waitForPageLoaded(10);
    String originalHandle = WebUI.getCurrentWindowHandle();
    log.endStep();

    log.step("Open new window");
    WebUI.click(ObjectRepository.findTestObject("Multiple Windows Page.Click Here Link"));
    WebUI.waitForNumberOfWindowsToBe(2, 10);
    log.endStep();

    log.step("Switch back to original window and verify URL");
    WebUI.switchToWindowByHandle(originalHandle);
    softly(
        soft ->
            soft.assertThat(WebUI.getCurrentUrl())
                .as("Original window URL should contain /windows")
                .contains("/windows"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC021
  // -------------------------------------------------------------------------
  @Test(
      description = "TC021: Hovering over the first user figure reveals a caption",
      groups = {"smoke", "navigation"})
  @TestMetadata(severity = CRITICAL, story = "Hovers")
  public void hoverOverFirstFigureShowsCaption() throws Exception {
    log.step("Navigate to Hovers page");
    hoversPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Hover over figure 1");
    hoversPage.hoverOverFigure(1);
    log.endStep();

    log.step("Verify caption 1 is visible and contains user info");
    String caption = hoversPage.getCaptionText(1);
    softly(
        soft ->
            soft.assertThat(caption)
                .as("Caption should contain user name or link")
                .containsIgnoringCase("user"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC022
  // -------------------------------------------------------------------------
  @Test(
      description = "TC022: Hovering over the second user figure reveals a different caption",
      groups = {"navigation"})
  @TestMetadata(severity = NORMAL, story = "Hovers")
  public void hoverOverSecondFigureShowsCaption() throws Exception {
    log.step("Navigate to Hovers page");
    hoversPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Hover over figure 2");
    hoversPage.hoverOverFigure(2);
    log.endStep();

    log.step("Verify caption 2 is visible");
    softly(
        soft -> {
          try {
            soft.assertThat(hoversPage.isCaptionVisible(2))
                .as("Caption 2 should be visible after hover")
                .isTrue();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC023
  // -------------------------------------------------------------------------
  @Test(
      description = "TC023: The TinyMCE iframe editor is present and accessible",
      groups = {"smoke", "navigation"})
  @TestMetadata(severity = CRITICAL, story = "iFrame")
  public void iframeEditorIsAccessible() throws Exception {
    log.step("Navigate to iFrame page");
    WebUI.navigateToUrl(BASE_URL + "/iframe");
    WebUI.waitForPageLoaded(15);
    log.endStep();

    log.step("Verify the TinyMCE iframe is present");
    WebUI.verifyElementPresent(ObjectRepository.findTestObject("Iframe Page.TinyMCE Frame"));
    log.endStep();

    log.step("Verify iframe is accessible and editor body is present inside it");
    WebUI.switchToFrame(ObjectRepository.findTestObject("Iframe Page.TinyMCE Frame"));
    WebUI.waitForElementPresent(ObjectRepository.findTestObject("Iframe Page.Editor Body"), 10);
    WebUI.switchToDefaultContent();
    log.endStep();

    log.step("Set content via TinyMCE JavaScript API and verify");
    // TinyMCE's editor body is a contenteditable div — clear()/sendKeys() is not supported.
    // Use the TinyMCE JS API to set and read content from the default-content context.
    WebUI.executeJavaScript("tinyMCE.activeEditor.setContent('<p>Automation test input</p>');");
    Object content = WebUI.executeJavaScript("return tinyMCE.activeEditor.getContent();");
    softly(
        soft ->
            soft.assertThat(String.valueOf(content))
                .as("TinyMCE content should contain the typed text")
                .contains("Automation test input"));
    log.endStep();
  }
}
