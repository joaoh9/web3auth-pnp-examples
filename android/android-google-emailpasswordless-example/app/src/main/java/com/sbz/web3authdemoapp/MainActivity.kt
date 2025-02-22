package com.sbz.web3authdemoapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    private lateinit var web3Auth: Web3Auth

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web3Auth = Web3Auth(
           Web3AuthOptions(
               context = this,
               clientId = getString(R.string.web3auth_project_id), // pass over your Web3Auth Client ID from Developer Dashboard
               network = Network.SAPPHIRE_MAINNET, // pass over the network you want to use (MAINNET or TESTNET or CYAN, AQUA, SAPPHIRE_MAINNET or SAPPHIRE_TESTNET)
               buildEnv = BuildEnv.PRODUCTION,
               redirectUrl = Uri.parse("com.sbz.web3authdemoapp://auth"), // your app's redirect URL
               loginConfig = hashMapOf(
                   "google" to LoginConfigItem(
                       verifier = "aggregate-sapphire",
                       verifierSubIdentifier= "w3a-google",
                       typeOfLogin = TypeOfLogin.GOOGLE,
                       name = "Aggregate Login",
                       clientId = getString(R.string.web3auth_google_client_id)
                    ),
                   "jwt" to LoginConfigItem(
                       verifier = "aggregate-sapphire",
                       verifierSubIdentifier= "w3a-a0-email-passwordless",
                       typeOfLogin = TypeOfLogin.JWT,
                       name = "Aggregate Login",
                       clientId = getString(R.string.web3auth_auth0_client_id)
                   )
               )
           )
        )

        // Handle user signing in when app is not alive
        web3Auth.setResultUrl(intent?.data)

        // Call initialize() in onCreate() to check for any existing session.
        val sessionResponse: CompletableFuture<Void> = web3Auth.initialize()
        sessionResponse.whenComplete { _, error ->
            if (error == null) {
                reRender()
                println("PrivKey: " + web3Auth.getPrivkey())
                println("ed25519PrivKey: " + web3Auth.getEd25519PrivKey())
                println("Web3Auth UserInfo" + web3Auth.getUserInfo())
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                // Ideally, you should initiate the login function here.
            }
        }

        // Setup UI and event handlers
        val signInGoogleButton = findViewById<Button>(R.id.signInGoogle)
        signInGoogleButton.setOnClickListener { signInGoogle() }

        val signInEPButton = findViewById<Button>(R.id.signInEP)
        signInEPButton.setOnClickListener { signInEP() }

        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener { signOut() }
        signOutButton.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Handle user signing in when app is active
        web3Auth.setResultUrl(intent?.data)
    }

    private fun signInEP() {
        val selectedLoginProvider = Provider.JWT
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(domain = "https://web3auth.au.auth0.com", verifierIdField = "email", isVerifierIdCaseSensitive = false)))

        loginCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                reRender()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
            }
        }
    }

    private fun signInGoogle() {
        val selectedLoginProvider = Provider.GOOGLE
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(LoginParams(selectedLoginProvider))

        loginCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                reRender()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
            }
        }
    }

    private fun signOut() {
        val logoutCompletableFuture =  web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                reRender()
            } else {
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong" )
            }
        }
        recreate()
    }

    private fun reRender() {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val signInEPButton = findViewById<Button>(R.id.signInEP)
        val signInGoogleButton = findViewById<Button>(R.id.signInGoogle)
        val signOutButton = findViewById<Button>(R.id.signOutButton)
        var key: String? = null
        var userInfo: UserInfo? = null
        try {
            key = web3Auth.getPrivkey()
            userInfo = web3Auth.getUserInfo()
        } catch (ex: Exception) {
            print(ex)
        }
        println(userInfo)
        if (key is String && key.isNotEmpty()) {
            contentTextView.text = gson.toJson(userInfo) + "\n Private Key: " + key
            contentTextView.visibility = View.VISIBLE
            signInEPButton.visibility = View.GONE
            signInGoogleButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
        } else {
            contentTextView.text = getString(R.string.not_logged_in)
            contentTextView.visibility = View.GONE
            signInEPButton.visibility = View.VISIBLE
            signInGoogleButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }
    }
}