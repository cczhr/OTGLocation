package com.cczhr.otglocation


import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.amap.api.mapcore.util.it
import com.android.internal.R.attr.apiKey
import com.cczhr.otglocation.database.AppDatabase
import com.cczhr.otglocation.database.LocationData
import com.cczhr.otglocation.utils.Application
import com.cczhr.otglocation.utils.CommonUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_google_map.*
import kotlinx.android.synthetic.main.activity_google_map.location_info
import kotlinx.android.synthetic.main.activity_google_map.select_location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@SuppressLint("MissingPermission")
class GoogleMapActivity() : BaseActivity(), OnMapReadyCallback{

    private var locationLat = "" //定位或者移动地图的纬度
    private var locationLon = "" ////定位或者移动地图的经度
    override val layoutId: Int = R.layout.activity_google_map
    val locationData = ArrayList<LocationData>()
    lateinit var locationAdapter: ArrayAdapter<LocationData>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private lateinit var locationCallback: LocationCallback
    override fun init() {
        locationAdapter = ArrayAdapter(this, R.layout.popup_location_item, locationData)
        select_location.setAdapter(locationAdapter)
        loadLocationData()
        mapInit()
        searchPlaceInit()


    }






    private fun mapInit(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }
    private fun searchPlaceInit(){
        Places.initialize(applicationContext, getString(R.string.google_map_key))
        Places.createClient(this)
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    addMarker(it,false)
                }
            }
            override fun onError(status: Status) {
            }
        })

    }

    private fun startLocation(){
        val lat = intent.getDoubleExtra("lat", -999999.0)
        val lon = intent.getDoubleExtra("lon", -999999.0)
        if (lat != -999999.0 && lon != -999999.0) {
            addMarker(LatLng(lat, lon), true)
        }
        //定位监听
        locationCallback = object : LocationCallback() {}
        //最后一次定位
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
            if (lat == -999999.0 && lon == -999999.0) {
                if (task.isSuccessful) {
                    task.result?.let {
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom( LatLng(it.latitude, it.longitude), 18F))
                    }
                }
            }

        }
        //开始定位
        fusedLocationProviderClient.requestLocationUpdates(LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)//设置高精度
            .setInterval(2000), locationCallback,   Looper.getMainLooper()
        )
    }


    fun addMarker(latlng:LatLng,shouldZoom:Boolean=false){
        map?.clear()
        map?.addMarker(MarkerOptions().position(latlng))
        if(shouldZoom){
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,18F))
        }else{
            map?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        }
        locationLat = latlng.latitude.toString()
        locationLon = latlng.longitude.toString()
        location_info.text = "当前选择坐标:\n${getText(R.string.latitude)}$locationLat\n${getText(R.string.longitude)}$locationLon"
    }


    private fun setMapClickListener(){
        map?.setOnMapClickListener {
            addMarker(it)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapClickListener()
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.isMyLocationEnabled = true;
        googleMap.uiSettings.apply {
            setAllGesturesEnabled(true)
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isIndoorLevelPickerEnabled = true

        }
        startLocation()
        setSelectLocationListener()


    }

    fun setSelectLocationListener(){
        select_location.setOnItemClickListener { _, _, position, _ ->
            addMarker(LatLng( locationData[position].lat.toDouble(),   locationData[position].lon.toDouble()),true)
        }
    }


    fun confirm(view: View) {
        val lat = locationLat.toDoubleOrNull()
        val lon = locationLon.toDoubleOrNull()
        if (lat == null || lon == null) {
            CommonUtil.showToast(Application.context, R.string.please_select_location)
        } else {
            setResult(0, Intent().putExtra("lat", lat).putExtra("lon", lon))
            finish()
        }

    }


    @SuppressLint("SetTextI18n")
    fun saveLocation(view: View) {
        val lat = locationLat.toDoubleOrNull()
        val lon = locationLon.toDoubleOrNull()
        if (lat == null || lon == null) {
            CommonUtil.showToast(Application.context, R.string.please_select_location)
            return
        }
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.view_save_location, null).apply {
                findViewById<TextView>(R.id.latitude).text = getString(R.string.latitude) + lat
                findViewById<TextView>(R.id.longitude).text = getString(R.string.longitude) + lon
            }
        val info = dialogView.findViewById<EditText>(R.id.info)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.save_location)
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.save), null)
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (info.text.isBlank()) {
                CommonUtil.showToast(Application.context, getString(R.string.please_input_info))
            } else {
                val infoText = info.text.toString()
                launch {
                    withContext(Dispatchers.IO) {
                        var code = -1L
                        if (AppDatabase.getDatabase(this@GoogleMapActivity
                            ).locationDataDao()
                                .queryCountByInfo(infoText) == 0L
                        ) {
                            code = AppDatabase.getDatabase(this@GoogleMapActivity).locationDataDao()
                                .insert(LocationData(0, locationLat, locationLon, infoText))
                        }
                        code
                    }.let {
                        if (it != -1L) {
                            CommonUtil.showToast(
                                Application.context,
                                getString(R.string.saved_successfully)
                            )
                            dialog.dismiss()
                            loadLocationData()
                        } else {
                            CommonUtil.showToast(
                                Application.context,
                                getString(R.string.save_failed)
                            )
                        }
                    }
                }
            }

        }


    }

    fun deleteLocalLocation(view: View) {
        val info = select_location.text.toString()
        if (!info.isBlank()) {
            launch {
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@GoogleMapActivity).locationDataDao().deleteByInfo(info)

                }
                select_location.setText("",false)
                loadLocationData()
            }
        }


    }
    private fun loadLocationData() {
        launch {
            locationData.clear()
            locationAdapter.notifyDataSetChanged()
            withContext(Dispatchers.IO) {
                locationData.addAll(
                    AppDatabase.getDatabase(this@GoogleMapActivity).locationDataDao().getAll()
                )
            }
            locationAdapter.notifyDataSetChanged()
        }
    }

    fun moveMap(view: View) {
        val lat = locationLat.toDoubleOrNull()
        val lon = locationLon.toDoubleOrNull()
        if (lat == null || lon == null)
            return
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lon),18F))


    }



}