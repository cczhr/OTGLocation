package com.cczhr.otglocation

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import com.amap.api.mapcore.util.it
import com.cczhr.otglocation.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_main
    lateinit var hotPlugTools: HotPlugTools
    var libTools = IMobileDeviceTools()
    var hasLib = false
    var isRoot = false
    var isConnected = false
    var hasDeveloperImg = false


    @SuppressLint("SetTextI18n")
    override fun init() {
        version?.text = "V ${Application.getVersion()}"
        latitude.setText(Application.getLat())
        longitude.setText(Application.getLon())
        log.requestFocus()
        hotPlugTools = HotPlugTools()
        requestPermissions {
            if (!it) {
                CommonUtil.showToast(Application.context, R.string.please_check_permissions)
                startAppDetailSetting()
            }
        }
        hasLib = libTools.checkInstallLib(this)
        lib_status.text = hasLib.toString()

        libTools.isRoot {
            isRoot = it
            if (!it) {
                CommonUtil.showToast(Application.context, R.string.root_hint)
            }
            root_status.text = it.toString()
        }
        libTools.logStr.observe(this, Observer {
            logAdd(it)
        })

        hotPlugTools.register(this, { deviceNode ->
            libTools.startUsbmuxd(deviceNode,{
                isConnected = true
                connect_status.setText(R.string.connected)
            }, {
                logAdd(it)
            }, {
                product_version.text = it
            }, {
                device_name.text = it
            }, {
                hasDeveloperImg = it
                developer_img.text = it.toString()
                if (it)
                    logAdd("重要的事情说两遍：连接成功！现在你可以修改定位了!\n")
                else
                    CommonUtil.showToast(Application.context, "开发者镜像写入失败！！")
            })
        }, {
            libTools.stopUsbmuxd {
                isConnected = false
                connect_status.setText(R.string.disconnected)
                product_version.text = ""
                device_name.text = ""
                developer_img.text = ""
            }
        })
        text_input_layout.swipeRight {
            log.setText("")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        libTools.release()
        hotPlugTools.unRegister(this)
    }


    fun logAdd(str: String) {
        log.append(str + "\n")
        log.setSelection(log.text.toString().length)

    }

    fun selectLocation(view: View) {
        val lat = latitude.text.toString().toDoubleOrNull()
        val lon = longitude.text.toString().toDoubleOrNull()
        val intent=Intent(this, BaiduMapActivity::class.java)//MapActivity
        if (lat != null && lon != null) {
            intent.putExtra("lat",lat)
            intent.putExtra("lon",lon)
        }

        startActivityForResult(intent , 102)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102) {
            data?.let {

                val lat = it.getDoubleExtra("lat", -999999.0)
                val lon = it.getDoubleExtra("lon", -999999.0)

                if (lat != -999999.0 && lon != -999999.0) {
                    latitude.setText(lat.toString())
                    longitude.setText(lon.toString())
                }
            }
        }

    }

    fun installLib(view: View) {
        libTools.installLib(this) {
            hasLib = it
            lib_status.text = hasLib.toString()
            logAdd("组件已安装")
        }
    }

    fun uninstallLib(view: View) {
        libTools.uninstallLib(this) {
            hasLib = false
            lib_status.text = hasLib.toString()
            logAdd("组件已删除")
        }

    }

    fun modifyLocation(view: View) {
        if(!checkStatus())
            return
        var lat = latitude.text.toString().toDoubleOrNull()
        var lon = longitude.text.toString().toDoubleOrNull()
        if (lat != null && lon != null) {
            if (!cancel_location_offset.isChecked) {
                val result = CoordinateTransformUtil.gcj02towgs84(lon, lat)
                lat = result[1]
                lon = result[0]
            }
            logAdd("实际写入位置\nlat:$lat\nlon:$lon")

            Application.saveLat(lat.toString())
            Application.saveLon(lon.toString())

            libTools.modifyLocation(lat, lon) {
                CommonUtil.showToast(Application.context, "定位修改成功")
            }
        }


    }

    fun restoreLocation(view: View) {
        if(!checkStatus())
            return

        libTools.resetLocation {
            CommonUtil.showToast(Application.context, "定位已还原")
        }

    }

    fun about(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about_help)
            .setView(R.layout.view_about)
            .show()
    }

    private fun checkStatus(): Boolean {
        if (!hasLib)
            CommonUtil.showToast(Application.context, "请安装组件!")
        else if (!isConnected)
            CommonUtil.showToast(Application.context, "请连接设备!")
        else if(!hasDeveloperImg)
            CommonUtil.showToast(Application.context, "开发者驱动未挂载!")

        return hasLib&&isConnected&&hasDeveloperImg
    }


}