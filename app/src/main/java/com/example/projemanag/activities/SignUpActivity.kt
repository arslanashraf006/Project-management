package com.example.projemanag.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignUpBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private var binding : ActivitySignUpBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
        binding!!.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding!!.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding!!.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun registerUser(){
        val name : String = binding!!.etName.text.toString().trim { it <= ' ' }
        val email : String = binding!!.etEmail.text.toString().trim { it <= ' ' }
        val password : String = binding!!.etPassword.text.toString().trim { it <= ' ' }
        
        if (validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                   // hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registerEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registerEmail)
                        FirestoreClass().registerUser(this, user)
//                        Toast.makeText(
//                            this,
//                            "$name you have successfully register the email address $registerEmail",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        FirebaseAuth.getInstance().signOut()
//                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Registration Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
//            Toast.makeText(
//                this@SignUpActivity,
//                "Now we can register a new user.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
    }

    private fun validateForm(name: String,
                             email: String,
                             password: String) : Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }else -> {
                true
            }
        }
    }
}