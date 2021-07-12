package com.dede.packageinstallerhelper

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent

/**
 * 无障碍事件下发
 *
 * @author hsh
 * @since 2021/7/12 11:17 上午
 */
class AccessibilityEventHandle(context: Context) {

    private val dispatcher: AccessibilityEventDispatcher

    init {
        if (false) {
            dispatcher = HWAccessibilityEventDispatcher()
        } else {
            dispatcher = AccessibilityEventDispatcher()
        }
    }

    fun getUpdateServiceInfo(serviceInfo: AccessibilityServiceInfo): AccessibilityServiceInfo {
        // 需要处理目标包名的无障碍事件
        serviceInfo.packageNames = arrayOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"
        )
        return serviceInfo
    }

    fun performAccessibilityEvent(event: AccessibilityEvent) {
        dispatcher.performAccessibilityEvent(event)
    }

}