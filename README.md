# automation-team-alpha

End-to-end UI automation suite for [The Internet](https://the-internet.herokuapp.com) — a public Selenium practice site.

| | |
|---|---|
| **Tech stack** | Java 21 · Gradle · TestNG · Selenium 4 |
| **Framework** | [test-automation-fwk](https://github.com/NDViet/test-automation-fwk) (`WebUI` facade · Page Object · `ObjectRepository`) |
| **Platform** | [test-automation-platform](https://github.com/NDViet/test-automation-platform) (`PlatformTestNGBase` · structured logging) |
| **Browsers** | Chrome · Edge · Firefox (parallel, all headless) |
| **Execution** | Local JVM · Docker container · Selenium Grid · GitHub Actions CI |

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project structure](#project-structure)
3. [Setup](#setup)
4. [Running tests](#running-tests)
5. [Cross-browser parallel execution](#cross-browser-parallel-execution)
6. [Code formatting](#code-formatting)
7. [Docker / container execution](#docker--container-execution)
8. [Selenium Grid (Docker Compose)](#selenium-grid-docker-compose)
9. [CI — GitHub Actions](#ci--github-actions)
10. [Writing new tests](#writing-new-tests)
11. [Configuration reference](#configuration-reference)

---

## Prerequisites

| Tool | Minimum version | Notes |
|------|----------------|-------|
| JDK | 21 | `JAVA_HOME` must be set |
| Chrome | latest stable | ChromeDriver managed automatically by Selenium 4 |
| Edge | latest stable | EdgeDriver managed automatically |
| Firefox | latest stable | GeckoDriver managed automatically |
| Docker | 24+ | Only needed for container execution |
| GitHub PAT | — | `read:packages` scope — needed to pull framework JARs |

> **Local framework JARs (optional):** If you work on `test-automation-fwk` or `test-automation-platform` locally, install them first so Gradle picks up your changes:
> ```bash
> cd /path/to/test-automation-fwk   && mvn install -DskipTests
> cd /path/to/test-automation-platform && mvn install -DskipTests
> ```

---

## Project structure

```
automation-team-alpha/
├── build.gradle                          # Gradle build — plugins, deps, test task
├── gradle.properties                     # JVM / Gradle tuning; GPR credential stubs
├── settings.gradle
│
├── containers/
│   └── Dockerfile                        # Test-runner image (extends ndviet/test-automation-java-common)
│
├── .github/workflows/
│   └── run-tests.yml                     # CI pipeline (build image → run tests → publish reports)
│
└── src/test/
    ├── java/com/ndviet/tests/
    │   ├── base/
    │   │   └── BaseTest.java             # @BeforeMethod/@AfterMethod — opens/closes browser per test
    │   ├── pages/                        # Page Object classes (one per AUT page)
    │   │   ├── LoginPage.java
    │   │   ├── CheckboxesPage.java
    │   │   ├── DropdownPage.java
    │   │   ├── JSAlertsPage.java
    │   │   ├── DynamicControlsPage.java
    │   │   ├── AddRemoveElementsPage.java
    │   │   └── HoversPage.java
    │   ├── authentication/
    │   │   └── AuthenticationTest.java   # TC001–TC003
    │   ├── form/
    │   │   └── FormInteractionTest.java  # TC004–TC008
    │   ├── alerts/
    │   │   └── AlertsAndPopupsTest.java  # TC009–TC012
    │   ├── dynamic/
    │   │   └── DynamicContentTest.java   # TC013–TC018
    │   └── navigation/
    │       └── NavigationAndWindowsTest.java  # TC019–TC023
    │
    └── resources/
        ├── configuration.properties      # Points ConfigurationManager at configuration.yaml
        ├── configuration.yaml            # Selenium settings (browser args, timeouts, …)
        ├── testng.xml                    # Suite — 15 test blocks × 3 browsers, parallel="tests"
        └── webElementIdentifiers/        # YAML locator files — one per page
            ├── LoginPage.yaml
            ├── CheckboxesPage.yaml
            └── …
```

---

## Setup

### 1. Clone

```bash
git clone <repo-url>
cd automation-team-alpha
```

### 2. Configure GitHub Packages credentials

The framework JARs are hosted on GitHub Packages. Create `~/.gradle/gradle.properties` (never commit this file):

```properties
gpr.user=<your-github-username>
gpr.token=<PAT-with-read:packages>
```

Alternatively export environment variables before running Gradle:

```bash
export GITHUB_ACTOR=<your-github-username>
export GITHUB_TOKEN=<your-PAT>
```

### 3. Verify the setup

```bash
./gradlew dependencies --configuration testRuntimeClasspath
```

A successful dependency resolution confirms credentials and network access are correct.

---

## Running tests

### Full suite (all browsers, parallel)

```bash
./gradlew test
```

### Single browser override

```bash
./gradlew test -Dbrowser=firefox
./gradlew test -Dbrowser=edge
./gradlew test -Dbrowser=chrome
```

> The `-Dbrowser` flag is forwarded as a system property; it acts as the **fallback** browser when a test block has no explicit `<parameter name="browser">` in `testng.xml`. In the default cross-browser suite every block already carries its own browser parameter.

### Custom AUT URL

```bash
./gradlew test -DAPP_URL=https://staging.example.com
```

### Specific TestNG suite

```bash
./gradlew test --tests "*AuthenticationTest*"
```

Or point to a different XML suite:

```groovy
// temporarily in build.gradle
useTestNG { suites 'src/test/resources/single-browser.xml' }
```

### Test reports

After the run, open:

```
build/reports/tests/test/index.html   # Gradle HTML report
build/test-results/test/              # JUnit-compatible XML (for CI parsers)
```

---

## Cross-browser parallel execution

`testng.xml` defines **15 `<test>` blocks** (5 feature groups × 3 browsers) with `parallel="tests" thread-count="3"`.

```
Chrome ──┐
Edge   ──┼──► 3 threads run simultaneously, each with its own WebDriver instance
Firefox──┘
```

Each `<test>` block passes `<parameter name="browser" value="…"/>`. `BaseTest.openBrowser()` reads this via `ITestContext` and calls `WebUI.openBrowser(browser, baseUrl)`.

`DriverManager` uses `ThreadLocal<WebDriver>`, so threads never share a driver.

**To disable parallel execution** (e.g. for debugging):

```xml
<!-- testng.xml -->
<suite … parallel="none">
```

---

## Code formatting

The project uses [Spotless](https://github.com/diffplug/spotless) with `google-java-format`.

```bash
./gradlew spotlessApply   # reformat all Java files in-place
./gradlew spotlessCheck   # verify formatting — fails if any file is unformatted
```

Add `spotlessCheck` to your pre-commit or CI step to keep the codebase consistently formatted.

---

## Docker / container execution

The `containers/Dockerfile` extends `ndviet/test-automation-java-common:latest` (which pre-installs browsers and a seeded `~/.m2`).

All framework JARs and compiled test sources are baked into the image at build time. **No GitHub credentials are required to run the image** — only to build it.

### Build the image

GitHub credentials are needed once, during `docker build`, to download the framework JARs from GitHub Packages:

```bash
docker build \
  --build-arg GITHUB_ACTOR=$GITHUB_ACTOR \
  --build-arg GITHUB_TOKEN=$GITHUB_TOKEN \
  -t automation-team-alpha:local \
  -f containers/Dockerfile .
```

### Run tests in the container

No credentials needed — everything is already baked in:

```bash
# Full cross-browser suite
docker run --rm \
  -v "$(pwd)/build:/workspace/build" \
  automation-team-alpha:local

# Single browser
docker run --rm \
  -v "$(pwd)/build:/workspace/build" \
  automation-team-alpha:local \
  ./gradlew test --no-daemon -Dbrowser=firefox

# Override target URL
docker run --rm \
  -e APP_URL=https://staging.example.com \
  -v "$(pwd)/build:/workspace/build" \
  automation-team-alpha:local
```

Reports are written to `./build/` on the host via the volume mount.

---

## Selenium Grid (Docker Compose)

`docker-compose.yml` starts a Selenium Grid 4 hub with Chrome, Firefox, and Edge nodes. All tests use `RemoteWebDriver` — no local browser installation required.

### Start the grid

```bash
docker compose up -d
```

Grid console: http://localhost:4444

### Run tests from the local JVM against the grid

```bash
# Default suite (thread-count=3, one browser at a time)
./gradlew test \
  -Dselenium.web_driver.target=REMOTE \
  -Dselenium.hub.url=http://localhost:4444/wd/hub

# Grid-optimised suite (thread-count=15, all 15 blocks in parallel)
./gradlew test \
  -Dselenium.web_driver.target=REMOTE \
  -Dselenium.hub.url=http://localhost:4444/wd/hub \
  -Dsuite=testng-grid.xml
```

`selenium.hub.url` defaults to `http://localhost:4444/wd/hub` (set in `configuration.yaml`) and can be omitted when the grid is running locally.

### Run tests in a container against the grid

Uses the `test` profile — waits for all three browser nodes to be healthy before starting:

```bash
# Build the test-runner image first (if not already done)
docker build \
  --build-arg GITHUB_ACTOR=$GITHUB_ACTOR \
  --build-arg GITHUB_TOKEN=$GITHUB_TOKEN \
  -t automation-team-alpha:local \
  -f containers/Dockerfile .

# Start the grid + run tests (exits when tests finish)
docker compose --profile test up \
  --abort-on-container-exit \
  --exit-code-from test-runner

# Use the GHCR image instead of a local build
TEST_RUNNER_IMAGE=ghcr.io/<your-repo>:latest \
  docker compose --profile test up \
  --abort-on-container-exit \
  --exit-code-from test-runner

# Override the target URL
APP_URL=https://staging.example.com \
  docker compose --profile test up \
  --abort-on-container-exit \
  --exit-code-from test-runner
```

### Tear down

```bash
docker compose down
```

---

## CI — GitHub Actions

The workflow at `.github/workflows/run-tests.yml` runs on:

| Trigger | Behaviour |
|---------|-----------|
| Push / PR to `main` or `develop` | Full suite |
| Schedule (`0 2 * * *` UTC) | Nightly full suite |
| `workflow_dispatch` | Manual run — choose browser, suite file, AUT URL |

**Jobs:**

1. **Build Test Runner Image** — `docker build` using `GITHUB_TOKEN` for GPR access; image saved as a workflow artifact.
2. **Execute Tests** — loads the image, mounts `./build`, runs `./gradlew test`; uploads HTML and XML reports as artifacts.

Reports are available in **Actions → run → Artifacts** and as a summary via `dorny/test-reporter`.

---

## Performance tests (K6)

K6 tests live in `performance/` and target [The Internet](https://the-internet.herokuapp.com). Results feed two complementary layers of the platform.

```
performance/
├── k6/
│   ├── homepage-load.js       # Landing page + key nav pages under ramping load
│   ├── login-flow.js          # Valid + invalid auth under load
│   └── form-interactions.js   # Dropdown, dynamic controls, dynamic loading
├── package.json               # @ndviet/adapter-k6 dependency + esbuild bundle script
├── .npmrc                     # GitHub Packages registry config for @ndviet scope
├── run-all.sh                 # Build (if needed) → run tests → stream to InfluxDB
└── .gitignore                 # Excludes results/, node_modules/, dist/
```

### Two-layer observability

| Layer | Tool | What it shows |
|-------|------|---------------|
| **Real-time drill-down** | InfluxDB → Grafana (`:3000`, dashboard `K6 Real-time Metrics`) | VUs over time, p50/p90/p95 time-series, error rate, TTFB, req/s — streamed live via `--out influxdb` |
| **Summary / history** | Platform ingestion (`:8081`, dashboard `K6 Performance`) | Pass/fail per check aggregated across runs — parsed by `K6JsonParser` from `handleSummary` JSON |

Both dashboards are provisioned automatically in the platform's Grafana. Results are filterable by **team**, **project**, and **environment**.

### Prerequisites

| Tool | Version |
|------|---------|
| [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) | ≥ 0.38.0 |
| Docker | 24+ (for the platform stack, optional) |

### Start InfluxDB + Grafana

InfluxDB is part of the shared platform stack — no separate compose file needed:

```bash
cd /path/to/test-automation-platform
docker compose up -d
# Grafana  → http://localhost:3000  (admin / admin)
# InfluxDB → http://localhost:8086
```

### Run tests

```bash
# Full suite — auto-streams to InfluxDB if reachable, publishes summary to platform
bash performance/run-all.sh

# Single test with live streaming
k6 run --out influxdb=http://localhost:8086/k6 performance/k6/homepage-load.js

# Override AUT URL or tagging
BASE_URL=https://staging.example.com \
K6_ENV=staging \
bash performance/run-all.sh
```

`run-all.sh` auto-detects InfluxDB via `/ping`. If not reachable, tests still execute and publish the summary — streaming is skipped gracefully.

### How results reach the platform

Each K6 test imports `@ndviet/adapter-k6` by package name and calls `publishToPlatform(data)` inside `handleSummary`. Before running, `run-all.sh` checks for a `dist/` directory and if absent runs `npm install && npm run build`, which uses esbuild to bundle each K6 script with the adapter inlined. K6 then runs the bundled `dist/` files — no npm resolution at k6 runtime.

The adapter reads `PLATFORM_URL`, `PLATFORM_API_KEY`, `PLATFORM_SUITE_NAME`, etc. from `__ENV` (passed by `run-all.sh`). If either credential is absent it skips silently.

**First run / adapter upgrade:**

```bash
cd performance
export NODE_AUTH_TOKEN=<github-pat-with-read:packages>
npm install @ndviet/adapter-k6@<version>   # or just: npm install
# dist/ is rebuilt automatically by run-all.sh on next run
```

To force a rebuild without running tests: `npm run build` inside `performance/`.

K6 metrics streamed to InfluxDB carry `team`, `project`, and `environment` tags so the real-time dashboard can filter per project/team without mixing runs.

| Env var | Default | Purpose |
|---------|---------|---------|
| `INFLUXDB_URL` | `http://localhost:8086` | InfluxDB endpoint; empty string skips streaming |
| `K6_TEAM` | `automation-team-alpha` | Tag written to every InfluxDB metric |
| `K6_PROJECT` | `the-internet` | Tag written to every InfluxDB metric |
| `K6_ENV` | `local` | Tag written to every InfluxDB metric |
| `PLATFORM_URL` | `http://localhost:8081` | Platform ingestion base URL |
| `PLATFORM_API_KEY` | `local-dev` | Ingestion API key |
| `PLATFORM_TEAM_ID` | `automation-team-alpha` | Team for ingestion routing |
| `PLATFORM_PROJECT_ID` | `the-internet` | Project for ingestion routing |
| `TEST_ENV` | `local` | Environment label on ingested runs |

### In CI

The `performance-tests` job in `run-tests.yml` installs k6, runs `run-all.sh`, and uploads `performance/results/` as an artifact.

Set the `INFLUXDB_URL` repository secret to point to a central InfluxDB instance to enable real-time metric streaming from CI. Leave it unset to run in summary-only mode (platform ingestion still runs).

---

## Writing new tests

### 1. Add a locator YAML

Create (or extend) `src/test/resources/webElementIdentifiers/<PageName>.yaml`:

```yaml
My Page:
  Submit Button: cssSelector=button[type='submit']
  Result Text:   id=result-message
```

Keys use nested YAML; the Java lookup path uses `.` as a separator — `"My Page.Submit Button"`.

### 2. Create a Page Object

```java
// src/test/java/com/ndviet/tests/pages/MyPage.java
public class MyPage {

  private static final String PATH = "My Page.";

  public void clickSubmit() throws Exception {
    WebUI.click(ObjectRepository.findTestObject(PATH + "Submit Button"));
  }

  public String getResult() throws Exception {
    WebUI.waitForElementVisible(ObjectRepository.findTestObject(PATH + "Result Text"), 10);
    return WebUI.getText(ObjectRepository.findTestObject(PATH + "Result Text"));
  }
}
```

### 3. Write the test class

```java
// src/test/java/com/ndviet/tests/myfeature/MyFeatureTest.java
@TestMetadata(owner = "automation-team-alpha", feature = "My Feature")
public class MyFeatureTest extends BaseTest {

  private final MyPage myPage = new MyPage();

  @Test(description = "TC0XX: …", groups = {"smoke", "myfeature"})
  @TestMetadata(severity = CRITICAL, story = "My Story")
  public void myTest() throws Exception {
    log.step("Navigate");
    WebUI.navigateToUrl(BASE_URL + "/my-path");
    log.endStep();

    log.step("Interact");
    myPage.clickSubmit();
    log.endStep();

    log.step("Assert");
    softly(soft -> soft.assertThat(myPage.getResult()).contains("expected"));
    log.endStep();
  }
}
```

### 4. Register in testng.xml

Add a `<test>` block for each browser inside `testng.xml`:

```xml
<test name="Chrome – My Feature Tests">
    <parameter name="browser" value="chrome"/>
    <classes><class name="org.ndviet.tests.myfeature.MyFeatureTest"/></classes>
</test>
```

Repeat for `edge` and `firefox`.

---

## Configuration reference

### `src/test/resources/configuration.yaml`

| Key | Default | Description |
|-----|---------|-------------|
| `selenium.browser.type` | `chrome` | Default browser when no TestNG parameter is set |
| `selenium.browser.chrome.args` | headless flags | Chrome launch arguments |
| `selenium.browser.edge.args` | headless flags | Edge launch arguments |
| `selenium.browser.firefox.args` | headless flags | Firefox launch arguments |
| `selenium.web_driver.target` | `LOCAL` | `LOCAL` or `REMOTE` (Selenium Grid) |
| `selenium.default.timeOut` | `30` | Default wait timeout in seconds |
| `selenium.enableTracing` | `false` | Selenium BiDi tracing |
| `webElementIdentifiers.directory` | *(set via system property in build.gradle)* | Resolved to absolute path at build time |

### Gradle system properties (`build.gradle` → `test` block)

| Property | Source | Purpose |
|----------|--------|---------|
| `configuration.base` | `${projectDir}/src/test/resources/configuration.yaml` | Tells `ConfigurationManager` where to load the YAML config |
| `webElementIdentifiers.directory` | `${projectDir}/src/test/resources/webElementIdentifiers` | Absolute path passed to `FileInputStream` inside the framework |
| `APP_URL` | `-DAPP_URL=…` or default | AUT base URL |
| `selenium.web_driver.target` | `-Dselenium.web_driver.target=…` or `LOCAL` | Local vs. Selenium Grid |
| `selenium.hub.url` | `-Dselenium.hub.url=…` | Grid hub endpoint (when `REMOTE`) |

### `~/.gradle/gradle.properties` (local only, not committed)

```properties
gpr.user=<github-username>
gpr.token=<PAT-with-read:packages>
```
