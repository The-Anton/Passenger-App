package com.example.passenger_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val PERMISSION_REQUEST = 10
class MainActivity : AppCompatActivity() {
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var isLocationDisabled = true
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



         val locationStateReceiver: BroadcastReceiver = object : BroadcastReceiver(){
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action.equals("android.location.PROVIDERS_CHANGED")) {
                    Toast.makeText(
                            context, "PROVIDERS_CHANGED",
                    Toast.LENGTH_LONG
                    ).show()
                    if (locationManager.isLocationEnabled) {
                        val fragment = HomeFragment()
                        supportFragmentManager.beginTransaction().replace(
                            R.id.container_main,
                            fragment,
                            fragment.javaClass.simpleName
                        ).commit()
                    }

                }
            }
        }
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationStateReceiver, filter)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))

        val user = Firebase.auth.currentUser
        if (user != null){
           //ljdfl
        }else {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
        val db = Firebase.firestore
        val uid = user?.uid.toString()
        val ref = db.collection("Passengers").document(uid)
        ref.get()
            .addOnSuccessListener { document ->
                if (document !=null){
                    val username = document.get("name")
                    supportActionBar?.setTitle("Hello $username")
                }
            }


        val bottomNav : BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> {
                    // Respond to navigation item 1 click
                    val fragment = HomeFragment()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.container_main,
                        fragment,
                        fragment.javaClass.simpleName
                    ).commit()
                    true
                }
                R.id.rewards -> {
                    val fragment = RewardFragment()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.container_main,
                        fragment,
                        fragment.javaClass.simpleName
                    ).commit()
                    // Respond to navigation item 2 click
                    true
                }
                R.id.profile -> {
                    val fragment = ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.container_main,
                        fragment,
                        fragment.javaClass.simpleName
                    ).commit()
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                getLocation()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            getLocation()
        }

    }





    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val uid = Firebase.auth.currentUser?.uid
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            isLocationDisabled = false
            if (hasGps) {
                val fragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(
                    R.id.container_main,
                    fragment,
                    fragment.javaClass.simpleName
                ).commit()
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(p0: Location) {
                            if (p0 != null) {
                                locationGps = p0
                                if (uid != null) {
                                    Firebase.firestore.collection("Passengers").document(uid)
                                        .update(
                                            "Longitude",
                                            locationGps!!.longitude,
                                            "Latitude",
                                            locationGps!!.latitude
                                        )
                                        .addOnSuccessListener {
                                            //Snackbar.make(button3, "Location Data feeds start", Snackbar.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            //Snackbar.make(button3, "Failed location feed", Snackbar.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }

                    })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(p0: Location) {
                            if (p0 != null) {
                                locationNetwork = p0
                                if (uid != null) {
                                    Firebase.firestore.collection("Passengers").document(uid)
                                        .update(
                                            "Longitude",
                                            locationNetwork!!.longitude,
                                            "Latitude",
                                            locationNetwork!!.latitude
                                        )
                                        .addOnSuccessListener {
                                            //Snackbar.make(button3, "Location Data feeds start", Snackbar.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            //Snackbar.make(button3, "Failed location feed", Snackbar.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }

                    })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if(locationGps!= null && locationNetwork!= null){
                if(locationGps!!.accuracy > locationNetwork!!.accuracy){
                    if (uid != null) {
                        Firebase.firestore.collection("Passengers").document(uid).update(
                            "Longitude",
                            locationGps!!.longitude, "Latitude", locationGps!!.latitude
                        )
                            .addOnSuccessListener {
                                //Snackbar.make(button3, "Location Data feeds start", Snackbar.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                //Snackbar.make(button3, "Failed location feed", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                }else{
                    if (uid != null) {
                        Firebase.firestore.collection("Passengers").document(uid).update(
                            "Longitude",
                            locationNetwork!!.longitude, "Latitude", locationNetwork!!.latitude
                        )
                            .addOnSuccessListener {
                                //Snackbar.make(button3, "Location Data feeds start", Snackbar.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                //Snackbar.make(button3, "Failed location feed", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                }
            }else{
                val fragment = NoLocationFragment()
                supportFragmentManager.beginTransaction().replace(
                    R.id.container_main,
                    fragment,
                    fragment.javaClass.simpleName
                ).commit()
            }
        }

        }

    override fun onResume() {
        getLocation()
        super.onResume()
    }
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                        permissions[i]
                    )
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Go to settings and enable the permission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (allSuccess)
                getLocation()

        }
    }
}
