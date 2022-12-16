package com.thesis.stepcounter

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Parameters : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var menuButton: Button
    lateinit var switchButton: Switch
    lateinit var likedRecoButton: Button

    //get value of global var
    private var globalVars = GlobalVariables.Companion

    private var tts: TextToSpeech? = null

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parameters)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
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
        menuButton = findViewById(R.id.paramReturnMenuButton)
        if (globalVars.globalLangAPP == "jp") {
            menuButton.text = globalVars.globalText_jp["menu"]
        } else {
            menuButton.text = globalVars.globalText_eng["menu"]
        }
        menuButton.setOnClickListener {
            var intent = Intent(this, Menu::class.java)
            startActivity(intent)
            finish()
        }
        menuButton.setOnLongClickListener {
            tts!!.speak("メニューに戻るにはここをクリック", TextToSpeech.QUEUE_FLUSH, null,"")
            true
        }

        //Define the switch button
        switchButton = findViewById(R.id.langSwitch)
        switchButton.isChecked = globalVars.globalLangAPP != "jp"
        switchButton.setOnClickListener {
            if (globalVars.globalLangAPP == "jp") {
                globalVars.globalLangAPP = "eng"
            } else {
                globalVars.globalLangAPP = "jp"
            }
            val intent = Intent(this, Parameters::class.java)
            startActivity(intent)
            finish()
        }

        //Define the reco Liked button
        likedRecoButton = findViewById(R.id.recoLikedButton)
        if (globalVars.globalLiked) {
            if (globalVars.globalLangAPP == "jp") {
                likedRecoButton.text = globalVars.globalText_jp["on"]
            } else {
                likedRecoButton.text = globalVars.globalText_eng["on"]
            }
        } else {
            if (globalVars.globalLangAPP == "jp") {
                likedRecoButton.text = globalVars.globalText_jp["off"]
            } else {
                likedRecoButton.text = globalVars.globalText_eng["off"]
            }
        }
        likedRecoButton.setOnClickListener {
            if (globalVars.globalLiked) {
                globalVars.globalLiked = false
                if (globalVars.globalLangAPP == "jp") {
                    likedRecoButton.text = globalVars.globalText_jp["off"]
                } else {
                    likedRecoButton.text = globalVars.globalText_eng["off"]
                }
            } else {
                globalVars.globalLiked = true
                if (globalVars.globalLangAPP == "jp") {
                    likedRecoButton.text = globalVars.globalText_jp["on"]
                } else {
                    likedRecoButton.text = globalVars.globalText_eng["on"]
                }
            }
        }

    }
}