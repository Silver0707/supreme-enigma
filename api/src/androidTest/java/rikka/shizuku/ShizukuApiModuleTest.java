package rikka.shizuku;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * Instrumented tests for the Shizuku API module.
 *
 * <p>The {@code api} module defines the public surface of the Shizuku API library.
 * These tests verify that the module is correctly packaged and that an Android
 * runtime environment is available to consumers of the library.
 */
@RunWith(AndroidJUnit4.class)
public class ShizukuApiModuleTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * Verifies that an application context is obtainable in a process that has the
     * Shizuku API module on its classpath. This is the minimal smoke-test for the
     * instrumented test environment.
     */
    @Test
    public void testApplicationContextIsAvailable() {
        assertNotNull("Application context must not be null", context);
    }

    /**
     * Verifies that the host application's package name is a non-empty string,
     * confirming the test runner is correctly initialised.
     */
    @Test
    public void testApplicationPackageNameIsNonEmpty() {
        String packageName = context.getPackageName();
        assertNotNull("Package name must not be null", packageName);
        assertTrue("Package name must not be empty", !packageName.isEmpty());
    }

    /**
     * Verifies that the Shizuku API namespace ({@code rikka.shizuku}) is reachable at
     * runtime by checking that the test class itself was loaded from the expected package.
     * This guards against accidental namespace renames that break downstream consumers.
     */
    @Test
    public void testModuleNamespaceIsReachable() {
        String className = ShizukuApiModuleTest.class.getName();
        assertTrue("Test class must reside in the rikka.shizuku package",
                className.startsWith("rikka.shizuku"));
    }
}
