package com.cczhr.otglocation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.offlinemap.OfflineMapActivity
import com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.cczhr.otglocation.utils.Application
import com.cczhr.otglocation.utils.CommonPopupWindow
import com.cczhr.otglocation.utils.runMainThread
import com.cczhr.otglocation.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity:BaseActivity(), LocationSource, AMapLocationListener,
    PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener {
    private var mLocationClient: AMapLocationClient?=null //定位发起端
    private lateinit var mLocationOption: AMapLocationClientOption
    private var mListener: LocationSource.OnLocationChangedListener?=null  //定位监听器
    private var locationLat= "" //定位或者移动地图的纬度
    private var locationLon= "" ////定位或者移动地图的经度
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private var isFirstLoc = true
    private lateinit var aMap: AMap
    override val layoutId: Int=R.layout.activity_map
    lateinit var inputTips:Inputtips
    var hasMaker=false
    override fun init() {


    }


    private  fun initQuery(){
        CommonUtil.setOnEnterClickListener(edit_query){ view: View, content: String ->
            switchInputMethod()
            if(content.isNotEmpty()){
                inputTips= Inputtips(this, InputtipsQuery(content, ""))
                inputTips.setInputtipsListener(this)
                inputTips.requestInputtipsAsyn()
            }

        }

    }
    private fun initLocationClient(){
        if(mLocationClient==null)
            mLocationClient =   AMapLocationClient(this);
        mLocationClient!!.setLocationListener(this)
        mLocationOption = AMapLocationClientOption()
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        mLocationClient!!.setLocationOption(mLocationOption)
        mLocationClient!!.disableBackgroundLocation(true)



    }
    private fun initMapView(){
        aMap=map_view.map
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17F))

        aMap.setOnMapClickListener {
            addMaker(it)
        }
        aMap.myLocationStyle = MyLocationStyle().showMyLocation(false)  //设置定位蓝点的Style
        aMap.setLocationSource(this)
        aMap.uiSettings.isMyLocationButtonEnabled = true
        aMap.isMyLocationEnabled = true
    }



    fun addMaker(latLng: LatLng){
        hasMaker=true
        aMap.clear()
        aMap.addMarker(MarkerOptions().position(latLng))
        locationLat = latLng.latitude.toString()
        locationLon =latLng.longitude.toString()

        location_info.text = "当前选择坐标:\n${getText(R.string.latitude)}$locationLat\n${getText(R.string.longitude)}$locationLon"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        map_view.onCreate(savedInstanceState)
        initMapView()
        initLocationClient()
        initQuery()


        val lat =  intent.getDoubleExtra("lat", -999999.0)
        val lon = intent.getDoubleExtra("lon", -999999.0)
        if (lat != -999999.0 && lon != -999999.0) {
            val latLng= LatLng(
                lat,
                lon
            )
            addMaker(latLng)
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng)
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        map_view.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        map_view.onResume()
        mLocationClient?.startLocation()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        map_view.onPause()
        mLocationClient?.stopLocation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        map_view.onSaveInstanceState(outState);
    }

    override fun deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient?.stopLocation()
            mLocationClient?.onDestroy()
        }
        mLocationClient = null
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
        mListener = onLocationChangedListener
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    if(!hasMaker) {
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(17F))
                        //将地图移动到定位点
                        aMap.moveCamera(
                            CameraUpdateFactory.changeLatLng(
                                LatLng(
                                    aMapLocation.latitude,
                                    aMapLocation.longitude
                                )
                            )
                        )
                    }
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener?.onLocationChanged(aMapLocation)
                    isFirstLoc = false

                }
            }
        }
    }

    fun downloadOfflineMap(view: View) {
        startActivity(
            Intent(
                this,
                OfflineMapActivity::class.java
            )
        )


    }



    private fun createSearchPopup(
        editText: EditText,
        list: List<LocationAddressInfo>,
        selectData: ((LocationAddressInfo) -> Unit)? = null
    ) {
        val popupWindow: CommonPopupWindow =
            CommonPopupWindow.Builder(this)
                .setView(R.layout.view_popup)
                .setWidthAndHeight(editText.width, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setBackGroundLevel(1.0f)
                .setOutsideTouchable(true)
                .create()
        val view: View = popupWindow.controller.mPopupView
        val listView = view.findViewById<ListView>(R.id.list_view)
        val adapter: ArrayAdapter<LocationAddressInfo> = ArrayAdapter(
            this ,
            android.R.layout.simple_list_item_1,
            list
        )
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val result = list[position]
                editText.setText( result.title)
                editText.setSelection( editText.text.toString().length)
            if (selectData != null)
                selectData(result)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(editText)
    }


    data class LocationAddressInfo(var latLonPoint: LatLonPoint,var title:String,var address:String){
        override fun toString(): String {
            return  "$title\n$address"
        }
    }

    override fun onGetInputtips(tips: MutableList<Tip>, rCode: Int) {
        if (rCode ==CODE_AMAP_SUCCESS) {
            val data: ArrayList<LocationAddressInfo> = ArrayList<LocationAddressInfo>() //自己创建的数据集合
            tips.forEach {
                data.add(LocationAddressInfo(it.point, it.name, it.district+it.address))
            }
            Application.context.runMainThread {
                if (data.isNotEmpty()) {
                    createSearchPopup(edit_query, data) {
                        val latLng= LatLng(
                            it.latLonPoint.latitude,
                            it.latLonPoint.longitude
                        )
                        addMaker(latLng)
                        aMap.moveCamera(
                            CameraUpdateFactory.changeLatLng(
                                latLng
                            )
                        )
                    }
                }else{
                    CommonUtil.showToast(Application.context, "无搜索结果")
                }
            }
        }else{
            Application.context.runMainThread {
                CommonUtil.showToast(Application.context, "无搜索结果")//"错误码$rCode"
            }
        }
    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }

    override fun onPoiSearched(p0: PoiResult?, p1: Int) {

    }

    fun moveMap(view: View) {
        val lat = locationLat.toDoubleOrNull()
        val lon=   locationLon.toDoubleOrNull()
        if(lat==null||lon==null)
            return

        val latLng= LatLng(lat, lon)
        aMap.moveCamera(
            CameraUpdateFactory.changeLatLng(
                latLng
            )
        )

    }

    fun confirm(view: View) {
        val lat = locationLat.toDoubleOrNull()
        val lon=   locationLon.toDoubleOrNull()
        if(lat==null||lon==null){
            CommonUtil.showToast(Application.context,R.string.please_select_location)
        }else{
            setResult(0,Intent().putExtra("lat",lat).putExtra("lon",lon))
            finish()
        }


    }
}