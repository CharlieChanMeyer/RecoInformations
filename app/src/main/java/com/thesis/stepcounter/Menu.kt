package com.thesis.stepcounter

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Menu : AppCompatActivity() {
    //create the buttons in the class variable
    lateinit var infoRecoButton: Button
    lateinit var paramsButton: Button
    lateinit var historyButton: Button
    lateinit var disconnectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        //Links the variable to their button
        infoRecoButton = findViewById(R.id.infoRecoButton)
        paramsButton = findViewById(R.id.parametersButton)
        historyButton = findViewById(R.id.historyButton)
        disconnectButton = findViewById(R.id.disconnectButton)

        infoRecoButton.setOnClickListener {
            val intent = Intent(this, InfoReco::class.java)
            startActivity(intent)
        }
    }

}