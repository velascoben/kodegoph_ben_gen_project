package com.kodego.velascoben.nrw

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.kodego.velascoben.nrw.databinding.ActivityUpdateLeakBinding
import com.kodego.velascoben.nrw.db.Reports
import com.kodego.velascoben.nrw.db.ReportsDao
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UpdateLeak : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateLeakBinding
    private lateinit var userName : String
    private lateinit var userType : String
    private lateinit var reportID : String

    private var reportDao = ReportsDao()

    lateinit var reportDate : String
    lateinit var reportTime : String
    lateinit var repairDate : String
    lateinit var repairTime : String
    lateinit var repairUser : String
    lateinit var reportType : String
    lateinit var reportLong : String
    lateinit var reportLat : String
    lateinit var reportUser : String
    lateinit var reportAddress1 : String
    lateinit var reportAddress2 : String
    lateinit var reportPhoto : String
    lateinit var repairPhoto : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUpdateLeakBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()
        userType = intent.getStringExtra("userType").toString()
        reportID = intent.getStringExtra("reportID").toString()

        binding.imgUpdateRepair.visibility = View.GONE

        binding.btnPhoto.setOnClickListener() {
            showCamera()
        }

        binding.btnUpdate.setOnClickListener() {
            submitReport()
        }

        // Open Google Maps Here
        binding.tvGPS.setEndIconOnClickListener() {
            // Create a Uri from an intent string. Use the result to create an Intent.
            val gmmIntentUri = Uri.parse("geo:0,0?z=18&q=$reportLat,$reportLong(Leak Here)")

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps")

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent)
        }

        reportDao.get()
            .whereEqualTo("repairUser",userName)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    if(data.id == reportID) {

                        reportDate = data["reportDate"].toString()
                        reportTime = data["reportTime"].toString()
                        repairDate = data["repairDate"].toString()
                        repairTime = data["repairTime"].toString()
                        repairUser = data["repairUser"].toString()
                        reportType = data["reportType"].toString()
                        reportLong = data["reportLong"].toString()
                        reportLat = data["reportLat"].toString()
                        reportUser = data["reportUser"].toString()
                        reportAddress1 = data["reportAddress1"].toString()
                        reportAddress2 = data["reportAddress2"].toString()
                        reportPhoto = data["reportPhoto"].toString()

                        // Check leakage type is reported
                        if (reportType == "low") {
                            binding.rbLow.setChecked(true)
                        } else if (reportType == "medium") {
                            binding.rbMedium.setChecked(true)
                        } else if (reportType == "high") {
                            binding.rbHigh.setChecked(true)
                        }

                        val storageRef = FirebaseStorage.getInstance().reference.child("images/$reportPhoto")

                        val localFile = File.createTempFile("tempImage","jpg")
                        storageRef.getFile(localFile).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.imgLeakPhoto.setImageBitmap(bitmap)
                        }
                            .addOnFailureListener {
                                binding.imgLeakPhoto.setImageResource(R.drawable.ic_user)
                            }

                        binding.etGPS.setText("$reportLat,$reportLong")
                        binding.etAddress1.setText(reportAddress1)
                        binding.etAddress2.setText(reportAddress2)

                    }

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

    }

    private fun submitReport() {

        val date = SimpleDateFormat("MM/dd/yyyy")
        repairDate = date.format(Date())

        val time = SimpleDateFormat("hh:mm aaa")
        repairTime = time.format(Date())

        // Initialized date/time format of the filename
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()

        // Initialized filename of the photo for upload and database record
        val filename = "$userName-Repair-${formatter.format(now)}"
        val storageReference = FirebaseStorage.getInstance().getReference("images/$filename")

        repairPhoto = filename

        val builder = AlertDialog.Builder(this@UpdateLeak)
        builder.setMessage("Are you sure to update the report? Make sure all the details are correct as this cannot be changed anymore.")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Delete selected note from database
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Updating report...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Get the data from an ImageView as bytes
                binding.imgUpdateRepair.isDrawingCacheEnabled = true
                binding.imgUpdateRepair.buildDrawingCache()
                val bitmap = (binding.imgUpdateRepair.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                // Add report to database
                updateData(
                    reportID,
                    reportDate,
                    reportTime,
                    repairDate,
                    repairTime,
                    repairUser,
                    reportType,
                    reportLong,
                    reportLat,
                    reportUser,
                    reportAddress1,
                    reportAddress2,
                    reportPhoto,
                    repairPhoto
                )

                // Save photo to database
                storageReference.putBytes(data)
                    .addOnSuccessListener {
                        binding.imgUpdateRepair.setImageURI(null) // Removes photo from imageview
                        Toast.makeText(applicationContext,"Successfully Updated",Toast.LENGTH_LONG).show()
                        if(progressDialog.isShowing) progressDialog.dismiss()

                        val intent = Intent(this, Plumber::class.java)
                        intent.putExtra("userName", userName)
                        intent.putExtra("userType", userType)
                        finish()
                        startActivity(intent)

                    }.addOnFailureListener{
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        Toast.makeText(applicationContext,"Update Failed",Toast.LENGTH_LONG).show()
                    }

            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }

        val alert = builder.create()
        alert.show()


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
                binding.imgUpdateRepair.setImageBitmap(image) // Attaches captured photo to imageview
                binding.imgUpdateRepair.visibility = View.VISIBLE // Shows the photo being captured by camera
            }
        }
    }

    private fun updateData(
        id : String,
        reportDate : String,
        reportTime : String,
        repairDate : String,
        repairTime : String,
        repairUser : String,
        reportType : String,
        reportLong : String,
        reportLat : String,
        reportUser : String,
        reportAddress1 : String,
        reportAddress2 : String,
        reportPhoto : String,
        repairPhoto : String
    ) {
        var mapData = mutableMapOf<String,String>()
        mapData["reportDate"] = reportDate
        mapData["reportTime"] = reportTime
        mapData["repairDate"] = repairDate
        mapData["repairTime"] = repairTime
        mapData["repairUser"] = repairUser
        mapData["reportType"] = reportType
        mapData["reportLong"] = reportLong
        mapData["reportLat"] = reportLat
        mapData["reportUser"] = reportUser
        mapData["reportAddress1"] = reportAddress1
        mapData["reportAddress2"] = reportAddress2
        mapData["reportPhoto"] = reportPhoto
        mapData["repairPhoto"] = repairPhoto
        reportDao.update(id,mapData)
    }

    override fun onBackPressed() {

        // To execute back press
        // super.onBackPressed()

        // To do something else
        val intent = Intent(this, Repair::class.java)
        intent.putExtra("userName", userName)
        intent.putExtra("userType", userType)
        finish()
        startActivity(intent)

    }

}