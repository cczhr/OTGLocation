package com.cczhr.otglocation.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.amap.api.mapcore.util.it
import com.cczhr.otglocation.R


/**
 * @author cczhr
 * @description 苹果设备的热插拔 usbmuxd热插拔是通过libusb这个库实现的 libusb在android设备上热插拔检测失效 就用原生UsbManager代替了
 * @since 2021/2/22
 */
class HotPlugTools {
    private val VID_APPLE = 0x5ac
    private val ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION"
    private var disconnectAppleDevice: (() -> Unit)? = null
    private var connectAppleDevice: ((deviceNode: String, fd: Int) -> Unit)? = null
    private val usbDeviceStateFilter = IntentFilter()

    private lateinit var context: Context;
    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device?.vendorId == VID_APPLE)
                    disconnectAppleDevice?.invoke()
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device?.vendorId == VID_APPLE)
                    connectAppleDevice?.invoke(device.deviceName, 0)

            }/* else if (ACTION_USB_PERMISSION == action) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (device?.vendorId == VID_APPLE)
                        checkPermission(device)
                } else {
                    CommonUtil.showToast(Application.context, R.string.usb_tips)
                }


            }*/
        }
    }

    init {
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbDeviceStateFilter.addAction(ACTION_USB_PERMISSION)

    }

    fun register(
        context: Context,
        connectAppleDevice: (deviceNode: String, fd: Int) -> Unit,
        disconnectAppleDevice: () -> Unit
    ) {
        this.context = context
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        this.connectAppleDevice = connectAppleDevice
        this.disconnectAppleDevice = disconnectAppleDevice
        for(device in mUsbManager.deviceList.values ){
            if (device?.vendorId == VID_APPLE) {
                connectAppleDevice?.invoke(device.deviceName, 0)

            }
        }
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter)
    }

   /* fun checkPermission(usbDevice: UsbDevice) {
        val mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (mUsbManager.hasPermission(usbDevice)) {
            mUsbManager.openDevice(usbDevice)?.let {
                connectAppleDevice?.invoke(usbDevice.deviceName, it.fileDescriptor)
            }

        } else {
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
            mUsbManager.requestPermission(usbDevice, pendingIntent);
        }

    }*/

    fun unRegister(context: Context) {
        connectAppleDevice = null
        disconnectAppleDevice = null
        context.unregisterReceiver(mUsbReceiver)
    }
}