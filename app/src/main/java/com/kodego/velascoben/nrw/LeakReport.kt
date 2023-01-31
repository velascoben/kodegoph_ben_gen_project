package com.kodego.velascoben.nrw

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.kodego.velascoben.nrw.databinding.ActivityLeakReportBinding
import com.kodego.velascoben.nrw.db.Reports
import com.kodego.velascoben.nrw.db.ReportsDao
import com.kodego.velascoben.nrw.db.Users
import com.kodego.velascoben.nrw.db.UsersDao
import com.kodego.velascoben.nrw.map.MapPresenter
import com.kodego.velascoben.nrw.map.Ui
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class LeakReport : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityLeakReportBinding
    private lateinit var tvGpsLocation: TextView
    private lateinit var latitude : String
    private lateinit var longitude : String
    private lateinit var userName : String

    private var userDao = UsersDao()
    private var reportsDao = ReportsDao()

    private val presenter = MapPresenter(this)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityLeakReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()

        binding.imgLeakage.visibility = View.GONE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnCapture.setOnClickListener() {
            showCamera()
        }

        binding.btnSubmit.setOnClickListener() {
            submitReport()
        }



//        binding.btnStartStop.setOnClickListener {
//            if (binding.btnStartStop.text == getString(R.string.start_label)) {
//                startTracking()
//                binding.btnStartStop.setText(R.string.stop_label)
//            } else {
//                stopTracking()
//                binding.btnStartStop.setText(R.string.start_label)
//            }
//        }

        presenter.onViewCreated()
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        presenter.ui.observe(this) { ui ->
            updateUi(ui)
        }

        presenter.onMapLoaded()
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun submitReport() {

        val date = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = date.format(Date())

        val time = SimpleDateFormat("hh:mm aaa")
        val currentTime = time.format(Date())

        // Initialized report type as null
        var type : String = ""

        // Check leakage type is reported
        if (binding.rbLow.isChecked) {
            type = "low"
        } else if (binding.rbMedium.isChecked) {
            type = "medium"
        } else if (binding.rbHigh.isChecked) {
            type = "high"
        }

        // Initialized date/time format of the filename
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",Locale.getDefault())
        val now = Date()

        // Initialized filename of the photo for upload and database record
        val filename = "$userName-${formatter.format(now)}"
        val storageReference = FirebaseStorage.getInstance().getReference("images/$filename")

        // Prepare data for submission to database
        val reportDate : String = currentDate
        val reportTime : String = currentTime
        val repairDate : String = ""
        val repairTime : String = ""
        val repairUser : String = ""
        val reportType : String = type
        val reportLong : String = longitude
        val reportLat : String = latitude
        val reportUser : String = userName
        val reportAddress1 : String = binding.etAddress1.text.toString()
        val reportAddress2 : String = binding.etAddress2.text.toString()
        val reportPhoto : String = filename
        val repairPhoto : String = ""

        val builder = AlertDialog.Builder(this@LeakReport)
        builder.setMessage("Are you sure to submit the report? Make sure all the details are correct as this cannot be changed anymore.")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Delete selected note from database
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Submitting report...")
                progressDialog.setCancelable(false)
                progressDialog
                progressDialog.show()

                // Get the data from an ImageView as bytes
                binding.imgLeakage.isDrawingCacheEnabled = true
                binding.imgLeakage.buildDrawingCache()
                val bitmap = (binding.imgLeakage.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                // Add report to database
                reportsDao.add(Reports("",reportDate,reportTime,repairDate,repairTime,repairUser,reportType,reportLong,reportLat,reportUser,reportAddress1,reportAddress2,reportPhoto,repairPhoto))

                // Save photo to database
                storageReference.putBytes(data)
                    .addOnSuccessListener {
                        binding.imgLeakage.setImageURI(null) // Removes photo from imageview
                        Toast.makeText(applicationContext,"Successfully Submitted",Toast.LENGTH_LONG).show()
                        if(progressDialog.isShowing) progressDialog.dismiss()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userName", userName)
                        finish()
                        startActivity(intent)

                    }.addOnFailureListener{
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        Toast.makeText(applicationContext,"Submission Failed",Toast.LENGTH_LONG).show()
                    }

            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }

        val alert = builder.create()
        alert.show()


    }

    // For Start and Stop Tracking - For future development
//    private fun startTracking() {
//        binding.container.txtPace.text = ""
//        binding.container.txtDistance.text = ""
//        binding.container.txtTime.base = SystemClock.elapsedRealtime()
//        binding.container.txtTime.start()
//        map.clear()
//
//        presenter.startTracking()
//    }
//
//    private fun stopTracking() {
//        presenter.stopTracking()
//        binding.container.txtTime.stop()
//    }

    @SuppressLint("MissingPermission")
    private fun updateUi(ui: Ui) {
        // Gets current longitude and latitude
        latitude = ui.currentLocation?.latitude.toString()
        longitude = ui.currentLocation?.longitude.toString()

        if (ui.currentLocation != null && ui.currentLocation != map.cameraPosition.target) {
            map.isMyLocationEnabled = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(ui.currentLocation, 18f))
            tvGpsLocation = binding.tvLocation
            tvGpsLocation.text = "Latitude: $latitude, Longitude: $longitude " // This displays longitude and latitude below map
        }
//        binding.container.txtDistance.text = ui.formattedDistance
//        binding.container.txtPace.text = ui.formattedPace
        drawRoute(ui.userPath)
    }

    private fun drawRoute(locations: List<LatLng>) {
        val polylineOptions = PolylineOptions()

        map.clear()

        val points = polylineOptions.points
        points.addAll(locations)

        map.addPolyline(polylineOptions)
    }

    private fun gotoSettings() {
        AlertDialog.Builder(this).setMessage("It seems that your permission has been denied. Go to Settings to enable permission")
            .setPositiveButton("Go to Settings") { dialog, item ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                var uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") {dialog, item ->
                dialog.dismiss()
            }.show()
    }

    private fun showCamera() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.CAMERA
        ).withListener(object  : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                //startActivity(cameraIntent)
                cameraLauncher.launch(cameraIntent)
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                gotoSettings()
            }

            override fun onPermissionRationaleShouldBeShown(
                request: PermissionRequest?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).onSameThread().check()
    }

    val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            result.data?.extras.let {
                val image : Bitmap = result.data?.extras?.get("data") as Bitmap
                binding.imgLeakage.setImageBitmap(image) // Attaches captured photo to imageview
                binding.imgLeakage.visibility = View.VISIBLE // Shows the photo being captured by camera
            }
        }
    }
}