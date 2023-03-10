package com.thesis.stepcounter

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.nio.charset.Charset
import java.util.*

class Parameters : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var menuButton: Button
    lateinit var switchButton: Switch
    lateinit var radioButton1: RadioButton
    lateinit var radioButton2: RadioButton
    lateinit var radioButton3: RadioButton
    lateinit var stepButton: Button
    lateinit var stepInput: EditText

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
            tts!!.setSpeechRate(1.5f)

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
                globalVars.globalLang = Locale.ENGLISH
            } else {
                globalVars.globalLangAPP = "jp"
                globalVars.globalLang = Locale.JAPAN
            }
            val intent = Intent(this, Parameters::class.java)
            startActivity(intent)
            finish()
        }

        //Define the radio checked
        radioButton1 = findViewById(R.id.rbmethod1)
        radioButton2 = findViewById(R.id.rbmethod2)
        radioButton3 = findViewById(R.id.rbmethod3)

        if (globalVars.globalMethodNumber == 1) {
            radioButton1.isChecked = true
        } else if (globalVars.globalMethodNumber == 2) {
            radioButton2.isChecked = true
        } else {
            radioButton3.isChecked = true
        }

        stepInput = findViewById(R.id.inputStepLength)

        getStepsLength()

        stepButton = findViewById(R.id.buttonStepLength)

        stepButton.setOnClickListener{
            setASL()
        }
    }

    private fun setASL() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"setASL.php"
        var userID = globalVars.globalUserID
        var asl = stepInput.text
        val requestBody = "userID=$userID&asl=$asl"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.replace("\"","").replace("}","").split(",",": ")
                    Log.e("Test1",strResp.toString())
                    if ("success" !in strResp) {
                        Toast.makeText(this, strResp[3], Toast.LENGTH_SHORT).show()
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
    private fun getStepsLength() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"getASL.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.replace("\"","").replace("}","").split(",",": ")
                    Log.e("Test1",strResp.toString())
                    if ("success" in strResp) {
                        strResp[3].trim()
                        stepInput.setText(strResp[3])
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

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rbmethod1 ->
                    if (checked) {
                        globalVars.globalMethodNumber = 1
                    }
                R.id.rbmethod2 ->
                    if (checked) {
                        globalVars.globalMethodNumber = 2
                    }

                R.id.rbmethod3 ->
                    if (checked) {
                        globalVars.globalMethodNumber = 3
                    }
            }
        }
    }


}