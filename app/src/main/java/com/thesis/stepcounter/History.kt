package com.thesis.stepcounter

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class History : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var menuButton: Button

    //get value of global var
    private var globalVars = GlobalVariables.Companion

    private var tts: TextToSpeech? = null

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (globalVars.globalUserID != -1) {
            if (status == TextToSpeech.SUCCESS) {
                // set JP Japan as language for tts
                val result = tts!!.setLanguage(globalVars.globalLang)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                }

                tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {}
                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
            } else {
                Log.e("TTS", "Initilization Failed!")
            }

            //Define the menu button
            menuButton = findViewById(R.id.histoReturnMenuButton)
            menuButton.setOnClickListener {
                var intent = Intent(this, Menu::class.java)
                startActivity(intent)
                finish()
            }
            menuButton.setOnLongClickListener {
                tts!!.speak("メニューに戻るにはここをクリック", TextToSpeech.QUEUE_FLUSH, null,"")
                true
            }
        } else {
            //Code test surement a retirer
            tts!!.speak("このページにアクセスするには、ログインする必要があります。", TextToSpeech.QUEUE_FLUSH, null,"")
            //set le code erreur "not logged"
            globalVars.globalErrorCode = 1
            //retourne au menu (a changer avec la page de connection)
            var intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }
    }
}