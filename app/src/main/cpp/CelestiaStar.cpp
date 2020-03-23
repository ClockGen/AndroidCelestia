#include "CelestiaJNI.h"
#include <celengine/star.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaStar_c_1getWebInfoURL(JNIEnv *env, jobject thiz) {
    Star *star = (Star *)env->GetLongField(thiz, caoPtrFieldID);
    return env->NewStringUTF(star->getInfoURL().c_str());
}