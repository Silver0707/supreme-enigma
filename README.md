diff --git a/home/runner/work/supreme-enigma/supreme-enigma/configuration-cache/src/main/kotlin/org/gradle/internal/DisplayName.kt b/home/runner/work/supreme-enigma/supreme-enigma/configuration-cache/src/main/kotlin/org/gradle/internal/DisplayName.kt
index 0000000..0000000 100644
--- a/home/runner/work/supreme-enigma/supreme-enigma/configuration-cache/src/main/kotlin/org/gradle/internal/DisplayName.kt
+++ b/home/runner/work/supreme-enigma/supreme-enigma/configuration-cache/src/main/kotlin/org/gradle/internal/DisplayName.kt
@@ -2,7 +2,22 @@
 
 import org.gradle.api.Describable
 
+/**
+ * A [Describable] that additionally provides a capitalized form of its display name.
+ *
+ * The capitalized form is used when the display name appears at the start of a sentence – for
+ * example in [toString] implementations that produce human-readable diagnostic strings.
+ *
+ * Prefer accepting [Describable] in APIs that only need the plain name; use [DisplayName] only
+ * when the capitalized form is explicitly required (e.g. for sentence-start contexts).
+ */
 interface DisplayName : Describable {
+    /**
+     * Returns the [displayName] with the first character converted to title-case (upper-case).
+     *
+     * Suitable for use at the beginning of a sentence or log line where conventional
+     * sentence-case capitalization is expected.
+     */
     val capitalizedDisplayName: String
 }
shizuku_version = (the version above)
implementation "dev.rikka.shizuku:api:$shizuku_version"# supreme-enigma
Ssss
