package com.thesis.stepcounter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import java.nio.charset.Charset


class Register : AppCompatActivity() {

    lateinit var textInputEditTextName:EditText
    lateinit var textInputEditTextEmail:EditText
    lateinit var textInputEditTextPassword:EditText
    lateinit var buttonSubmit: Button
    lateinit var buttonLogin: Button

    lateinit var name: String
    lateinit var email: String
    lateinit var password: String

    lateinit var textViewError: TextView
    lateinit var progressRegi: ProgressBar

    //get value of global var
    private var globalVars = GlobalVariables.Companion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        textInputEditTextName = findViewById(R.id.regiNameInput)
        textInputEditTextEmail = findViewById(R.id.RegiEmailInput)
        textInputEditTextPassword = findViewById(R.id.RegiPasswordInput)
        buttonSubmit = findViewById(R.id.regiRegisterButton)
        buttonLogin = findViewById(R.id.regiLoginButton)
        textViewError = findViewById(R.id.regiErrorLogin)

        buttonSubmit.setOnClickListener {
            name = textInputEditTextName.text.toString()
            email = textInputEditTextEmail.text.toString()
            password = textInputEditTextPassword.text.toString()

            postVolley(name, email, password)

        }

        buttonLogin.setOnClickListener {
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun postVolley(name: String, email: String, password: String) {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"register.php"

        val requestBody = "name=$name&email=$email&password=$password"
        val stringReq : StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()
                    if ("success" in strResp) {
                        var intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        textViewError.text = strResp
                    }
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    textViewError.text = strError
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }
}