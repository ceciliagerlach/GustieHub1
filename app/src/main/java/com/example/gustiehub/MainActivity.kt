package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gustiehub.GlobalData.groupList
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Year
import kotlin.math.sign

class MainActivity : AppCompatActivity() {
    lateinit var loginButton: Button

    private val SERVER_CLIENT_ID = "183734578676-c4vnp76b0k2cbu26f2qb6ujjikr6hknb.apps.googleusercontent.com"
    private val CLIENT_EMAIL_DOMAIN = "@gustavus.edu"
    private lateinit var auth: FirebaseAuth
    private  val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this) // Ensure Firebase is initialized
//        GlobalData.initializeGlobalData()

        loginButton = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            signIn()
        }

        // *****************************************************************************

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val email = currentUser.email ?: "null"

            // Fetch user details from Firestore
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: "null"
                    val lastName = document.getString("lastName") ?: "null"
                    val gradYear = document.getLong("gradYear")?.toInt() ?: -1  // Handle gradYear as Int
                    val homeState = document.getString("homeState") ?: "Unknown"
                    val areasOfStudy = document.getString("areasOfStudy") ?: "Undeclared"

                    if (gradYear == -1) {
                        println("Invalid gradYear value in Firestore")
                    }

                    // Create User object (triggers init block)
                    val user = User(userId, email, firstName, lastName, gradYear, homeState, areasOfStudy)
                } else {
                    println("User document does not exist in Firestore")
                }
            }.addOnFailureListener { e ->
                println("Error fetching user document: ${e.message}")
            }
        } else {
            println("No authenticated user found.")
        }

        // set up sign-out button
        //findViewById<Button>(R.id.logout_button).setOnClickListener {
        //signOut()
        //}
    } // end onCreate()

    override fun onStart() {
        super.onStart()
        // check if user is signed in and update UI
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private var credentialManager = CredentialManager.create(this)
    /**
     * Initiates Google Sign-In using Credential Manager
     */
    private fun signIn() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Building Google Sign-In request...")

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(SERVER_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false) // Allows selecting any Google account
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d(TAG, "Requesting credential from Credential Manager...")
                val credential = credentialManager.getCredential(this@MainActivity, request).credential

                Log.d(TAG, "Credential received: ${credential.data}")
                handleSignIn(credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed: ${e.localizedMessage}", e)

            }
        }
    }

    /**
     * Handles the received credential and signs in with Firebase
     */
    private fun handleSignIn(credential: Credential) {
        Log.d(TAG, "Received credential: ${credential.data}")

        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Log.d(TAG, "Google ID Token: ${googleIdTokenCredential.idToken}")

            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not a valid Google ID Token!")
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
                    val email = user?.email
                    val userId = user?.uid

                    // check email belongs to Gustavus
                    if (email != null && email.endsWith(CLIENT_EMAIL_DOMAIN) && userId != null) {
                        Log.d(TAG, "Sign-in successful: $email")

                        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                        userRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                // user exists => go to dashboard
                                val dashboardIntent = DashboardActivity.newIntent(this, email)
                                startActivity(dashboardIntent)
                            } else {
                                // user doesn't exist => go to profile setup
                                showProfileCreationDialog(userId, email)
                            }
                        }.addOnFailureListener {
                            Log.w(TAG, "Failed to check user existence: ${it.message}")
                        }
                    } else {
                        Log.w(TAG, "Unauthorized email domain: $email")
                        auth.signOut()
                        updateUI(null)
                        Toast.makeText(this, "Use a Gustavus email address", Toast.LENGTH_SHORT).show()
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

    private fun onUserSignedUp(userId: String, email: String, firstName: String,
                               lastName: String, gradYear: Int, homeState: String,
                               areasOfStudy: String) {
        val newUser = User(userId, email, firstName, lastName,
            gradYear, homeState, areasOfStudy)
        newUser.createUserProfile(userId, email, firstName, lastName,
            gradYear, homeState, areasOfStudy) { success, error ->
            if (success) {
                println("User profile created successfully")
            } else {
                println("Error creating user profile: $error")
            }
        }
    }

    private fun showProfileCreationDialog(userId: String, email: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_user_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        // access UI elements
        val firstNameField = dialogView.findViewById<EditText>(R.id.firstName)
        val lastNameField = dialogView.findViewById<EditText>(R.id.lastName)
        val gradYearField = dialogView.findViewById<EditText>(R.id.classYear)
        val homeStateField = dialogView.findViewById<EditText>(R.id.homeState)
        val areasOfStudyField = dialogView.findViewById<EditText>(R.id.areasOfStudy)
//        val cancelButton = dialogView.findViewById<Button>(R.id.buttonCancel)
        val confirmButton = dialogView.findViewById<Button>(R.id.buttonConfirm)

        confirmButton.setOnClickListener {
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val gradYearInput = gradYearField.text.toString().trim()

            val gradYear: Int? = try {
                val yearInt = gradYearInput.toInt()
                if (yearInt in 1900..2050) {
                    yearInt // Return the valid year
                } else {
                    null // Invalid year
                }
            } catch (e: NumberFormatException) {
                null // Handle non-numeric input
            }

            if (gradYear == null) {
                Toast.makeText(this, "Please enter a valid graduation year (e.g., 2025).", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val homeState = homeStateField.text.toString().trim()
            val areasOfStudy = areasOfStudyField.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                val user = User(userId, email, firstName, lastName, gradYear, homeState, areasOfStudy)

                user.createUserProfile(userId, email, firstName, lastName,
                    gradYear, homeState, areasOfStudy) { success, error ->
                    if (success) {
                        Log.d(TAG, "User profile created successfully.")
                        dialog.dismiss()

                        // go to Dashboard
                        val dashboardIntent = DashboardActivity.newIntent(this, email)
                        startActivity(dashboardIntent)
                    } else {
                        Log.e(TAG, "Error creating user profile: $error")
                        Toast.makeText(this, "Failed to create profile: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


}