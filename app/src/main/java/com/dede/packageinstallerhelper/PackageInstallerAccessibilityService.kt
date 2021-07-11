package com.dede.packageinstallerhelper

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 系统包安装器辅助功能服务
 *
 * @author hsh
 * @since 2021/7/11 1:22 下午
 */
class PackageInstallerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "PackageInstaller"

        private val TARGET_PACKAGE_NAMES = arrayOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"
        ) // 需要处理目标包名的无障碍事件

        private const val ID_FORMAT = "%s:id/%s"

        private const val PACKAGE_INSTALLER = "com.android.packageinstaller"
        private const val PACKAGE_SYSTEM = "android"

        private const val ID_ALERT_TITLE = "alertTitle"
        private const val ID_ALERT_BUTTON1 = "button1"// POSITIVE, 安装确定
        private const val ID_ALERT_BUTTON2 = "button2"// NEGATIVE, 安装取消, 安装完成
        private const val ID_ALERT_MESSAGE = "message"// 卸载Dialog
        private const val ID_UNINSTALL_MESSAGE = "message"// 卸载Act
        private const val ID_OK_BUTTON = "ok_button"// 安装确定
        private const val ID_DONE_BUTTON = "done_button"// 安装完成
        private const val ID_LAUNCH_BUTTON = "launch_button"// 打开

        private const val ID_INSTALLING_MESSAGE = "installing"// 安装中
        private const val ID_PROGRESS_BAR = "progress_bar"// 安装中
        private const val ID_PROGRESS = "progress"// 安装中
        private const val ID_SCROLLVIEW = "scrollview"// 权限scrollview
        private const val ID_INSTALL_SUCCESS_MESSAGE = "install_success"// 安装成功
        private const val ID_INSTALL_CONFIRM_QUESTION = "install_confirm_question"// 安装
        private const val ID_INSTALL_CONFIRM_QUESTION_UPDATE =
            "install_confirm_question_update"// 覆盖安装
    }

    private fun AccessibilityNodeInfo.findNodeInfoById(
        packageName: String,
        id: String
    ): AccessibilityNodeInfo? {
        val nodeInfos =
            this.findAccessibilityNodeInfosByViewId(ID_FORMAT.format(packageName, id))
        if (nodeInfos == null || nodeInfos.isEmpty()) {
            return null
        }
        for (i in (1 until nodeInfos.size)) {
            nodeInfos[i]?.recycle()
        }
        return nodeInfos[0]
    }

    private fun AccessibilityNodeInfo.performClick() {
        this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun AccessibilityNodeInfo.use(action: AccessibilityNodeInfo.() -> Unit): AccessibilityNodeInfo {
        action.invoke(this)
        this.recycle()
        return this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.i(TAG, "onAccessibilityEvent, event: ${event.eventType}")
//        Log.i(TAG, "onAccessibilityEvent, package: ${event.packageName}")
        val source = event.source ?: return
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                // 卸载
                val uninstallAlertNodeInfo =
                    source.findNodeInfoById(PACKAGE_SYSTEM, ID_ALERT_MESSAGE)
                val uninstallNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_UNINSTALL_MESSAGE)
                if (uninstallAlertNodeInfo != null || uninstallNodeInfo != null) {
                    Log.i(TAG, "onAccessibilityEvent: uninstall")
                    uninstallAlertNodeInfo?.recycle()
                    uninstallNodeInfo?.recycle()
                    return
                }

                // 安装中
                val installingNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_INSTALLING_MESSAGE)
                val progressNodeInfo = source.findNodeInfoById(PACKAGE_INSTALLER, ID_PROGRESS)
                if (installingNodeInfo != null || progressNodeInfo != null) {
                    installingNodeInfo?.recycle()
                    progressNodeInfo?.recycle()
                    Log.i(TAG, "onAccessibilityEvent: installing")
                    return
                }

                // 安装成功 alert
                val installSuccessNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_INSTALL_SUCCESS_MESSAGE)?.use {
                        source.findNodeInfoById(PACKAGE_SYSTEM, ID_ALERT_BUTTON2)?.use {
                            Log.i(TAG, "onAccessibilityEvent: click this")
                            this.performClick()
                        }
                    }
                if (installSuccessNodeInfo != null) {
                    Log.i(TAG, "onAccessibilityEvent: installSuccess")
                    return
                }
                // 安装成功old act
                val launchNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_LAUNCH_BUTTON)?.use {
                        source.findNodeInfoById(PACKAGE_INSTALLER, ID_DONE_BUTTON)?.use {
                            Log.i(TAG, "onAccessibilityEvent: click $this")
                            this.performClick()
                        }
                    }
                if (launchNodeInfo != null) {
                    Log.i(TAG, "onAccessibilityEvent: installSuccess")
                    return
                }

                // 标题
                source.findNodeInfoById(PACKAGE_SYSTEM, ID_ALERT_TITLE)?.use {
                    Log.i(TAG, "onAccessibilityEvent: " + this.text)
                }

                // 滚动
                source.findNodeInfoById(PACKAGE_INSTALLER, ID_SCROLLVIEW)?.use {
                    this.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    Log.i(TAG, "onAccessibilityEvent: scroll_forward")
                }

                // 安装
                val installQuestionNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_INSTALL_CONFIRM_QUESTION)
                // 更新
                val updateQuestionNodeInfo =
                    source.findNodeInfoById(PACKAGE_INSTALLER, ID_INSTALL_CONFIRM_QUESTION_UPDATE)
                if (installQuestionNodeInfo != null || updateQuestionNodeInfo != null) {
                    Log.i(TAG, "onAccessibilityEvent: install")
                    // 确定按钮
                    val okButton = source.findNodeInfoById(PACKAGE_SYSTEM, ID_ALERT_BUTTON1)
                        ?: source.findNodeInfoById(PACKAGE_INSTALLER, ID_OK_BUTTON)
                    okButton?.use {
                        Log.i(TAG, "onAccessibilityEvent: click $okButton")
                        this.performClick()
                    }
                }
                installQuestionNodeInfo?.recycle()
                updateQuestionNodeInfo?.recycle()

                // 安装中、卸载中old
                source.findNodeInfoById(PACKAGE_INSTALLER, ID_PROGRESS_BAR)?.use {
                    Log.i(TAG, "onAccessibilityEvent: installing or unstalling")
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
            }
            else -> {
            }
        }
        source.recycle()
    }

    override fun onInterrupt() {
        Log.i(TAG, "onInterrupt: ")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected: ")
        serviceInfo.packageNames = TARGET_PACKAGE_NAMES
    }

}