package com.dede.packageinstallerhelper

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.dede.packageinstallerhelper.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.btServiceStatus.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val isAccessibilityEnable =
            isAccessibilityEnable(this, PackageInstallerAccessibilityService::class.java)
        val resId =
            if (isAccessibilityEnable) R.string.bt_enable_service
            else R.string.bt_disable_service
        mainBinding.btServiceStatus.setText(resId)
    }

    private fun isAccessibilityEnable(
        context: Context,
        clazz: Class<out AccessibilityService>
    ): Boolean {
        val accessibilityManager =
            context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!accessibilityManager.isEnabled) {
            return false
        }

        val services = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (TextUtils.isEmpty(services)) {
            return false
        }
        val stringSplitter = TextUtils.SimpleStringSplitter(':')
            .apply { setString(services) }
        val serviceName = context.packageName + "/" + clazz.canonicalName
        while (stringSplitter.hasNext()) {
            val next = stringSplitter.next()
            Log.i("MainActivity", "checkAccessibilityEnable: " + next)
            if (next == serviceName) {
                return true
            }
        }
        return false
    }
}