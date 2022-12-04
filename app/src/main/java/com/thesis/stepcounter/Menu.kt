package com.thesis.stepcounter

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Menu : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var infoRecoButton: Button
    lateinit var paramsButton: Button
    lateinit var historyButton: Button
    lateinit var disconnectButton: Button

    private var tts: TextToSpeech? = null

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        //Links the variable to their button
        infoRecoButton = findViewById(R.id.infoRecoButton)
        paramsButton = findViewById(R.id.parametersButton)
        historyButton = findViewById(R.id.historyButton)
        disconnectButton = findViewById(R.id.disconnectButton)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set JP Japan as language for tts
            val result = tts!!.setLanguage(Locale.JAPAN)

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

        //Link the infoReco button to his activity
        infoRecoButton.setOnClickListener {
            val intent = Intent(this, InfoReco::class.java)
            startActivity(intent)
        }

        //Set help speech when user do a long click on the info reco button
        infoRecoButton.setOnLongClickListener {
            tts!!.speak("情報のおすすめはこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        //Set help speech when user do a long click on the info reco button
        paramsButton.setOnLongClickListener {
            tts!!.speak("パラメータはこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        //Set help speech when user do a long click on the info reco button
        historyButton.setOnLongClickListener {
            tts!!.speak("沿革はこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }

        //Set help speech when user do a long click on the info reco button
        disconnectButton.setOnLongClickListener {
            tts!!.speak("接続解除はこちら", TextToSpeech.QUEUE_FLUSH, null,"")

            true
        }
    }

}