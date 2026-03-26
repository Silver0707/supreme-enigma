#include <jni.h>
#include <android/log.h>

#define LOG_TAG "rish"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("rish native library loaded");
    return JNI_VERSION_1_6;
}

} // extern "C"
