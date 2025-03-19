package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_EMAIL = "com.example.gustiehub.email"

        fun newIntent(packageContext: Context, email: String): Intent? {
            return Intent(packageContext, DashboardActivity::class.java).apply {
                putExtra("USER_EMAIL", email)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val email = intent.getStringExtra(EXTRA_EMAIL)
        // use the email to fetch information
    }

}