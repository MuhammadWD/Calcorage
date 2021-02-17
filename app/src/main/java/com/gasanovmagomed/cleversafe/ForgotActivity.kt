package com.gasanovmagomed.cleversafe

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var inputEmail: EditText;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        auth = FirebaseAuth.getInstance()
    }

    fun sendCode(view: View){
        inputEmail = findViewById(R.id.emailInput)
        val emailAddress = inputEmail.text.toString()

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) successSendingMsg()
                    else failSendingMsg()
                }
    }

    private fun successSendingMsg() { Toast.makeText(this, "Link successful sent to to your email address", Toast.LENGTH_LONG).show() }
    private fun failSendingMsg() { Toast.makeText(this, "Something was wrong! Please repeat later", Toast.LENGTH_LONG).show() }

    fun goBack(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}