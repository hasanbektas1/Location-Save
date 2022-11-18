package com.hasanbektas.locationsave

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.hasanbektas.locationsave.databinding.ActivityMapsBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.lang.Exception


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{


    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private  lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var permissionLauncherCamera : ActivityResultLauncher<String>
    private lateinit var sharedPreferences : SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : LocationDatabase
    private lateinit var placeDao : LocationDao
    val compositeDisposable  = CompositeDisposable()
    var placeFromMain : com.hasanbektas.locationsave.Location? = null
    val IMAGE_CAPTURE_CODE: Int = 12346
    var imageUri: Uri? = null
    var selectedBitmap : Bitmap? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences= this.getSharedPreferences("com.hasanbektas.maps2", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLatitude = 0.0

        db = Room.databaseBuilder(applicationContext,LocationDatabase::class.java,"Places").build()

        placeDao = db.locationDao()

        binding.saveButton.isEnabled = false

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info == "new"){

            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE
            binding.cameraimageView.visibility = View.INVISIBLE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    println("location--: "+ location.toString())

                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                    if (trackBoolean == false){
                        println("locationtract: "+ location.toString())

                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()

                    }
                }

            }

            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){

                    Snackbar.make(binding.root,"Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()

                }else{
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

            } else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation !=null){
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled = true


            }

        }else{

            binding.cameraimageView.visibility = View.VISIBLE

            mMap.clear()

            placeFromMain = intent.getSerializableExtra("selectedPlace") as com.hasanbektas.locationsave.Location

            placeFromMain?.let {
                val latlng = LatLng(it.latitude,it.longitude)
                val imagess = it.image
                var bitmapimage = BitmapFactory.decodeByteArray(imagess,0,imagess.size)

                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.addMarker(MarkerOptions().position(latlng)
                    .title("Marker")
                    .snippet("Population: 4,137,400")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapimage)))

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                //   binding.cameraimageView.setImageBitmap(bitmapimage)

                binding.placeText.setText(it.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
                binding.camera.visibility = View.INVISIBLE
                binding.cameraimageView.visibility = View.INVISIBLE

            }
        }
    }

    private fun registerLauncher(){

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation !=null){
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    mMap.isMyLocationEnabled = true
                }
            } else{
                Toast.makeText(this@MapsActivity,"Permission needed", Toast.LENGTH_LONG).show()
            }
        }

        permissionLauncherCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                openCamera()
            }
        }

    }

    fun save (view: View){

        if (selectedLatitude != null && selectedLongitude != null && selectedBitmap != null){

            val smallBitmap  = makeSmallerBitmap(selectedBitmap!!,300)

            // Görseli veriye çevirme
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()


            val place = Location(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!,byteArray)
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }
    private fun handleResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete ( view: View){

        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }

    private fun makeSmallerBitmap(image : Bitmap, maximumSize : Int): Bitmap {


        var width = image.width
        var height = image.height

        var bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            // landscaope
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
            // portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)

    }
    fun camera(view: View){

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CAMERA)){

                Snackbar.make(view,"Permission needed for camera",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                    View.OnClickListener {
                        permissionLauncherCamera.launch(android.Manifest.permission.CAMERA)
                    }).show()
            }else{
                permissionLauncherCamera.launch(android.Manifest.permission.CAMERA)
            }
        }else{
            // CAMERA AÇ

            openCamera()
        }
    }

    private fun openCamera(){

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"imageTitle")
        imageUri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode== Activity.RESULT_OK){

            if (imageUri != null){

                try {
                    if (Build.VERSION.SDK_INT >=28){

                        val source = ImageDecoder.createSource(this@MapsActivity.contentResolver,imageUri!!)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)

                        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                            // Permission granted
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                            val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            mMap.clear()
                            if (currentLocation !=null){
                                val kullanıcıGüncelKonum = LatLng(currentLocation.latitude,currentLocation.longitude)
                                mMap.addMarker(MarkerOptions().position(kullanıcıGüncelKonum))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kullanıcıGüncelKonum,15f))


                                selectedLatitude = currentLocation.latitude
                                selectedLongitude = currentLocation.longitude

                                binding.saveButton.isEnabled = true
                            }
                            mMap.isMyLocationEnabled = true
                        }
                        binding.cameraimageView.visibility = View.VISIBLE
                        binding.cameraimageView.setImageBitmap(selectedBitmap)

                    }else{
                        selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                        binding.cameraimageView.visibility = View.VISIBLE
                        binding.cameraimageView.setImageBitmap(selectedBitmap)
                    }

                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }



}