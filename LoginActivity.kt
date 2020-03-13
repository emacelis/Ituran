package com.ituran.mapsapplication

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val button = findViewById<Button>(R.id.buttonLogRegistro) as Button

        button.setOnClickListener(View.OnClickListener { button ->
            registro()
        }

        )
    }

//


        private fun registro() {

            val etEmail = findViewById(R.id.editTextemailREGISTRO) as EditText
            val etPassword = findViewById(R.id.editTextPasswordREGISTRO) as EditText

            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            //       if(!email.isEmpty() && !password.isEmpty()){
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("USUARIOCREADO", "createUserWithEmail:success")
                       // val user = auth.currentUser

                        val intent = Intent(this, MapsActivity::class.java)
                        startActivity(intent)


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("USUARIOCREADO", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }
        }
}