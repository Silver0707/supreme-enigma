package rikka.rish;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * Instrumented tests verifying that the rish module's AAR is correctly packaged
 * and its resources are accessible at runtime.
 */
@RunWith(AndroidJUnit4.class)
public class RishModulePackagingTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * Verifies that an application context is available, which is a prerequisite for
     * any Android instrumented test in this module.
     */
    @Test
    public void testApplicationContextIsNotNull() {
        assertNotNull("Application context must not be null", context);
    }

    /**
     * Verifies that the package name of the test application is non-empty, confirming
     * the test runner is correctly attached to a host application.
     */
    @Test
    public void testApplicationPackageNameIsNonEmpty() {
        String packageName = context.getPackageName();
        assertNotNull("Package name must not be null", packageName);
        assertTrue("Package name must not be empty", !packageName.isEmpty());
    }

    /**
     * Verifies that the native library file is present in the APK's native library directory
     * by attempting to load it. This confirms the CMake build correctly produced and
     * packaged {@code librish.so}.
     */
    @Test
    public void testNativeLibraryIsPresentInApk() {
        try {
            System.loadLibrary("rish");
        } catch (UnsatisfiedLinkError e) {
            throw new AssertionError(
                    "librish.so was not found in the APK — check that the CMake build "
                            + "produced the shared library and that it is packaged correctly: "
                            + e.getMessage(), e);
        }
    }
}
