//
// Created by angset on 16/10/2017.
//
#include <jni.h>
#include "com_example_angiopasqui_permissionchecker_SSL.h"

JNIEXPORT jstring JNICALL Java_com_example_angiopasqui_permissionchecker_SSL_Sign
  (JNIEnv * env, jobject obj){
    return (*env)->NewStringUTF(env, "Hello from JNI");
  }
