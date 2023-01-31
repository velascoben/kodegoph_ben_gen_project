package com.kodego.velascoben.nrw

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.kodego.velascoben.nrw.databinding.ActivityMainBinding
import com.kodego.velascoben.nrw.databinding.ActivityPlumberBinding
import com.kodego.velascoben.nrw.db.Reports
import com.kodego.velascoben.nrw.db.ReportsDao
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.ByteArrayOutputStream
import java.io.File

class Plumber : AppCompatActivity() {

    lateinit var binding: ActivityPlumberBinding
    private var userDao = UsersDao()
    private var reportDao = ReportsDao()
    private lateinit var userName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPlumberBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()

        userDao.get()
            .whereEqualTo("userName",userName) // Gets the data from the database from the logged in user
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    var userPhoto = data["userPhoto"].toString()

                    if(userPhoto != "") {
                        // Retrieve Image
                        val imageName = userPhoto
                        val storageRef = FirebaseStorage.getInstance().reference.child("images/$imageName")
                        val localFile = File.createTempFile("tempImage","jpg")
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.userImage.setImageBitmap(bitmap)
                            }

                    } else {
                        binding.userImage.setImageResource(R.drawable.user)
                    }

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

        view()

        binding.btnRepairList.setOnClickListener() {
            val intent = Intent(this, Repair::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        binding.userImage.setOnClickListener() {
            val intent = Intent(this, Options::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        userDao.get()
            .whereEqualTo("userName",userName)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    binding.tvFirstName.text = data["userFirst"].toString()
                    var userPhoto = data["userPhoto"].toString()

                    if(userPhoto == "") {
                        binding.userImage.setImageResource(R.drawable.ic_user)
                    } else {
                        // Retrieve Image
                        val imageName = userPhoto
                        val storageRef = FirebaseStorage.getInstance().reference.child("images/$imageName.jpg")
                        val localFile = File.createTempFile("tempImage","jpg")
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.userImage.setImageBitmap(bitmap)
                            }


                    }

//                        var id = data.id
//
//                        var username = data["userName"].toString()
                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

    }

    fun view() {
        reportDao.get()
            .whereEqualTo("repairUser","")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                var reports : ArrayList<Reports> = ArrayList<Reports>()

                for (data in queryDocumentSnapshots.documents) {

                    var reportID = data.id
                    val reportDate = data["reportDate"].toString()
                    val reportTime = data["reportTime"].toString()
                    val repairDate = data["repairDate"].toString()
                    val repairTime = data["repairTime"].toString()
                    val repairUser = data["repairUser"].toString()
                    val reportType = data["reportType"].toString()
                    val reportLong = data["reportLong"].toString()
                    val reportLat = data["reportLat"].toString()
                    val reportUser = data["reportUser"].toString()
                    val reportAddress1 = data["reportAddress1"].toString()
                    val reportAddress2 = data["reportAddress2"].toString()
                    val reportPhoto = data["reportPhoto"].toString()
                    val repairPhoto = data["repairPhoto"].toString()

                    var report = Reports(reportID,reportDate,reportTime,repairDate,repairTime,repairUser,reportType,reportLong,reportLat,reportUser,reportAddress1,reportAddress2,reportPhoto,repairPhoto)
                    reports.add(report)

                }

                // Initialization
                val adapter = RepairsAdapter(reports)

                // Binding Adapter
                binding.rvReports.adapter = adapter
                binding.rvReports.layoutManager = LinearLayoutManager(applicationContext,
                    LinearLayoutManager.HORIZONTAL,false)

                adapter.onUpdate = {
                        item : Reports, position : Int ->

                    val builder = AlertDialog.Builder(this@Plumber)
                    builder.setMessage("Are you sure to book this leakage for repair?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, id ->
                            // Delete selected note from database
                            val progressDialog = ProgressDialog(this)
                            progressDialog.setMessage("Booking...")
                            progressDialog.setCancelable(false)
                            progressDialog
                            progressDialog.show()

                            // Update report to database
                            updateData(userName,item.reportID)
                            Toast.makeText(applicationContext,"Successfully Booked",Toast.LENGTH_LONG).show()
                            if(progressDialog.isShowing) progressDialog.dismiss()
                            adapter.notifyDataSetChanged()
                            val intent = Intent(this, Plumber::class.java)
                            intent.putExtra("userName", userName)
                            finish()
                            startActivity(intent)

                        }
                        .setNegativeButton("No") { dialog, id ->
                            // Dismiss the dialog
                            dialog.dismiss()
                        }

                    val alert = builder.create()
                    alert.show()

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }
    }

    // Updates database that the plumber has booked this leakage for repair
    private fun updateData(repairUser : String, id : String) {
        var mapData = mutableMapOf<String,String>()
        mapData["repairUser"] = repairUser
        reportDao.update(id,mapData)
    }
}