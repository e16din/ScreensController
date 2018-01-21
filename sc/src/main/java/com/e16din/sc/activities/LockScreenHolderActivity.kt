package com.e16din.sc.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.e16din.humanoid.logD
import com.e16din.humanoid.logI
import com.e16din.sc.INVALID_VALUE
import com.e16din.sc.ScreensController
import com.e16din.sc.screens.LockScreen
import com.e16din.sc.services.LockScreenService
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


const val REQ_REQUEST_PERMISSION = 100

class LockScreenHolderActivity : AppCompatActivity() {

    private lateinit var serviceIntent: Intent

    //override fun controller() = ScreensController.instance()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        "startScreen LockScreenHolderActivity".logI()

        serviceIntent = Intent(applicationContext, LockScreenService::class.java)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val sc = ScreensController.instance()
        var screen = sc.currentScreen as LockScreen?
        if (screen == null) {
            screen = ScreensController.getFirstScreen() as LockScreen
        }

        if (screen.windowBackgroundDrawable != INVALID_VALUE) {
            window.setBackgroundDrawableResource(screen.windowBackgroundDrawable)
        }

        startScreenLocker()

        super.onCreate(savedInstanceState)

        if (screen.holderTheme != INVALID_VALUE) {
            setTheme(screen.holderTheme)
        }
        if (screen.holderLayout != INVALID_VALUE) {
            setContentView(screen.holderLayout)
        }
    }

    override fun onStop() {
        super.onStop()
        "hide stub activity".logD()
    }

    fun stopService() {
        stopService(serviceIntent)
    }

    private fun startScreenLocker() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startLockService()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (Settings.canDrawOverlays(this@LockScreenHolderActivity)) {
                            startLockService()

                        } else {
                            requestOverlayPermission()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                                    token: PermissionToken) {
                    }
                })
                .check()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:" + packageName)
        startActivityForResult(intent, REQ_REQUEST_PERMISSION)
    }

    private fun startLockService() {
        startService(serviceIntent)
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_REQUEST_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startLockService()
            }
        }
    }

    override fun onBackPressed() {
        // do nothing
    }
}
