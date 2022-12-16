package com.thesis.stepcounter

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.nio.charset.Charset
import java.util.*

class Menu : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var infoRecoButton: Button
    lateinit var paramsButton: Button
    lateinit var historyButton: Button
    lateinit var disconnectButton: Button

    //get value of global var
    private var globalVars = GlobalVariables.Companion

    private var tts: TextToSpeech? = null

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        //Links the variable to their button
        infoRecoButton = findViewById(R.id.infoRecoButton)
        if (globalVars.globalLangAPP == "jp") {
            infoRecoButton.text = globalVars.globalText_jp["info"]
        } else {
            infoRecoButton.text = globalVars.globalText_eng["info"]
        }
        paramsButton = findViewById(R.id.parametersButton)
        if (globalVars.globalLangAPP == "jp") {
            paramsButton.text = globalVars.globalText_jp["para"]
        } else {
            paramsButton.text = globalVars.globalText_eng["para"]
        }
        historyButton = findViewById(R.id.historyButton)
        if (globalVars.globalLangAPP == "jp") {
            historyButton.text = globalVars.globalText_jp["history"]
        } else {
            historyButton.text = globalVars.globalText_eng["history"]
        }
        disconnectButton = findViewById(R.id.disconnectButton)
        if (globalVars.globalLangAPP == "jp") {
            disconnectButton.text = globalVars.globalText_jp["disco"]
        } else {
            disconnectButton.text = globalVars.globalText_eng["disco"]
        }

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (globalVars.globalUserID == -1) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        if (status == TextToSpeech.SUCCESS) {
            // set JP Japan as language for tts
            val result = tts!!.setLanguage(globalVars.globalLang)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }

            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {}
                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {}
            })
        } else {
            Log.e("TTS", "Initilization Failed!")
        }

        if (globalVars.globalErrorCode == 1) {
            tts!!.speak("このページにアクセスするには、ログインする必要があります。", TextToSpeech.QUEUE_FLUSH, null,"")
            globalVars.globalErrorCode = 0
        }

        //Link the infoReco button to his activity
        infoRecoButton.setOnClickListener {
            val intent = Intent(this, InfoReco::class.java)
            startActivity(intent)
            finish()
        }

        //Set help speech when user do a long click on the info reco button
        infoRecoButton.setOnLongClickListener {
            tts!!.speak("情報のおすすめはこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        //Link the parameters button to his activity
        paramsButton.setOnClickListener {
            val intent = Intent(this, Parameters::class.java)
            startActivity(intent)
            finish()
        }

        //Set help speech when user do a long click on the parameters button
        paramsButton.setOnLongClickListener {
            tts!!.speak("パラメータはこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        //Link the infoReco button to his activity
        historyButton.setOnClickListener {
            val intent = Intent(this, History::class.java)
            startActivity(intent)
            finish()
        }

        //Set help speech when user do a long click on the history button
        historyButton.setOnLongClickListener {
            tts!!.speak("沿革はこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        disconnectButton.setOnClickListener {
            postVolley(globalVars.globalUserEmail,globalVars.globalUserApiKEY)
        }

        //Set help speech when user do a long click on the disconnect button
        disconnectButton.setOnLongClickListener {
            tts!!.speak("接続解除はこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }
    }

    private fun postVolley(email: String, apiKey: String) {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"logout.php"

        val requestBody = "email=$email&apiKey=$apiKey"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()
                    if (strResp == "success") {
                        globalVars.globalUserID = -1
                        globalVars.globalUserEmail = ""
                        globalVars.globalUserApiKEY = ""
                        var intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, strResp, Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    Toast.makeText(this, strError, Toast.LENGTH_SHORT).show()
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

}