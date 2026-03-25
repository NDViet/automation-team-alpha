package org.ndviet.tests.base;

import com.platform.testframework.testng.PlatformTestNGBase;
import java.lang.reflect.Method;
import org.ndviet.library.WebUI;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest extends PlatformTestNGBase {

  protected static final String BASE_URL =
      System.getProperty("APP_URL", "https://the-internet.herokuapp.com");

  @BeforeMethod(alwaysRun = true)
  public void openBrowser(Method method, ITestContext context) {
    String browser = context.getCurrentXmlTest().getParameter("browser");
    if (browser == null || browser.isBlank()) browser = "chrome";
    log.step("Open browser [" + browser + "] | " + method.getName());
    WebUI.openBrowser(browser, BASE_URL);
    WebUI.maximizeWindow();
    log.endStep();
  }

  @AfterMethod(alwaysRun = true)
  public void closeBrowser() {
    log.step("Close browser");
    WebUI.closeBrowser();
    log.endStep();
  }
}
