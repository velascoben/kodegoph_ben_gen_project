package com.kodego.velascoben.nrw

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kodego.velascoben.nrw.databinding.ActivityRegistrationBinding
import com.kodego.velascoben.nrw.db.Users
import com.kodego.velascoben.nrw.db.UsersDao
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class Registration : AppCompatActivity() {

    lateinit var binding : ActivityRegistrationBinding

    private var dao = UsersDao()

    private val SECRETKEY = "NRWAppKey"
    private val SECRETIV = "NRWAppIV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener() {
            var type : String = ""
            val firstname = binding.etFirstName.text.toString()
            val lastname = binding.etLastName.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (binding.rbDetector.isChecked) {
                type = "detector"
            } else if (binding.rbPlumber.isChecked) {
                type = "plumber"
            }

            dao.add(Users(firstname,lastname,username,password,type,0))

            // Clear
            binding.etFirstName.setText("")
            binding.etLastName.setText("")
            binding.etUsername.setText("")
            binding.etPassword.setText("")

            binding.rgType.clearCheck()

            // create an intent to switch to MainActivity after adding new user
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

            Toast.makeText(applicationContext,"User added successfully!", Toast.LENGTH_LONG).show()

        }
    }

    fun String.encryptCBC(): String {
        val iv = IvParameterSpec(SECRETIV.toByteArray())
        val keySpec = SecretKeySpec(SECRETKEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val crypted = cipher.doFinal(this.toByteArray())
        val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
        return String(encodedByte)
    }
}