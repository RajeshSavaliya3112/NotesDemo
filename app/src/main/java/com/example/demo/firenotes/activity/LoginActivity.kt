package com.example.demo.firenotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.demo.firenotes.databinding.ActivityLoginBinding
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.utils.Extension.snackbar
import com.example.demo.firenotes.utils.Loading
import com.example.demo.firenotes.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        title = "Login"

        initViewModel()

        initObserver()

        initListeners()
    }

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    private fun initObserver() {
        authViewModel.getLoggedInUser().observe(this,{
            if(it != null){
                navigateToMain(it.uid)
            }
        })
    }

    private fun navigateToMain(uid: String) {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.putExtra("uid",uid)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun initListeners() {

        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.etEmail.text.toString()
            val password = loginBinding.etPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                /** Execute Login */
                authViewModel.loginUser(email,password)?.observe(this,{ resource ->
                    when(resource){
                        is Resource.Success -> {
                            Loading.hideLoading()
                            navigateToMain(resource.data!!.uid)
                        }
                        is Resource.Error -> {
                            Loading.hideLoading()
                            snackbar("${resource.message}")
                        }
                        is Resource.Loading -> Loading.displayLoadingWithText(this,cancelable = false)
                    }
                })
            }else{
                snackbar("All fields are required")
            }
        }

        loginBinding.tvSignUp.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
    }
}