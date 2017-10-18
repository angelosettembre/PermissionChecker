LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := plopapksign
LOCAL_LDLIBS += -lplopapksign

include $(BUILD_SHARED_LIBRARY)