package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class Login : AppCompatActivity() {

    private lateinit var registerNow: TextView
    private lateinit var emailText : TextInputEditText
    private lateinit var passwordText : TextInputEditText
    private lateinit var errorText : TextView
    private lateinit var submitButton : Button
    private lateinit var progressBar : ProgressBar
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerNow = findViewById(R.id.register)
        emailText = findViewById(R.id.email)
        passwordText = findViewById(R.id.password)
        submitButton = findViewById(R.id.submit)
        errorText = findViewById(R.id.error)
        progressBar = findViewById(R.id.loading)
        sharedPreferences = getSharedPreferences("MyAppPref", MODE_PRIVATE)

        // if already apikey open homepage
        // TODO: check if already access token, but it is expired, so use refresh token to refresh it
        if(sharedPreferences.getString("access_token",null)!=null || sharedPreferences.getString("refresh_token",null)!=null){
            openHomepage()
        }

        submitButton.setOnClickListener{

            progressBar.visibility = View.VISIBLE
            errorText.visibility  = View.GONE

            val queue: RequestQueue = Volley.newRequestQueue(this)
            val url = Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_LOGIN
            val postData = JSONObject()
            postData.put("email", emailText.text)
            postData.put("password", passwordText.text)
            val request = JsonObjectRequest(Request.Method.POST, url, postData, { response ->

                progressBar.visibility = View.GONE
                Log.i("TAG", "${Constants.API_PATH_LOGIN} response: "+response.toString())
                Toast.makeText(this,"Login successful",Toast.LENGTH_SHORT).show()
                try {
                    val editor = sharedPreferences.edit()
                    editor.putString("access_token",response.getJSONObject("user").getString("access_token"))
                    editor.putString("refresh_token",response.getJSONObject("user").getString("refresh_token"))
                    editor.apply()
                    openHomepage()

                }catch (e: Exception){
                    errorText.text = e.message
                    errorText.visibility = View.VISIBLE
                    Log.e("TAG", "JSONException: "+e.message)
                }

            }, { error ->
                var errorMessage = "Some error occurred"
                try {
                    val errorJson = JSONObject(String(error.networkResponse.data))
                    errorMessage = errorJson.getString("message")
                }catch (_: Exception){
                }

                progressBar.visibility = View.GONE
                errorText.text = errorMessage
                errorText.visibility = View.VISIBLE
                Log.e("TAG", "${Constants.API_PATH_LOGIN} error: "+errorMessage)
            })
            queue.add(request)
        }

        registerNow.setOnClickListener{
            val intent = Intent(this,Signup::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openHomepage(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}