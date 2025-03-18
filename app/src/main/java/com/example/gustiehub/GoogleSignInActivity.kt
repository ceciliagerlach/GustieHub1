package com.example.gustiehub

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.*
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class GoogleSignInActivity : AppCompatActivity() {
    private val SERVER_CLIENT_ID = "183734578676-c4vnp76b0k2cbu26f2qb6ujjikr6hknb.apps.googleusercontent.com"
    private val CLIENT_EMAIL_DOMAIN = "@gustavus.edu"
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // set up sign-in button
        findViewById<Button>(R.id.login_button).setOnClickListener {
            signIn()
        }

//        // set up sign-out button
//        findViewById<Button>(R.id.logout_button).setOnClickListener {
//            signOut()
//        }
    }

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
                    .setFilterByAuthorizedAccounts(true) // Filter by Gustavus Google accounts
                    .setServerClientId(SERVER_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // request credentials
                val credential = credentialManager.getCredential(this@GoogleSignInActivity, request).credential
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
        Log.d(TAG, "User signed in: ${user?.email ?: "No user"}")
    }

    companion object {
        private const val TAG = "GoogleSignInActivity"
    }
}
