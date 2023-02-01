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
import com.google.firebase.storage.FirebaseStorage
import com.kodego.velascoben.nrw.databinding.ActivityChangePasswordBinding
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class ChangePassword : AppCompatActivity() {

    lateinit var binding : ActivityChangePasswordBinding
    private var userDao = UsersDao()
    lateinit var userName : String

    lateinit var userID : String
    lateinit var userPass : String
    lateinit var userType : String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()
        userDao.get()
            .whereEqualTo("userName",userName) // Gets the data from the database from the logged in user
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    userID = data.id
                    userType = data["userType"].toString()

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }


        binding.btnNewPassword.setOnClickListener() {

            // Checks if confirm password is the same as the password
            if(binding.etPassword.text.toString() == binding.etConfirmPassword.text.toString()) {

                // Check if password is empty - if not empty, gets the new password and encrypts it
                if (binding.etPassword.text.toString() != "") {
                    userPass = binding.etPassword.text.toString().toMD5()
                }

                val builder = AlertDialog.Builder(this@ChangePassword)
                builder.setMessage("Are you sure you want to update your password?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        // Delete selected note from database
                        val progressDialog = ProgressDialog(this)
                        progressDialog.setMessage("Updating password...")
                        progressDialog.setCancelable(false)
                        progressDialog
                        progressDialog.show()

                        // Call function to save data to database
                        updateData(
                            userID,
                            userPass,
                        )
                        if(progressDialog.isShowing) progressDialog.dismiss()

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

                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }

                val alert = builder.create()
                alert.show()

            } else {

                Toast.makeText(applicationContext,"Password are not the same. Try Again.", Toast.LENGTH_LONG).show()

            }

        }

    }

    private fun updateData(
        id : String,
        userPass : String
    ) {
        var mapData = mutableMapOf<String,String>()
        mapData["userPass"] = userPass
        userDao.update(id,mapData)
        Toast.makeText(
            applicationContext,
            "Password Updated",
            Toast.LENGTH_LONG
        ).show()
    }

    // Encrypt Password - Start Here
    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
    // Encrypt Password - End Here
}