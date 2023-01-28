package com.kodego.velascoben.nrw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.kodego.velascoben.nrw.databinding.ActivityLoginBinding
import com.kodego.velascoben.nrw.db.UsersDao

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

            dao.get()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}