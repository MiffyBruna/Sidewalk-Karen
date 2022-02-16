package com.example.sidewalkkaren

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import android.content.Intent



class MapActivity : AppCompatActivity(),OnMapReadyCallback  {
    private lateinit var saveCurrentLocation:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        saveCurrentLocation = findViewById(R.id.saveLocation)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)


    }
    override fun onMapReady(googleMap: GoogleMap) {

        var notSeattle = LatLng(45.4314, -122.3735)

        var marker = googleMap.addMarker(
            MarkerOptions()
                .position(notSeattle)
                .draggable(true)
                .title("DRAG TO POSITION"))

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(notSeattle))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15F), 1000, null)

        googleMap.setOnCameraMoveListener {
            var newlatlon = googleMap.cameraPosition.target
            if (marker!=null){
                marker.position = newlatlon
                notSeattle = marker.position
            }
        }

        saveCurrentLocation.setOnClickListener {

            var finalLocation = marker?.position

            var finalLocationLat = finalLocation?.latitude
            var finalLocationLon = finalLocation?.longitude
            var FLOC = setOf(finalLocationLat, finalLocationLon)


            var YASS = finalLocation?.longitude?.let { it1 -> getAddress( finalLocation?.latitude, it1) }


            val intent = Intent(this@MapActivity, HomeActivity::class.java)
            intent.putExtra("coordinates", "$finalLocationLat, $finalLocationLon")
            intent.putExtra("address", "$YASS")


            Toast.makeText( this,"NewLocation $YASS", Toast.LENGTH_LONG).show()

            setResult(5809, intent)
            finish()
        }


 }
    private fun getAddress(lat: Double, long: Double): String {

        val geocoder = Geocoder(this)
        val addresses: List<Address> = geocoder.getFromLocation(lat, long, 1)

        return addresses[0].getAddressLine(0)
    }

}





