package com.kodego.velascoben.nrw

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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.kodego.velascoben.nrw.databinding.ActivityProfileBinding
import com.kodego.velascoben.nrw.db.ReportsDao
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {

    lateinit var binding : ActivityProfileBinding
    private var userDao = UsersDao()
    private lateinit var userName : String

    lateinit var userID : String
    lateinit var userFirst : String
    lateinit var userLast : String
    lateinit var userPass : String
    lateinit var userType : String
    lateinit var userPhoto : String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityProfileBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()

        binding.userImage.setOnClickListener() {
            showGallery()
        }

        binding.btnUpdateProfile.setOnClickListener() {
            submitReport()
        }

        userDao.get()
            .whereEqualTo("userName",userName) // Gets the data from the database from the logged in user
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    userID = data.id // Gets the document ID from the database
                    userFirst = data["userFirst"].toString()
                    userLast = data["userLast"].toString()
                    userName = data["userName"].toString()
                    userPass = data["userPass"].toString()
                    userType = data["userType"].toString()

                    binding.etFirstName.setText(userFirst)
                    binding.etLastName.setText(userLast)
                    binding.etUsername.setText(userName)

                    var userPhoto = data["userPhoto"].toString()

                    if(userPhoto == "") {
                        binding.userImage.setImageResource(R.drawable.user)
                    } else {
                        // Retrieve Image
                        val imageName = userPhoto
                        val storageRef = FirebaseStorage.getInstance().reference.child("images/$imageName")
                        val localFile = File.createTempFile("tempImage","jpg")
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.userImage.setImageBitmap(bitmap)
                            }
                    }

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

    }

    private fun submitReport() {

            if (binding.etFirstName.text.toString() != "") {
                userFirst = binding.etFirstName.text.toString()
            }

            if (binding.etLastName.text.toString() != "") {
                userLast = binding.etLastName.text.toString()
            }

            // Initialized filename of the photo for upload and database record
            val filename = "$userName-profile"
            val storageReference = FirebaseStorage.getInstance().getReference("images/$filename")

            userPhoto = filename // Filename is saved to variable ready to be saved in the Database

            val builder = AlertDialog.Builder(this@Profile)
            builder.setMessage("Are you sure to update your profile?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Delete selected note from database
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Updating profile...")
                    progressDialog.setCancelable(false)
                    progressDialog
                    progressDialog.show()

                    // Get the data from an ImageView as bytes
                    binding.userImage.isDrawingCacheEnabled = true
                    binding.userImage.buildDrawingCache()
                    val bitmap = (binding.userImage.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    // Call function to save data to database
                    updateData(
                        userID,
                        userFirst,
                        userLast,
                        userPass,
                        userPhoto
                    )

                    // Save photo to database
                    storageReference.putBytes(data)
                        .addOnSuccessListener {
                            binding.userImage.setImageURI(null) // Removes photo from imageview
                            Toast.makeText(
                                applicationContext,
                                "Successfully Updated",
                                Toast.LENGTH_LONG
                            ).show()
                            if (progressDialog.isShowing) progressDialog.dismiss()

                            if(userType == "detector") {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("userName", userName)
                                finish()
                                startActivity(intent)
                            } else {
                                val intent = Intent(this, Plumber::class.java)
                                intent.putExtra("userName", userName)
                                finish()
                                startActivity(intent)
                            }

                        }.addOnFailureListener {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                            Toast.makeText(applicationContext, "Update Failed", Toast.LENGTH_LONG)
                                .show()
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

    private fun showGallery() {
        // Checks permission for file storage access
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                gotoSettings()
            }

            override fun onPermissionRationaleShouldBeShown(
                request: PermissionRequest?,
                token: PermissionToken?,
            ) {
                token?.continuePermissionRequest()
            }

        }).onSameThread().check()
    }


    // Opens Gallery Here
    val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val selectedImage = result.data?.data
                binding.userImage.setImageURI(selectedImage)
            }
        }
    }

    // Update Data - Start Here

    private fun updateData(
        id : String,
        userFirst : String,
        userLast : String,
        userPass : String,
        userPhoto : String
    ) {
        var mapData = mutableMapOf<String,String>()
        mapData["userFirst"] = userFirst
        mapData["userLast"] = userLast
        mapData["userPass"] = userPass
        mapData["userPhoto"] = userPhoto
        userDao.update(id,mapData)
    }

    // Update Data - End Here

}