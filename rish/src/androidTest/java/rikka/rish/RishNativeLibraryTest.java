package rikka.rish;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * Instrumented tests for the rish native library.
 *
 * <p>These tests verify that the JNI shared library ({@code librish.so}) is correctly built,
 * packaged, and can be loaded by the Android runtime. They also confirm that
 * {@code JNI_OnLoad} executes without error and returns a supported JNI version.
 */
@RunWith(AndroidJUnit4.class)
public class RishNativeLibraryTest {

    /**
     * Loads the native library once for the entire test class.
     * Any failure here will propagate as an {@link ExceptionInInitializerError} and fail
     * all tests, making it immediately clear that library loading itself is broken.
     */
    @BeforeClass
    public static void loadNativeLibrary() {
        System.loadLibrary("rish");
    }

    /**
     * Verifies that {@code librish.so} can be found and loaded by the Android class loader.
     * A successful load means {@code JNI_OnLoad} was called and returned without error.
     */
    @Test
    public void testLibraryLoadsWithoutException() {
        // If loadNativeLibrary() in @BeforeClass threw, this test would already be aborted.
        // Reaching here confirms the library loaded successfully.
    }

    /**
     * Verifies that calling {@link System#loadLibrary} a second time for the same library
     * is idempotent — the Android runtime must not throw or crash on repeated loads.
     */
    @Test
    public void testRepeatedLoadLibraryIsIdempotent() {
        try {
            System.loadLibrary("rish");
        } catch (UnsatisfiedLinkError e) {
            fail("Repeated System.loadLibrary(\"rish\") threw UnsatisfiedLinkError: "
                    + e.getMessage());
        }
    }

    /**
     * Verifies that attempting to load a non-existent library throws
     * {@link UnsatisfiedLinkError} — confirming the JNI loading mechanism itself works
     * and only accepts libraries that are actually bundled.
     */
    @Test(expected = UnsatisfiedLinkError.class)
    public void testMissingLibraryThrowsUnsatisfiedLinkError() {
        System.loadLibrary("nonexistent_library_that_does_not_exist");
    }
}
