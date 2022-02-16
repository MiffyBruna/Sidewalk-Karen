package com.example.sidewalkkaren


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import  android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener


class HomeActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var send: Button
    private lateinit var getPos: Button
    private lateinit var openMap: Button
    private lateinit var takePhoto: Button
    private lateinit var attachPhoto: Button
    private lateinit var displayAddress: TextView
    private lateinit var displayCurrentPos: TextView
    private lateinit var imageView: ImageView
    private lateinit var spinner: Spinner



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        displayCurrentPos = findViewById(R.id.displayCurrentPosition)
        displayAddress = findViewById(R.id.displayCurrentAddress)
        imageView = findViewById(R.id.photoPreview)
        spinner = findViewById(R.id.spinner)

        openMap = findViewById(R.id.getPositionInMap)
        attachPhoto = findViewById(R.id.attachPhoto)
        takePhoto = findViewById(R.id.takePhoto)
        getPos = findViewById(R.id.getPosition)
        send = findViewById(R.id.sendRequest)


        val typeOfRequest = resources.getStringArray(R.array.Type_of_request)


        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, typeOfRequest)
            spinner.adapter = adapter}


        spinner.onItemSelectedListener = object :
            OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {

                    emailSubject = typeOfRequest[position]
            }

           override fun onNothingSelected(parent: AdapterView<*>) {
              // write code to perform some action
           }
        }
        getPos.setOnClickListener {
            getCurrentLocation()
        }

        takePhoto.setOnClickListener{

            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val providerFile =FileProvider.getUriForFile(this,"com.example.androidcamera.fileprovider", photoFile)
            attachments = providerFile.toString()
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
            if (takePhotoIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(takePhotoIntent, TAKE_PHOTO_CODE)
            }else {
                Toast.makeText(this,getString(R.string.cameraAccessDenied), Toast.LENGTH_SHORT).show()
            }
        }


        attachPhoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PHOTO_PERMISSION_CODE)
                } else{
                    chooseImageGallery()
                }
            }else{
                chooseImageGallery()
            }
        }

        openMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, 5809 )
        }

        send.setOnClickListener {
            val recipient = "mipinipi@gmail.com"
            val subject = emailSubject
            val message ="Hello"+"\n Today in my daily walk I noticed a "+ emailSubject+ " at: " +"\n" +  displayAddress.text.toString().trim() +"\nLATITUDE/LONGITUDE: "+ displayCurrentPos.text.toString().trim() + "\n Please fix this, accessibility and proper sidewalks are important because they can offer considerable health, economic and equity benefits. " +"\n"+ "\n Your nice neighbor,\n  K.A.R.E.N"


            if(subject == "Missing sidewalk" ||
                subject == "Broken sidewalk" ||
                subject == "Ramps outdated" ||
                subject == "Missing ramp" ||
                subject == "Crosswalk request"  ){

               if(displayAddress.text.toString() != ""){
                sendEmail(recipient, subject, message) }
            } else{ Toast.makeText(this, "Subject or/and location missing", Toast.LENGTH_LONG).show()}
        }

    }



    private fun sendEmail(recipient: String, subject: String, message: String){


        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"

//        mIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(photoFromGalleryUri) )
//        mIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("$photoUri"))



        if(attachments != ""){
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        mIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(attachments) )}
        else{
            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            mIntent.putExtra(Intent.EXTRA_TEXT, message) }


        try {
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }


    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if(checkPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    val location: Location? = task.result
                    if(location == null){
                        Toast.makeText(this,"Null Received",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
                        displayCurrentPos.text = ""+ location.latitude + ", "+ location.longitude
                        displayAddress.text = getAddress(location.latitude,location.longitude)
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermissions()
        }
    }

    private fun getAddress(lat: Double, long: Double): String {

        val geocoder = Geocoder(this)
        val addresses: List<Address> = geocoder.getFromLocation(lat, long, 1)

        return addresses[0].getAddressLine(0)

    }

    private fun chooseImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ATTACH_PHOTO_FROM_GALLERY_CODE)
    }

    companion object{
        private const val GET_LOCATION_FROM_MAP = 5809
        private const val PERMISSION_REQUEST_ACCESS_LOCATION= 1001
        private const val ATTACH_PHOTO_FROM_GALLERY_CODE = 10008
        private const val PHOTO_PERMISSION_CODE = 61001
    }



    private fun isLocationEnabled(): Boolean{
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )

    }

    private fun checkPermission():Boolean{
        if(
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION
        ){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "OK", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }else{
                Toast.makeText(applicationContext, "DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ATTACH_PHOTO_FROM_GALLERY_CODE && resultCode == Activity.RESULT_OK){

            imageView.setImageURI(data?.data)

            Log.i("whatisthisthing:", data?.data.toString())
            attachments = data?.data.toString()
        }

        if(requestCode == GET_LOCATION_FROM_MAP){
            val coordinates = data?.getStringExtra("coordinates")
            val coordtoadress = data?.getStringExtra("address")
            displayCurrentPos.text = coordinates
            displayAddress.text = coordtoadress
        }

        if(requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK){
            val takenPhoto = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(takenPhoto)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}

private const val TAKE_PHOTO_CODE = 13
private lateinit var photoFile: File
var FILE_NAME = "photo.jpeg"
var photoFromGalleryUri = ""
var attachments = ""
var emailSubject = ""
