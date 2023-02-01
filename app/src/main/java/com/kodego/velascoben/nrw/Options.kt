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
import com.kodego.velascoben.nrw.databinding.ActivityOptionsBinding
import com.kodego.velascoben.nrw.db.UsersDao
import java.io.ByteArrayOutputStream
import java.io.File

class Options : AppCompatActivity() {

    lateinit var binding : ActivityOptionsBinding
    private lateinit var userName : String
    private var userDao = UsersDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOptionsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName").toString()

        userDao.get()
            .whereEqualTo("userName",userName)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->

                for (data in queryDocumentSnapshots.documents) {

                    binding.tvFirstName.text = data["userFirst"].toString()
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
                        binding.userImage.setImageResource(R.drawable.ic_user)
                    }

                }

            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Error getting documents: ", Toast.LENGTH_LONG).show()
            }



        binding.btnUpdateInfo.setOnClickListener() {
            val intent = Intent(this, Profile::class.java)
            intent.putExtra("userName", userName)
            finish()
            startActivity(intent)
        }

        binding.btnUpdatePass.setOnClickListener() {
            val intent = Intent(this, ChangePassword::class.java)
            intent.putExtra("userName", userName)
            finish()
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener() {
            val builder = AlertDialog.Builder(this@Options)
            builder.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Delete selected note from database
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Logging out...")
                    progressDialog.setCancelable(false)
                    progressDialog
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
}