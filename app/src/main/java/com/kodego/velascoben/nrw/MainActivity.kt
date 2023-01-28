package com.kodego.velascoben.nrw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kodego.velascoben.nrw.databinding.ActivityMainBinding
import com.kodego.velascoben.nrw.db.Users

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // SESSIONS
        // Data Source
        var sessionList: MutableList<Sessions> = mutableListOf<Sessions>(
            Sessions("2:30AM", "Jan 30", "Emergency NRW Session"),
            Sessions("2:30AM", "Feb 15", "Scheduled NRW Session"),
            Sessions("2:30AM", "Mar 15", "Scheduled NRW Session"),
            Sessions("2:00AM", "Apr 1", "Scheduled NRW Session"),
            Sessions("2:30AM", "Apr 30", "Scheduled NRW Session"),
        )

        // Initialization
        val adapter = SessionAdapter(sessionList)

        // Binding Adapter
        binding.rvSessions.adapter = adapter
        binding.rvSessions.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)


        // SESSIONS
        // Data Source
        var usersList: MutableList<Users> = mutableListOf<Users>(
            Users("Ben", "Velasco","ben20", "","detector",R.drawable.ic_user),
            Users("Eugene", "Santos","eugeneRaygun", "","plumber",R.drawable.ic_user),
            Users("Albert", "Velasco","albertMC2", "","detector",R.drawable.ic_user),
            Users("Benny", "Valdez","metaBenny", "","plumber",R.drawable.ic_user),
            Users("Bench", "Mark","pioneerMe", "","detector",R.drawable.ic_user),
        )

        // Initialization
        val adapterUsers = UsersAdapter(usersList)

        // Binding Adapter
        binding.rvUsers.adapter = adapterUsers
        binding.rvUsers.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)

        // Get data from previous screen
        //var name: String? = intent.getStringExtra("nameID")

        //binding.txtWelcome.text = "Welcome Back, $name!"

        binding.btnReportLeak.setOnClickListener() {
            val intent = Intent(this, LeakReport::class.java)
            startActivity(intent)
        }

    }
}