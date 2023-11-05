package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set up a listener for item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Load the home fragment
                    loadFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_posts -> {
                    // Load the search fragment
                    loadFragment(PostsFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_profile -> {
                    // Load the profile fragment
                    loadFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        // Load the initial fragment
        loadFragment(HomeFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.commit()
    }

}