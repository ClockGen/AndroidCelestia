#include "CelestiaJNI.h"
#include <celengine/simulation.h>
#include <celengine/selection.h>
#include <celengine/starbrowser.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1getSelection(JNIEnv *env,
                                                                           jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    Selection sel = sim->getSelection();
    return (jlong)new Selection(sel);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1setSelection(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong ptr) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    sim->setSelection(Selection(*(Selection *)ptr));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1getUniverse(JNIEnv *env,
                                                                          jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    return (jlong)sim->getUniverse();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1completionForText(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jstring text,
                                                                                jint limit) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    const char *str = env->GetStringUTFChars(text, nullptr);
    std::vector<std::string> results = sim->getObjectCompletion(str);
    env->ReleaseStringUTFChars(text, str);
    jobject arrayObject = env->NewObject(alClz, aliMethodID, (int)results.size());
    int count = 0;
    for (auto result : results) {
        if (count > limit)
            break;
        env->CallBooleanMethod(arrayObject, alaMethodID, env->NewStringUTF(result.c_str()));
        count += 1;
    }
    return arrayObject;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1findObject(JNIEnv *env, jobject thiz,
                                                                         jstring name) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    const char *str = env->GetStringUTFChars(name, nullptr);
    Selection *sel = new Selection(sim->findObject(str));
    env->ReleaseStringUTFChars(name, str);
    return (jlong)sel;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1getStarBrowser(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jint kind) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    return (jlong)new StarBrowser(sim, (int)kind);
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1reverseObserverOrientation(
        JNIEnv *env, jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    sim->reverseObserverOrientation();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1getTime(JNIEnv *env, jobject thiz) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    return sim->getTime();
}

extern "C"
JNIEXPORT void JNICALL
Java_space_celestia_mobilecelestia_core_CelestiaSimulation_c_1setTime(JNIEnv *env, jobject thiz,
                                                                      jdouble time) {
    Simulation *sim = (Simulation *)env->GetLongField(thiz, csiPtrFieldID);
    sim->setTime(time);
}