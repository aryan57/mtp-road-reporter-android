package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.TokenManager
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class Login : AppCompatActivity() {

    private lateinit var registerNow: TextView
    private lateinit var emailText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var errorText: TextView
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiRequestHandler: ApiRequestHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        TokenManager.init(this) // login is our launcher activity, so initialise the tokens in this activity

        apiRequestHandler = ApiRequestHandler(this)
        registerNow = findViewById(R.id.register)
        emailText = findViewById(R.id.email)
        passwordText = findViewById(R.id.password)
        submitButton = findViewById(R.id.submit)
        errorText = findViewById(R.id.error)
        progressBar = findViewById(R.id.loading)

        checkFromStoredTokens()

        submitButton.setOnClickListener {

            submitButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            errorText.visibility = View.GONE

            val postData = JSONObject()
            postData.put("email", emailText.text)
            postData.put("password", passwordText.text)

            apiRequestHandler.makeApiRequest(Request.Method.POST,
                Constants.API_PATH_LOGIN,
                postData,
                { response ->

                    try {
                        TokenManager.setAccessToken(
                            response.getJSONObject("user").getString("access_token")
                        )
                        TokenManager.setRefreshToken(
                            response.getJSONObject("user").getString("refresh_token")
                        )
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        openHomepage()

                    } catch (e: Exception) {
                        errorText.text = e.message
                        errorText.visibility = View.VISIBLE
                        Log.e("TAG", "error parsing json : $response")
                    }
                },
                { errorMessage ->

                    errorText.text = errorMessage
                    errorText.visibility = View.VISIBLE
                },
                {
                    progressBar.visibility = View.GONE
                    submitButton.isEnabled = true
                })
        }

        registerNow.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkFromStoredTokens() {
        if (!TokenManager.hasTokens()) return

        progressBar.visibility = View.VISIBLE
        // if user already logged in with the saved tokens go to homepage
        apiRequestHandler.makeApiRequest(Request.Method.GET,
            Constants.API_PATH_WHOAMI,
            null,
            { response ->
                try {
                    val name = response.getJSONObject("user").getString("name")
                    Toast.makeText(this, "Hello: $name", Toast.LENGTH_SHORT).show()
                    openHomepage()
                } catch (e: Exception) {
                    errorText.text = e.message
                    errorText.visibility = View.VISIBLE
                    Log.e("TAG", "error parsing json : $response")
                }
            },
            {
                // could not login from stored tokens, so user has to re-login
                TokenManager.clearTokens()
            },
            {
                progressBar.visibility = View.GONE
            })
    }

    private fun openHomepage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}