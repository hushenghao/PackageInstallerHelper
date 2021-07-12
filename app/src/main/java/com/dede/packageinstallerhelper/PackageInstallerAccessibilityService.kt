package com.dede.packageinstallerhelper

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * 系统包安装器辅助功能服务
 *
 * https://developer.android.com/guide/topics/ui/accessibility/service
 *
 * @author hsh
 * @since 2021/7/11 1:22 下午
 */
class PackageInstallerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "PackageInstallerService"
    }

    private val accessibilityEventHandle by lazy { AccessibilityEventHandle(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.i(TAG, "onAccessibilityEvent, event: ${event.eventType}")
        accessibilityEventHandle.performAccessibilityEvent(event)
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected")
        serviceInfo = accessibilityEventHandle.getUpdateServiceInfo(serviceInfo)
    }

}