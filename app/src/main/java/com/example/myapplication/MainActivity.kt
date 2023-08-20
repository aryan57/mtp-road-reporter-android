package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var email: TextView
    private lateinit var fetchButton: Button
    private lateinit var apiRequestHandler: ApiRequestHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiRequestHandler = ApiRequestHandler(this)
        logoutButton = findViewById(R.id.logout)
        fetchButton = findViewById(R.id.fetch_profile)
        email = findViewById(R.id.email)

        logoutButton.setOnClickListener {
            apiRequestHandler.logoutUser()
        }

        fetchButton.setOnClickListener {
            fetchButton.isEnabled = false
            apiRequestHandler.makeApiRequest(Request.Method.GET,
                Constants.API_PATH_WHOAMI,
                null,
                { response ->
                    Toast.makeText(this, response.toString(), Toast.LENGTH_SHORT).show()
                },
                {},
                {
                    fetchButton.isEnabled = true
                })
        }
    }
}