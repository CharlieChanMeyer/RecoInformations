package com.thesis.stepcounter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.nio.charset.Charset

class History : AppCompatActivity(),TextToSpeech.OnInitListener {
    //create the buttons in the class variable
    lateinit var menuButton: Button

    //create the history display in the class variable
    lateinit var tableDisplay: TableLayout

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

            //Define the display
            tableDisplay = findViewById(R.id.historyDisplayTable)
            var tableToCreate = postVolley(globalVars.globalUserID)

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

    fun postVolley(id: Int) {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"history.php"

        val requestBody = "user_id=$id"
        val stringReq : StringRequest =
            @SuppressLint("UseCompatLoadingForDrawables")
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.split(",", "|").toMutableList()
                    if ("success" in strResp) {
                        var nbRestaurant = (strResp.size)/4 - 1
                        strResp.removeAt(0)
                        strResp.removeAt(0)
                        for (i in 0..nbRestaurant) {
                            val tableRow = TableRow(this)
                            val tViewName = TextView(this)
                            var tViewDate = TextView(this)
                            strResp.removeAt(0)
                            var tmp = strResp[0].split("/")
                            if (globalVars.globalLangAPP == "jp") {
                                tViewName.text = tmp[0]
                            } else {
                                tViewName.text = tmp[1]
                            }
                            tViewName.setTextColor(Color.parseColor("#000000"))
                            tViewName.gravity = Gravity.CENTER;
                            tViewName.background = resources.getDrawable(R.drawable.border)
                            tableRow.addView(tViewName);
                            strResp.removeAt(0)
                            strResp.removeAt(0)
                            tViewDate.text = strResp[0]
                            tViewDate.background = resources.getDrawable(R.drawable.border)
                            tViewDate.setTextColor(Color.parseColor("#000000"))
                            tViewDate.gravity = Gravity.CENTER;
                            tableRow.addView(tViewDate);
                            tableDisplay.addView(tableRow)
                            strResp.removeAt(0)
                        }
                    } else {
                        Toast.makeText(this, strResp[1], Toast.LENGTH_SHORT).show()
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