package com.kodego.velascoben.nrw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kodego.velascoben.nrw.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Data Source
        var sessionList: MutableList<Sessions> = mutableListOf<Sessions>(
            Sessions("2:30AM", "Jan 30", "Emergency NRW Session"),
            Sessions("2:30AM", "Feb 15", "Scheduled NRW Session"),
            Sessions("2:30AM", "Mar 15", "Scheduled NRW Session"),
            Sessions("2:00AM", "Apr 1", "Scheduled NRW Session"),
            Sessions("2:30AM", "Apr 30", "Scheduled NRW Session"),
        )
        val adapter = SessionAdapter(sessionList)
        binding.rvSessions.adapter = adapter
        binding.rvSessions.layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)


        // Get data from previous screen
        //var name: String? = intent.getStringExtra("nameID")

        //binding.txtWelcome.text = "Welcome Back, $name!"

        binding.btnReportLeak.setOnClickListener() {
            val intent = Intent(this, LeakReport::class.java)
            startActivity(intent)
        }

    }
}