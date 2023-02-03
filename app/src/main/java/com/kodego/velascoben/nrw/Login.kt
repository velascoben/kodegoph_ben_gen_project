package com.kodego.velascoben.nrw

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.auth.User
import com.kodego.velascoben.nrw.databinding.ActivityLoginBinding
import com.kodego.velascoben.nrw.db.Users
import com.kodego.velascoben.nrw.db.UsersDao
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

class Login : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    private var dao = UsersDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Reference to TextView
        val tvForgotten = binding.tvForgotPass
        // Set On-click Listener
        tvForgotten.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        // Get Reference to TextView
        val tvRegisterHere = binding.tvRegister
        // Set On-click Listener
        tvRegisterHere.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener() {

            if((binding.etUsername.text.toString() == "") || (binding.etPassword.text.toString() == "")) {

                Toast.makeText(applicationContext, "Username or password cannot be empty", Toast.LENGTH_LONG).show()

            } else {

            dao.get()
                .whereEqualTo("userName", binding.etUsername.text.toString())
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->

                    for (data in queryDocumentSnapshots.documents) {

                        var id = data.id

                        var username = data["userName"].toString()
                        var password = data["userPass"].toString()
                        var type = data["userType"].toString()

                        if ((username == binding.etUsername.text.toString()) && (password == binding.etPassword.text.toString()
                                .toMD5())
                        ) {

                            if (type == "detector") {

                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("userName", username)
                                intent.putExtra("userType", type)
                                Toast.makeText(
                                    applicationContext,
                                    "Logged in...",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                                startActivity(intent)

                            } else if (type == "plumber") {

                                val intent = Intent(this, Plumber::class.java)
                                intent.putExtra("userName", username)
                                intent.putExtra("userType", type)
                                Toast.makeText(applicationContext,"Logged in...",Toast.LENGTH_LONG).show()
                                finish()
                                startActivity(intent)

                            } else {

                                val intent = Intent(this, Admin::class.java)
                                intent.putExtra("userName", username)
                                Toast.makeText(
                                    applicationContext,
                                    "Logged in...",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                                startActivity(intent)

                            }

                        } else {

                            Toast.makeText(
                                applicationContext,
                                "Incorrect username or password",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "User does not exist", Toast.LENGTH_LONG).show()
                }
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
}