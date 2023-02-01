package com.kodego.velascoben.nrw

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.kodego.velascoben.nrw.databinding.ActivityPlumberBinding
import com.kodego.velascoben.nrw.databinding.ActivityRepairBinding
import com.kodego.velascoben.nrw.db.Reports
import com.kodego.velascoben.nrw.db.ReportsDao
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.File

class Repair : AppCompatActivity() {

    lateinit var binding: ActivityRepairBinding
    private var userDao = UsersDao()
    private var reportDao = ReportsDao()
    private lateinit var userName : String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityRepairBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()

        reportDao.get()
            .whereEqualTo("repairUser",userName).whereEqualTo("repairDate","")
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
                binding.rvReports.layoutManager = GridLayoutManager(applicationContext,2)

                adapter.onUpdate = {
                        item : Reports, position : Int ->

                        val intent = Intent(this, UpdateLeak::class.java)
                        intent.putExtra("userName", userName)
                        intent.putExtra("reportID", item.reportID)
                        finish()
                        startActivity(intent)
                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }

    }
}