package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
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

class Signup : AppCompatActivity() {

    private lateinit var  emailText : TextInputEditText
    private lateinit var  nameText : TextInputEditText
    private lateinit var  passwordText : TextInputEditText
    private lateinit var submitButton: Button
    private lateinit var loginNow: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var roleDropdown: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailText = findViewById(R.id.email)
        nameText = findViewById(R.id.name)
        passwordText = findViewById(R.id.password)
        submitButton = findViewById(R.id.submit)
        errorText = findViewById(R.id.error)
        progressBar = findViewById(R.id.loading)
        loginNow = findViewById(R.id.login)
        roleDropdown = findViewById(R.id.user_role_dropdown)

        loginNow.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

//        roleDropdown.setOnItemClickListener { parent, _, position, _ ->
//            val str = parent.getItemAtPosition(position) as String
//        }

        submitButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            errorText.visibility = View.GONE

            val queue: RequestQueue = Volley.newRequestQueue(this)
            val url = Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_SIGNUP
            val postData = JSONObject()
            postData.put("email", emailText.text)
            postData.put("password", passwordText.text)
            postData.put("role", roleDropdown.text)
            val request =
                JsonObjectRequest(Request.Method.POST, url, postData, { response ->

                    progressBar.visibility = View.GONE
                    Log.i("TAG", "${Constants.API_PATH_SIGNUP} response: "+response.toString())
                    Toast.makeText(this,"Signup Successful",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()

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
                    Log.e("TAG", "${Constants.API_PATH_SIGNUP} error: "+errorMessage)
                })

            queue.add(request)
        }

    }
}