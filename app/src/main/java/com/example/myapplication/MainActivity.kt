package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var logoutButton: Button
    private lateinit var email: TextView
    private lateinit var fetchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("MyAppPref", MODE_PRIVATE)
        logoutButton = findViewById(R.id.logout)
        fetchButton = findViewById(R.id.fetch_profile)
        email = findViewById(R.id.email)
        email.text = sharedPreferences.getString("access_token","not found")

        logoutButton.setOnClickListener{
            doLogout()
        }

        fetchButton.setOnClickListener{
            val queue: RequestQueue = Volley.newRequestQueue(this)
            val url = Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_WHOAMI
            val request = object : JsonObjectRequest(Request.Method.GET, url, null, { response ->
                Log.i("TAG", "${Constants.API_PATH_WHOAMI} response: "+response.toString())
                Toast.makeText(this,response.toString(),Toast.LENGTH_SHORT).show()
            }, { error ->
                Log.e("TAG", "${Constants.API_PATH_WHOAMI} error: "+error.message)
            })
            {
                // Override the headers for the request
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${sharedPreferences.getString("access_token","not found")}"
                    return headers
                }
            }
            queue.add(request)
        }
    }

    private fun doLogout(){
        val editor = sharedPreferences.edit()
        editor.putString("access_token",null)
        editor.putString("refresh_token",null)
        editor.apply()
        val intent = Intent(this,Login::class.java)
        startActivity(intent)
        finish()
    }
}