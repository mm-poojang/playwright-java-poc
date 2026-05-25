package com.example.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Playwright Java equivalent of common {@code playwright.config.ts} settings:
 * <ul>
 *   <li>Video: always recorded for each test (see {@link #VIDEO_WIDTH} / {@link #VIDEO_HEIGHT}).</li>
 *   <li>Screenshots: written only when the test fails (full page).</li>
 * </ul>
 *
 * <p>Use {@code @ExtendWith(PlaywrightArtifactsExtension.class)} on a test class and add a
 * {@link Page} parameter to test methods that need the browser.
 *
 * <p>Artifacts:
 * <ul>
 *   <li>{@code target/playwright-artifacts/runs/<class>__<method>/video/} — WebM per run</li>
 *   <li>{@code target/playwright-artifacts/screenshots/<class>__<method>.png} — on failure only</li>
 * </ul>
 *
 * <p>System property {@code playwright.headless} (default {@code false}) matches prior headed local runs;
 * set to {@code true} for CI.
 */
public final class PlaywrightArtifactsExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final Path ARTIFACTS_ROOT = Paths.get("target", "playwright-artifacts");

    /** Same as typical TS config {@code video.size.width}. */
    public static final int VIDEO_WIDTH = 1920;

    /** Same as typical TS config {@code video.size.height}. */
    public static final int VIDEO_HEIGHT = 1000;

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(PlaywrightArtifactsExtension.class.getName());

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "false"));

        Path runDir = runDirectory(context);
        Files.createDirectories(runDir);
        Path videoDir = runDir.resolve("video");

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        BrowserContext browserContext = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(VIDEO_WIDTH, VIDEO_HEIGHT)
                        .setRecordVideoDir(videoDir)
                        .setRecordVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT));
        Page page = browserContext.newPage();

        ExtensionContext.Store store = context.getStore(NS);
        store.put("playwright", playwright);
        store.put("browser", browser);
        store.put("browserContext", browserContext);
        store.put("page", page);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getStore(NS);
        Page page = store.get("page", Page.class);
        BrowserContext browserContext = store.get("browserContext", BrowserContext.class);
        Browser browser = store.get("browser", Browser.class);
        Playwright playwright = store.get("playwright", Playwright.class);

        if (playwright == null) {
            return;
        }

        try {
            if (context.getExecutionException().isPresent() && page != null) {
                Path screenshots = ARTIFACTS_ROOT.resolve("screenshots");
                Files.createDirectories(screenshots);
                Path png = screenshots.resolve(slug(context) + ".png");
                page.screenshot(new Page.ScreenshotOptions().setPath(png).setFullPage(true));
            }
        } finally {
            if (browserContext != null) {
                browserContext.close();
            }
            if (browser != null) {
                browser.close();
            }
            playwright.close();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return Page.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext.getStore(NS).get("page", Page.class);
    }

    private static Path runDirectory(ExtensionContext context) {
        return ARTIFACTS_ROOT.resolve("runs").resolve(slug(context));
    }

    private static String slug(ExtensionContext context) {
        String raw = context.getRequiredTestClass().getSimpleName()
                + "__"
                + context.getRequiredTestMethod().getName();
        return raw.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }
}
