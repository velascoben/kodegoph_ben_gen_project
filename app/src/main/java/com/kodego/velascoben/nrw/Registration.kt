package com.kodego.velascoben.nrw

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.kodego.velascoben.nrw.databinding.ActivityRegistrationBinding
import com.kodego.velascoben.nrw.db.Users
import com.kodego.velascoben.nrw.db.UsersDao
import java.security.MessageDigest
import java.util.*
import javax.crypto.*


class Registration : AppCompatActivity() {

    lateinit var binding : ActivityRegistrationBinding

    private var dao = UsersDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener() {
            var type : String = ""
            val firstname = binding.etFirstName.text.toString()
            val lastname = binding.etLastName.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString().toMD5()

            dao.get()
                .whereEqualTo("userName",username)
                .get()
                .addOnSuccessListener { snapShot ->

                    if(snapShot.isEmpty) {
                        if (binding.rbDetector.isChecked) {
                            type = "detector"
                        } else if (binding.rbPlumber.isChecked) {
                            type = "plumber"
                        }

                        dao.add(Users(firstname,lastname,username, password,type,""))

                        // Clear
                        binding.etFirstName.setText("")
                        binding.etLastName.setText("")
                        binding.etUsername.setText("")
                        binding.etPassword.setText("")

                        binding.rgType.clearCheck()

                        // create an intent to switch to MainActivity after adding new user
                        val intent = Intent(this, Login::class.java)
                        finish()
                        startActivity(intent)

                        Toast.makeText(applicationContext,"User added successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Username already exists. Choose another one.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
                }
        }
    }


    // Password Encryption and Decryption
    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    override fun onBackPressed() {
        // To execute back press
        // super.onBackPressed()

        // To do something else

        val intent = Intent(this, Login::class.java)
        finish()
        startActivity(intent)

    }
}