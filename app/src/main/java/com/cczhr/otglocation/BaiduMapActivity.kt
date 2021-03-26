package com.cczhr.otglocation

import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import kotlinx.android.synthetic.main.activity_baidu_map.*

/**
 * @author cczhr
 * @description
 * @since 2021/3/26 11:45
 */
class BaiduMapActivity :BaseActivity() {
    override val layoutId: Int   = R.layout.activity_baidu_map

    lateinit var baiduMap: BaiduMap

    lateinit var locationClient:LocationClient
    override fun init() {
        locationClient=LocationClient(this)
        locationClient.locOption= LocationClientOption().apply {
            isOpenGps=true
            coorType="gcj02"
            scanSpan=1000
        }
        locationClient.registerLocationListener(object :BDAbstractLocationListener(){
            override fun onReceiveLocation(location: BDLocation?) {




            }

        })
        baiduMap=map_view.map
        baiduMap.isMyLocationEnabled=true


        locationClient.start()
    }


    override fun onResume() {
        map_view.onResume()
        super.onResume()

    }


    override fun onPause() {
        map_view.onPause()
        super.onPause()

    }


    override fun onDestroy() {
        locationClient.stop()
        baiduMap.isMyLocationEnabled=false
        map_view.onDestroy()
        super.onDestroy()

    }


}