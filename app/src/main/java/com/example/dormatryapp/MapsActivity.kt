package com.example.dormatryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.dormatryapp.constants.Constants.Iqtisodiyot
import com.example.dormatryapp.constants.Constants.KEY
import com.example.dormatryapp.constants.Constants.PROVINCES
import com.example.dormatryapp.constants.Constants.TASHKENT_UNIVERSITY
import com.example.dormatryapp.constants.Constants.arxitekturaQurilish
import com.example.dormatryapp.constants.Constants.axborotTexnalogiyalari
import com.example.dormatryapp.constants.Constants.diplomatiya
import com.example.dormatryapp.constants.Constants.farmatsevtika
import com.example.dormatryapp.constants.Constants.gubkina
import com.example.dormatryapp.constants.Constants.irregatsiya
import com.example.dormatryapp.constants.Constants.jahonTillari
import com.example.dormatryapp.constants.Constants.jurnalistika
import com.example.dormatryapp.constants.Constants.kimyoTexnologiya
import com.example.dormatryapp.constants.Constants.konservatoriyasi
import com.example.dormatryapp.constants.Constants.milliy
import com.example.dormatryapp.constants.Constants.milliy_rassomchilik
import com.example.dormatryapp.constants.Constants.moliya
import com.example.dormatryapp.constants.Constants.pedagogika
import com.example.dormatryapp.constants.Constants.pediaterya
import com.example.dormatryapp.constants.Constants.sanatMadaniyat
import com.example.dormatryapp.constants.Constants.sharqshunoslik
import com.example.dormatryapp.constants.Constants.stomatologiya
import com.example.dormatryapp.constants.Constants.texnika
import com.example.dormatryapp.constants.Constants.tibbiyotAkademiyasi
import com.example.dormatryapp.constants.Constants.toqimachilik
import com.example.dormatryapp.constants.Constants.transport
import com.example.dormatryapp.constants.Constants.xalqaroIslomAkademiyasi
import com.example.dormatryapp.constants.Constants.yuridik
import com.example.dormatryapp.constants.Constants.yuridikIqtisoslashgan
import com.example.dormatryapp.constants.TTJLocation
import com.example.dormatryapp.constants.listener.OnItemSelectListener
import com.example.dormatryapp.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*
import org.osmdroid.bonuspack.routing.MapQuestRoadManager
import org.osmdroid.util.GeoPoint


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //    private var myLoc:
    private var roadManager: MapQuestRoadManager? = null
    private var myLocation: GeoPoint? = null
    private val dialog = SpinnerAdapter()
    private var viloyatId: Int = 0
    private var universitetId: Int = 0
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var hasEnabled = false
    private var hasLocatedMe = false
    private val locationService by lazy(LazyThreadSafetyMode.NONE) {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (!hasLocatedMe) {
                hasLocatedMe = true
                val loc = result.lastLocation
                val tash = LatLng(loc.latitude, loc.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tash, 8F))
                myLocation = GeoPoint(tash.latitude, tash.longitude)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roadManager = MapQuestRoadManager(KEY)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        spinner_province.setOnClickListener {
            val listener = object : OnItemSelectListener {
                override fun onItemSelected(id: Int) {
                    this@MapsActivity.viloyatId = id
                    if (id > -1) {
                        spinner_university.isEnabled = true
                        spinner_province.text = PROVINCES[id]
                        spinner_university.text = "Universitet"
                        val latLng = LatLng(41.311081, 69.240562)
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10F)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap.animateCamera(cameraUpdate)
                        mMap.resetMinMaxZoomPreference()
                    }
                }
            }
            selectFilter("Viloyat", PROVINCES, listener)
        }

        spinner_university.setOnClickListener {
            when (viloyatId) {
                0 -> {
                    selectCountryItem(TASHKENT_UNIVERSITY)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasEnabled) {
            hasEnabled = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                } else {
                    checkLocationAvailableLaity()
                }
            } else {
                checkLocationAvailableLaity()
                userLocationEnable()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener {
            loadRouting(it)
        }
    }

    private fun loadRouting(latLng: LatLng) {
        if (myLocation == null) {
            Toast.makeText(this, "You need switch on location", Toast.LENGTH_LONG).show()
        } else {
            val list = ArrayList<GeoPoint>()
            list.add(myLocation!!)
            list.add(GeoPoint(latLng.latitude, latLng.longitude))
            val liveData = MutableLiveData<ArrayList<GeoPoint>>()
            val routTask = RoutingTask(liveData, roadManager!!, list)
            routTask.start()
            liveData.observe(this) {
                drawPloyLine(it)
            }
        }
    }

    private fun drawPloyLine(arrayList: java.util.ArrayList<GeoPoint>) {
        val option = PolylineOptions()
        arrayList.forEach {
            option.add(LatLng(it.latitude, it.longitude))
        }
        option.color(Color.RED)
        mMap.addPolyline(option)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAvailableLaity()
                userLocationEnable()
            }
        } else {
            Toast.makeText(this, "Give me permission Pleace", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectCountryItem(array: Array<String>) {
        val listener = object : OnItemSelectListener {
            override fun onItemSelected(id: Int) {
                if (id > -1) {
                    mMap.clear()
                    universitetId = id
                    spinner_university.text = array[id]
                    selectedUniver(id)
                }
            }
        }
        selectFilter("Universitet", array, listener)
    }

    private fun selectedUniver(id: Int) {
        when (id) {
            0 -> {
                setMarkerAllDormitory(gubkina)
            }
            1 -> {
                setMarkerAllDormitory(diplomatiya)
            }
            2 -> {
                setMarkerAllDormitory(milliy_rassomchilik)

            }
            3 -> {
                setMarkerAllDormitory(axborotTexnalogiyalari)
            }
            4 -> {
                setMarkerAllDormitory(irregatsiya)
            }
            5 -> {
                setMarkerAllDormitory(pediaterya)
            }
            6 -> {
                setMarkerAllDormitory(transport)
            }
            7 -> {
                setMarkerAllDormitory(yuridik)
            }
            8 -> {
                setMarkerAllDormitory(yuridikIqtisoslashgan)
            }
            9 -> {
                setMarkerAllDormitory(texnika)
            }
            10 -> {
                setMarkerAllDormitory(tibbiyotAkademiyasi)
            }
            11 -> {
                setMarkerAllDormitory(moliya)
            }
            12 -> {
                setMarkerAllDormitory(kimyoTexnologiya)
            }
            13 -> {
                setMarkerAllDormitory(sharqshunoslik)
            }
            14 -> {

                setMarkerAllDormitory(pedagogika)
            }
            15 -> {
                setMarkerAllDormitory(toqimachilik)
            }
            16 -> {
                setMarkerAllDormitory(arxitekturaQurilish)
            }
            17 -> {
                setMarkerAllDormitory(Iqtisodiyot)
            }
            18 -> {
                setMarkerAllDormitory(stomatologiya)
            }
            19 -> {
                setMarkerAllDormitory(farmatsevtika)
            }
            20 -> {
                setMarkerAllDormitory(konservatoriyasi)
            }
            21 -> {
                setMarkerAllDormitory(milliy)
            }
            22 -> {
                setMarkerAllDormitory(sanatMadaniyat)
            }
            23 -> {
                setMarkerAllDormitory(jahonTillari)
            }
            24 -> {
                setMarkerAllDormitory(xalqaroIslomAkademiyasi)
            }
            25 -> {
                setMarkerAllDormitory(jurnalistika)
            }
        }
    }

    private fun setMarkerAllDormitory(list: Array<TTJLocation>) {
        list.forEach {
            mMap.addMarker(MarkerOptions().position(it.latLng).title(it.name))
        }
        val last = list[list.size - 1].latLng
        mMap.moveCamera(CameraUpdateFactory.newLatLng(last))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(last, 15F))

    }

    private fun selectFilter(s: String, provinces: Array<String>, listener: OnItemSelectListener) {
        dialog.getData(s, provinces, listener)
        dialog.show(supportFragmentManager, "asdad")
    }

    @SuppressLint("MissingPermission")
    private fun userLocationEnable() {
        mMap.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationAvailableLaity() {
        locationService.locationAvailability
            .addOnSuccessListener {
                if (it.isLocationAvailable) {
                    requestLocation()
                    userLocationEnable()

                } else {
                    setUpLocationRequest()
                }
            }
            .addOnFailureListener { setUpLocationRequest() }
    }

    private fun setUpLocationRequest() {
        val req = createLocationReq()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .build()

        LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                requestLocation()
                userLocationEnable()
            }
            .addOnFailureListener { resolvableRequestException(it) }
    }

    private fun resolvableRequestException(it: Exception) {
        if (it is ResolvableApiException) {
            try {
                it.startResolutionForResult(this, 1002)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        locationService.requestLocationUpdates(createLocationReq(), callback, Looper.myLooper()!!)
    }

    private fun createLocationReq() = LocationRequest().apply {
        interval = 10_000
        fastestInterval = 5_000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}