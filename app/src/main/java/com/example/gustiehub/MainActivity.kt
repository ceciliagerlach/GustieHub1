package com.example.gustiehub

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var emailInput: EditText
    lateinit var loginButton: Button
    Gusties = Group("Gusties", null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email_input)
        loginButton = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            // verify email here and update current user info

            // move to dashboard view if valid email/password
            val dashboardIntent = DashboardActivity.newIntent(this, email)
            startActivity(dashboardIntent)
        }
    }
}