package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants

class ProfileFragment : Fragment() {

    private lateinit var logoutButton: Button
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileRole: TextView
    private lateinit var errorText: TextView
    private lateinit var apiRequestHandler: ApiRequestHandler
    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false)

        apiRequestHandler = ApiRequestHandler(view.context)
        logoutButton = view.findViewById(R.id.logout)
        profileName = view.findViewById(R.id.profile_name)
        profileEmail = view.findViewById(R.id.profile_email)
        profileRole = view.findViewById(R.id.profile_role)
        errorText = view.findViewById(R.id.error)


        logoutButton.setOnClickListener {
            apiRequestHandler.logoutUser()
        }

        // Make the API call when the fragment is created
        makeApiCallAndUpdateTextView()

        return view
    }

    private fun makeApiCallAndUpdateTextView() {

        errorText.visibility = View.GONE

        apiRequestHandler.makeApiRequest(Request.Method.GET,
            Constants.API_PATH_WHOAMI,
            null,
            { response ->

                try {
                    profileEmail.text = response.getJSONObject("user").getString("email")
                    profileName.text = response.getJSONObject("user").getString("name")
                    profileRole.text = response.getJSONObject("user").getString("role")

                } catch (e: Exception) {
                    errorText.text = e.message
                    errorText.visibility = View.VISIBLE
                    Log.e(Constants.LOG_TAG_NAME, "error parsing json : $response")
                }
            },
            { errorMessage ->
                errorText.text = errorMessage
                errorText.visibility = View.VISIBLE
            }, {})
    }

}