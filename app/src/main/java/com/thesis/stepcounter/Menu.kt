package com.thesis.stepcounter

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
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

    private var waitingResponse = false

    private var idFood = -1

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
            tts!!.setSpeechRate(globalVars.globalSpeechSpeed)

            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    if (waitingResponse) {
                        waitForResponse()
                    }
                }
                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {}
            })
        } else {
            Log.e("TTS", "Initilization Failed!")
        }

        getRestaurants()

        if (globalVars.globalErrorCode == 1) {
            tts!!.speak("このページにアクセスするには、ログインする必要があります。", TextToSpeech.QUEUE_FLUSH, null,"")
            globalVars.globalErrorCode = 0
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

        if (globalVars.globalMethodNumber == 2) {
            infoRecoButton.setOnClickListener {
                val intent = Intent(this, InfoReco::class.java)
                startActivity(intent)
                finish()
            }
        } else if (globalVars.globalMethodNumber == 1){
            verifyLikedFood()
        } else {
            verifyLikedRestaurant()
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
                    Log.d("Debugging",strResp)
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

    private fun getRestaurants() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"getRestaurants.php"

        val requestBody = ""
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString().split(",")
                    for (restaurant in strResp) {
                        if (restaurant !in globalVars.globalRestaurantName) {
                            globalVars.globalRestaurantName.add(restaurant)
                        }
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

    private fun verifyLikedFood() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"verifyLikedFood.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var food = response.toString()
                    if (food != "") {
                        var split_tmp = food.split(":")
                        idFood = split_tmp[0].toInt()
                        var food_lg = split_tmp[1].split("|")
                        waitingResponse = true
                        if (globalVars.globalLangAPP == "jp") {
                            tts!!.speak(
                                food_lg[0]+"の好みを1～9で教えてください。",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        } else {
                            tts!!.speak(
                                food_lg[1] + "is unrated. Please rate it from 1 to 9.",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    } else {
                        updatePredicted();
                    }
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    Log.e("Error",strError)
                    Toast.makeText(this, strError, Toast.LENGTH_SHORT).show()
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    private fun verifyLikedRestaurant() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"verifyRatedRestaurant.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    var food = response.toString()
                    if (food != "") {
                        var split_tmp = food.split(":")
                        idFood = split_tmp[0].toInt()
                        var food_lg = split_tmp[1].split("|")
                        waitingResponse = true
                        if (globalVars.globalLangAPP == "jp") {
                            tts!!.speak(
                                food_lg[0]+"の好みを1～9で教えてください。",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        } else {
                            tts!!.speak(
                                food_lg[1] + "is unrated. Please rate it from 1 to 9.",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    } else {
                        infoRecoButton.setOnClickListener {
                            val intent = Intent(this, InfoReco::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    Log.e("Error",strError)
                    Toast.makeText(this, strError, Toast.LENGTH_SHORT).show()
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    private fun updatePredicted() {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"recommendationClass.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener {
                    //Link the infoReco button to his activity
                    infoRecoButton.setOnClickListener {
                        val intent = Intent(this, InfoReco::class.java)
                        startActivity(intent)
                        finish()
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

    private fun waitForResponse():Boolean {
        var res: Boolean = false

        // on below line we are calling speech recognizer intent.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        // on below line we are passing language model
        // and model free form in our intent
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // on below line we are passing our
        // language as a default language.
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "ja"
        )

        // on below line we are specifying a prompt
        // message as speak to text on below line.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声テキスト化")

        // on below line we are specifying a try catch block.
        // in this block we are calling a start activity
        // for result method and passing our result code.
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            res = true
        } catch (e: Exception) {
            // on below line we are displaying error message in toast
            Toast
                .makeText(
                    this@Menu, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
        return res
    }

    // on below line we are calling on activity result method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                var resp = Objects.requireNonNull(res)[0]

                // List of accepted response
                //var acceptedResp = arrayListOf<String>("大嫌い","嫌い","普通","好き","大好き","hate","dislike","normal","like","love")
                var acceptedResp = arrayListOf<String>("1","2","3","4","5","6","7","8","9","一","二","三","四","五","六","七","八","九")

                if (resp in acceptedResp) {
                    waitingResponse = false
                    var rating = 0
                    if ((resp.contains("1", ignoreCase = true)) or
                        (resp.contains("一", ignoreCase = true))) {
                        rating = 1
                    } else if ((resp.contains("2", ignoreCase = true)) or
                        (resp.contains("二", ignoreCase = true))) {
                        rating = 2
                    } else if ((resp.contains("3", ignoreCase = true)) or
                        (resp.contains("三", ignoreCase = true))) {
                        rating = 3
                    } else if ((resp.contains("4", ignoreCase = true)) or
                        (resp.contains("四", ignoreCase = true))) {
                        rating = 4
                    } else if ((resp.contains("5", ignoreCase = true)) or
                        (resp.contains("五", ignoreCase = true))) {
                        rating = 5
                    } else if ((resp.contains("6", ignoreCase = true)) or
                        (resp.contains("六", ignoreCase = true))) {
                        rating = 6
                    } else if ((resp.contains("7", ignoreCase = true)) or
                        (resp.contains("七", ignoreCase = true))) {
                        rating = 7
                    } else if ((resp.contains("8", ignoreCase = true)) or
                        (resp.contains("八", ignoreCase = true))) {
                        rating = 8
                    } else if ((resp.contains("9", ignoreCase = true)) or
                        (resp.contains("九", ignoreCase = true))) {
                        rating = 9
                    }
                    if (globalVars.globalMethodNumber == 1) {
                        pushRating(rating)
                    } else if (globalVars.globalMethodNumber == 3)  {
                        pushRatingRestaurant(rating)
                    }
                } else {
                    if (globalVars.globalLangAPP == "jp") {
                        tts!!.speak(
                            "1～9で教えてください。",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    } else {
                        tts!!.speak(
                            "Please tell us on a scale of 1 to 9.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }
                }

            }
        }
    }

    private fun pushRating(rating: Int) {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"pushRatingFood.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID&foodID=$idFood&rating=$rating"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    if (response.toString() == "Success") {
                        verifyLikedFood()
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

    private fun pushRatingRestaurant(rating: Int) {
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"pushRatingRestaurant.php"
        var userID = globalVars.globalUserID
        val requestBody = "userID=$userID&foodID=$idFood&rating=$rating"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    if (response.toString() == "Success") {
                        verifyLikedRestaurant()
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