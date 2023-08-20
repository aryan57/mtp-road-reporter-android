package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class Signup : AppCompatActivity() {

    private lateinit var emailText: TextInputEditText
    private lateinit var nameText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var submitButton: Button
    private lateinit var loginNow: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var roleDropdown: AutoCompleteTextView
    private lateinit var apiRequestHandler: ApiRequestHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        apiRequestHandler = ApiRequestHandler(this)
        emailText = findViewById(R.id.email)
        nameText = findViewById(R.id.name)
        passwordText = findViewById(R.id.password)
        submitButton = findViewById(R.id.submit)
        errorText = findViewById(R.id.error)
        progressBar = findViewById(R.id.loading)
        loginNow = findViewById(R.id.login)
        roleDropdown = findViewById(R.id.user_role_dropdown)

        loginNow.setOnClickListener {
            openLoginPage()
        }

        submitButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            errorText.visibility = View.GONE
            submitButton.isEnabled = false

            val postData = JSONObject()
            postData.put("email", emailText.text)
            postData.put("password", passwordText.text)
            postData.put("role", roleDropdown.text)
            postData.put("name", nameText.text)

            apiRequestHandler.makeApiRequest(Request.Method.POST,
                Constants.API_PATH_SIGNUP,
                postData,
                {
                    Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                    openLoginPage()
                },
                { errorMessage ->
                    errorText.text = errorMessage
                    errorText.visibility = View.VISIBLE
                },
                {
                    submitButton.isEnabled = true
                    progressBar.visibility = View.GONE
                })
        }

    }

    private fun openLoginPage() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}