package com.kodego.velascoben.nrw

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.kodego.velascoben.nrw.databinding.ActivityMainBinding
import com.kodego.velascoben.nrw.db.Reports
import com.kodego.velascoben.nrw.db.ReportsDao
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var userDao = UsersDao()
    private var reportDao = ReportsDao()
    lateinit var userName : String
    lateinit var userType : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()
        userType = intent.getStringExtra("userType").toString()
        var countReports : Int
        var userReports : Int

        userDao.get()
            .whereEqualTo("userName",userName)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    binding.tvFirstName.text = data["userFirst"].toString()
                    val userPhoto = data["userPhoto"].toString()

                    if(userPhoto != "") {
                        // Retrieve Image
                        val storageRef = FirebaseStorage.getInstance().reference.child("images/$userPhoto")
                        val localFile = File.createTempFile("tempImage","jpg")
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.userImage.setImageBitmap(bitmap)
                            }

                    } else {
                        binding.userImage.setImageResource(R.drawable.ic_user)
                    }

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

        binding.userImage.setOnClickListener() {
            val intent = Intent(this, Options::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("userType", userType)
            startActivity(intent)
        }

        binding.imgInformation.setOnClickListener() {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("This app was designed and created by Ben & Gen")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        reportDao.get()
            .whereEqualTo("reportUser",userName)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                val reports : ArrayList<Reports> = ArrayList<Reports>()

                for (data in queryDocumentSnapshots.documents) {

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

                    val report = Reports("",reportDate,reportTime,repairDate,repairTime,repairUser,reportType,reportLong,reportLat,reportUser,reportAddress1,reportAddress2,reportPhoto,repairPhoto)
                    reports.add(report)

                }

                // Initialization
                val adapter = ReportsAdapter(reports)

                // Binding Adapter
                binding.rvReports.adapter = adapter
                binding.rvReports.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

        // Top Leakage Reporters
        userDao.get()
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                val users: MutableList<SortedUsers> = mutableListOf()

                for (data in queryDocumentSnapshots.documents) {

                    val username = data["userName"].toString()
                    val usertype = data["userType"].toString()
                    val userPhoto = data["userPhoto"].toString()

                    if(usertype == userType) {

                        reportDao.get()
                            .get()
                            .addOnSuccessListener { snapShots ->

                                userReports = 0
                                countReports = 0

                                for (info in snapShots.documents) {

                                    val reportUser = info["reportUser"].toString()

                                    if (username == reportUser) {
                                        countReports++
                                    }

                                    if (reportUser == userName) {
                                        userReports++
                                    }

                                }

                                binding.tvUserReport.text = userReports.toString()

                                if (countReports > 0) {
                                    val user = SortedUsers(
                                        countReports,
                                        username,
                                        userPhoto,
                                    )

                                    users.add(user)

                                // Initialize sorting
                                users.sortByDescending { it.userCount }
                                val adapter = UsersAdapter(users)

                                // Binding Adapter
                                binding.rvUsers.adapter = adapter
                                binding.rvUsers.layoutManager = LinearLayoutManager(
                                    applicationContext,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            }

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Error getting documents: ",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    }
                }

                }


            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

        binding.btnReportLeak.setOnClickListener() {
            val mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Checking GPS is enabled
            val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Check GPS status
           if(mGPS) {

               val intent = Intent(this, LeakReport::class.java)
               intent.putExtra("userName", userName)
               intent.putExtra("userType", userType)
               finish()
               startActivity(intent)

           } else {

               Toast.makeText(applicationContext,"Your GPS is disabled. Turn it on and try again. ", Toast.LENGTH_LONG).show()

           }
        }

    }

    // Press Back Button
    override fun onBackPressed() {

        // To execute back press
        // super.onBackPressed()

        // To do something else
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("You will be logged out. Do you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Delete selected note from database
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Logging out...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    // Logout of the app
                    val intent = Intent(this, Login::class.java)
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