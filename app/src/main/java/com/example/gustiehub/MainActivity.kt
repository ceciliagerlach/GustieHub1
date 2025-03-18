package com.example.gustiehub

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import androidx.credentials.*
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlin.math.sign

class MainActivity : AppCompatActivity() {
    lateinit var emailInput: EditText
    lateinit var loginButton: Button

    private val SERVER_CLIENT_ID = "183734578676-c4vnp76b0k2cbu26f2qb6ujjikr6hknb.apps.googleusercontent.com"
    private val CLIENT_EMAIL_DOMAIN = "@gustavus.edu"
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private  val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email_input)
        loginButton = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            // verify email here and update current user info
            if (email.isNotEmpty() && email.endsWith(CLIENT_EMAIL_DOMAIN)) {
                signIn()
            } else {
                Toast.makeText(this, "Enter a Gustavus email address", Toast.LENGTH_SHORT).show()
            }
        }

        // *****************************************************************************

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        // set up sign-in button
//        findViewById<Button>(R.id.login_button).setOnClickListener {
//            signIn()
//        }

        // set up sign-out button
        //findViewById<Button>(R.id.sign_out_button).setOnClickListener {
        //signOut()
        //}
    } // end onCreate()

    override fun onStart() {
        super.onStart()
        // check if user is signed in and update UI
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    /**
     * Initiates Google Sign-In using Credential Manager
     */
    private fun signIn() {
        lifecycleScope.launch {
            try {
                // create Google Sign-In request
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Allow all Google accounts
                    .setServerClientId(SERVER_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // request credentials
                val credential = credentialManager.getCredential(this@MainActivity, request).credential
                handleSignIn(credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Handles the received credential and signs in with Firebase
     */
    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // authenticate with Firebase using the Google ID token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID Token!")
        }
    }

    /**
     * Signs in to Firebase using the Google ID token
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // check email belongs to Gustavus
                    val email = user?.email
                    if (email != null && email.endsWith(CLIENT_EMAIL_DOMAIN)) {
                        Log.d(TAG, "Sign-in successful: $email")
                        updateUI(user)

                        // move to dashboard view if valid email/password
                        val dashboardIntent = DashboardActivity.newIntent(this, email)
                        startActivity(dashboardIntent)
                    } else {
                        Log.w(TAG, "Unauthorized email domain: $email")
                        auth.signOut() // Sign out the user
                        updateUI(null) // Reset UI
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    /**
     * Signs out the user from Firebase and clears credentials
     */
    private fun signOut() {
        auth.signOut()

        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Updates UI based on user authentication state
     */
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "User signed in: ${user.email}")
        } else {
            Log.d(TAG, "No user signed in")
        }
    }

}