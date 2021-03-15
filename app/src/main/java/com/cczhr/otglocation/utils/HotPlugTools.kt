package com.cczhr.otglocation.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

/**
 * @author cczhr
 * @description 苹果设备的热插拔 usbmuxd热插拔是通过libusb这个库实现的 libusb在android设备上热插拔检测失效 就用原生UsbManager代替了
 * @since 2021/2/22
 */
class HotPlugTools  {
    private val VID_APPLE = 0x5ac
    private var disconnectAppleDevice:(()->Unit)?=null
    private var connectAppleDevice:((deviceNode:String)->Unit)?=null
    private val usbDeviceStateFilter = IntentFilter()
    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if(device?.vendorId==VID_APPLE)
                    disconnectAppleDevice?.invoke()
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if(device?.vendorId==VID_APPLE)
                    connectAppleDevice?.invoke(device.deviceName)

            }
        }
    }
    init {
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
    }

    fun register(context: Context,connectAppleDevice:(deviceNode:String)->Unit,disconnectAppleDevice:()->Unit){
        val mUsbManager =  context.getSystemService(Context.USB_SERVICE) as UsbManager
        this.connectAppleDevice=connectAppleDevice
        this.disconnectAppleDevice=disconnectAppleDevice
        mUsbManager.deviceList.forEach { (_, value) ->

            if(value?.vendorId==VID_APPLE){
                connectAppleDevice.invoke(value.deviceName)
            }

        }
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter)
    }
    fun unRegister(context: Context){
        connectAppleDevice=null
        disconnectAppleDevice=null
        context.unregisterReceiver(mUsbReceiver)
    }
}