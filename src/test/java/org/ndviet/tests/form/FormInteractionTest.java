package org.ndviet.tests.form;

import static com.platform.testframework.annotation.TestMetadata.Severity.CRITICAL;
import static com.platform.testframework.annotation.TestMetadata.Severity.NORMAL;

import org.ndviet.tests.base.BaseTest;
import org.ndviet.tests.pages.CheckboxesPage;
import org.ndviet.tests.pages.DropdownPage;
import com.platform.testframework.annotation.TestMetadata;
import org.testng.annotations.Test;

@TestMetadata(owner = "automation-team-alpha", feature = "Form Interactions")
public class FormInteractionTest extends BaseTest {

  private final CheckboxesPage checkboxesPage = new CheckboxesPage();
  private final DropdownPage dropdownPage = new DropdownPage();

  // -------------------------------------------------------------------------
  // TC004
  // -------------------------------------------------------------------------
  @Test(
      description =
          "TC004: Verify checkboxes initial state – checkbox1 unchecked, checkbox2 checked",
      groups = {"smoke", "form"})
  @TestMetadata(severity = CRITICAL, story = "Checkboxes")
  public void verifyCheckboxInitialStates() throws Exception {
    log.step("Navigate to checkboxes page");
    checkboxesPage.navigateTo(BASE_URL);
    log.endStep();

    log.step("Verify initial states");
    softly(
        soft -> {
          try {
            soft.assertThat(checkboxesPage.isCheckbox1Checked())
                .as("Checkbox 1 should be unchecked by default")
                .isFalse();
            soft.assertThat(checkboxesPage.isCheckbox2Checked())
                .as("Checkbox 2 should be checked by default")
                .isTrue();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC005
  // -------------------------------------------------------------------------
  @Test(
      description = "TC005: Check the first checkbox and verify it becomes checked",
      groups = {"form"})
  @TestMetadata(severity = NORMAL, story = "Checkboxes")
  public void checkFirstCheckbox() throws Exception {
    log.step("Navigate and check checkbox 1");
    checkboxesPage.navigateTo(BASE_URL);
    checkboxesPage.checkCheckbox1();
    log.endStep();

    log.step("Verify checkbox 1 is now checked");
    softly(
        soft -> {
          try {
            soft.assertThat(checkboxesPage.isCheckbox1Checked())
                .as("Checkbox 1 should be checked after interaction")
                .isTrue();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC006
  // -------------------------------------------------------------------------
  @Test(
      description = "TC006: Uncheck the second checkbox and verify it becomes unchecked",
      groups = {"form"})
  @TestMetadata(severity = NORMAL, story = "Checkboxes")
  public void uncheckSecondCheckbox() throws Exception {
    log.step("Navigate and uncheck checkbox 2");
    checkboxesPage.navigateTo(BASE_URL);
    checkboxesPage.uncheckCheckbox2();
    log.endStep();

    log.step("Verify checkbox 2 is now unchecked");
    softly(
        soft -> {
          try {
            soft.assertThat(checkboxesPage.isCheckbox2Checked())
                .as("Checkbox 2 should be unchecked after interaction")
                .isFalse();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC007
  // -------------------------------------------------------------------------
  @Test(
      description = "TC007: Select 'Option 1' from the dropdown",
      groups = {"smoke", "form"})
  @TestMetadata(severity = CRITICAL, story = "Dropdown")
  public void selectDropdownOptionOne() throws Exception {
    log.step("Navigate to dropdown page and select Option 1");
    dropdownPage.navigateTo(BASE_URL);
    dropdownPage.selectByText("Option 1");
    log.endStep();

    log.step("Verify Option 1 is selected");
    String selected = dropdownPage.getSelectedOptionText();
    softly(
        soft ->
            soft.assertThat(selected)
                .as("Selected option should be 'Option 1'")
                .isEqualTo("Option 1"));
    log.endStep();
  }

  // -------------------------------------------------------------------------
  // TC008
  // -------------------------------------------------------------------------
  @Test(
      description = "TC008: Select 'Option 2' from the dropdown by index",
      groups = {"form"})
  @TestMetadata(severity = NORMAL, story = "Dropdown")
  public void selectDropdownOptionTwo() throws Exception {
    log.step("Navigate to dropdown page and select Option 2 by index");
    dropdownPage.navigateTo(BASE_URL);
    dropdownPage.selectByIndex(2);
    log.endStep();

    log.step("Verify Option 2 is selected");
    String selected = dropdownPage.getSelectedOptionText();
    softly(
        soft ->
            soft.assertThat(selected)
                .as("Selected option should be 'Option 2'")
                .isEqualTo("Option 2"));
    log.endStep();
  }
}
