package com.cczhr.otglocation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * @author cczhr
 * @since  2020/9/6
 * @description https://github.com/cczhr
 */
abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var filePathResult: (String) -> Unit
    private lateinit var permissionsResult: (Boolean) -> Unit
    protected abstract val layoutId: Int
    protected abstract fun init()
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main +
            CoroutineExceptionHandler { _, exception -> handleException(exception) }
    protected open fun handleException(t: Throwable) = t.printStackTrace()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        init()
    }
    fun requestPermissions(result: (Boolean) -> Unit) {
        permissionsResult = result
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        var isGetAllPermissions = true
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_DENIED) {
                isGetAllPermissions = false
                break
            }
        }
        if (!isGetAllPermissions) {
            ActivityCompat.requestPermissions(this, permissions, 100)
        } else {
            permissionsResult.invoke(true)
        }
    }


      fun startAppDetailSetting() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
    protected open fun switchInputMethod() {
        val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun isShouldHideInput(
        v: View?,
        event: MotionEvent
    ): Boolean {
        if (v != null && v is EditText) {
            val location = intArrayOf(0, 0)
            v.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]

            return (event.x < left || event.x > left + v.width
                    || event.y < top || event.y > top + v.height)
        }
        return false
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideInput(v, ev)) {
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v!!.windowToken, 0)


            }
            return super.dispatchTouchEvent(ev)
        }
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionsResult.invoke(false)
                        return
                    }
                }
                permissionsResult.invoke(true)
            }
        }
    }

}