package com.example.myapplication

import RoadReportAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.RoadReport

class PostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var roadReportAdapter: RoadReportAdapter
    private lateinit var apiRequestHandler: ApiRequestHandler
    private lateinit var view: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_posts, container, false)
        apiRequestHandler = ApiRequestHandler(view.context)

        recyclerView = view.findViewById(R.id.postRecyclerView)

        // Set up RecyclerView and adapter
        roadReportAdapter = RoadReportAdapter() // Create your custom adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(), DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.adapter = roadReportAdapter

        // Fetch posts from your API and update the adapter with the data
        // Replace this with your actual API call and data population logic

        val roadReports = mutableListOf<RoadReport>()
        apiRequestHandler.makeApiRequest(Request.Method.GET,
            Constants.API_PATH_ALLPOSTS,
            null,
            { response ->

                try {
                    val jsonArray = response.getJSONArray("list")
                    for (i in 0 until jsonArray.length()) {
                        val element = jsonArray.getJSONObject(i)
                        val x = RoadReport(
                            id = element.getInt("id"),
                            categoryName = element.getString("categoryName"),
                            email = element.getString("email"),
                            description = element.getString("description"),
                            imgID = element.getString("imgID"),
                            name = element.getString("name"),
                            unixTime = element.getLong("unixTime"),
                            longitude = element.getDouble("longitude"),
                            latitude = element.getDouble("latitude")
                        )
                        roadReports.add(x)
                    }

                } catch (e: Exception) {
                    Toast.makeText(view.context, e.message, Toast.LENGTH_SHORT).show()
                    Log.e(Constants.LOG_TAG_NAME, "error parsing json : $response")
                }
            },
            { errorMessage ->
                Toast.makeText(view.context, errorMessage, Toast.LENGTH_SHORT).show()
            },
            {
                roadReportAdapter.submitList(roadReports)

            })

        return view
    }

}