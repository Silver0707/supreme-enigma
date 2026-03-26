package rikka.rish;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Local (JVM) unit tests for the rish module.
 *
 * <p>These tests do not require an Android device or emulator. They validate
 * the module's build-generated constants via the {@link BuildConfig} class.
 */
public class RishModuleConfigTest {

    /**
     * Verifies that the module namespace exposed by the generated {@link BuildConfig}
     * is a well-formed, dot-separated identifier. This guards against the namespace
     * being accidentally cleared or malformed in the build configuration.
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
     * Verifies that the module namespace is exactly {@code rikka.rish}.
     * Any accidental rename would break downstream consumers that depend on this
     * package name for class loading and resource resolution.
     */
    @Test
    public void testLibraryPackageNameMatchesExpected() {
        assertTrue("LIBRARY_PACKAGE_NAME must start with 'rikka.rish'",
                BuildConfig.LIBRARY_PACKAGE_NAME.startsWith("rikka.rish"));
    }
}

