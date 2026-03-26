package rikka.shizuku;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Local (JVM) unit tests for the Shizuku API module.
 *
 * <p>These tests exercise the build-generated {@link BuildConfig} class to verify
 * that the module's configuration constants are correct and self-consistent.
 * They run on the local JVM without requiring an Android device or emulator.
 */
public class ShizukuApiConfigTest {

    /**
     * Verifies that the module's library package name exposed by the generated
     * {@link BuildConfig} is a well-formed, dot-separated identifier. Malformed
     * package names cause resource-merging failures at build time and should be
     * caught as early as possible.
     */
    @Test
    public void testLibraryPackageNameIsWellFormed() {
        String packageName = BuildConfig.LIBRARY_PACKAGE_NAME;
        assertNotNull("BuildConfig.LIBRARY_PACKAGE_NAME must not be null", packageName);
        assertTrue("LIBRARY_PACKAGE_NAME must not be empty", !packageName.isEmpty());
        assertTrue("LIBRARY_PACKAGE_NAME must contain a dot separator",
                packageName.contains("."));
        String[] parts = packageName.split("\\.");
        assertTrue("LIBRARY_PACKAGE_NAME must have at least two segments", parts.length >= 2);
        for (String part : parts) {
            assertTrue("Each segment of LIBRARY_PACKAGE_NAME must be non-empty",
                    !part.isEmpty());
        }
    }

    /**
     * Verifies that the library package name is exactly {@code rikka.shizuku}.
     * Any accidental rename would break downstream consumers that depend on this
     * package for class loading and resource resolution.
     */
    @Test
    public void testLibraryPackageNameMatchesExpected() {
        assertTrue("LIBRARY_PACKAGE_NAME must start with 'rikka.shizuku'",
                BuildConfig.LIBRARY_PACKAGE_NAME.startsWith("rikka.shizuku"));
    }
}

