package com.thesis.stepcounter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.nio.charset.Charset
import java.util.*

class InfoReco : AppCompatActivity(), SensorEventListener,TextToSpeech.OnInitListener {
    // Added SensorEventListener the MainActivity class
    // Implement all the members in the class MainActivity
    // after adding SensorEventListener

    lateinit var outputTV: TextView
    lateinit var menuButton: Button
    lateinit var tv_stepsTaken: TextView
    lateinit var tv_infoView: TextView

    //get value of global var
    private var globalVars = GlobalVariables.Companion

    // we have assigned sensorManger to nullable
    private var sensorManager: SensorManager? = null

    private var tts: TextToSpeech? = null

    //Create a variable to stock if the user like the restaurant or not
    private var restaurantLiked = false

    //Verify if the communication with the server is finish
    private var communicationServer = false

    // Creating a variable which will give the running status
    // and initially given the boolean value as false
    private var running = false

    // Creating a variable which will counts total steps
    // and it has been given the value of 0 float
    private var totalSteps = 0f

    // Creating a variable which counts previous total
    // steps and it has also been given the value of 0 float
    private var previousTotalSteps = 0f

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    //on below we create a variable to stock the user response
    private var listUserResponse: MutableList<String> = mutableListOf()

    //Local list of restaurants names
    private var listRestaurantName: MutableList<String> = mutableListOf()

    private var asl = 0.1

    private var ifd = 25

    private var suggestionName = ""


    //Boolean to know what type of information was given to the user
    // -1 : Reset
    // 0 : Information found
    // 1 : Question to the user about his taste
    // 2 : Guide user to the destination
    private var ttsCodeInfo: Int = -1

    //      *********** CREATE AND INIT ***********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_reco)
        tts = TextToSpeech(this, this)

        reset()
        loadData()
        getStepsLength()

        // Adding a context of SENSOR_SERVICE aas Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            // set JP Japan as language for tts
            val result = tts!!.setLanguage(globalVars.globalLang)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
            tts!!.setSpeechRate(1.5f)

            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    userInteraction()
                }
                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {}
            })
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
        outputTV = findViewById(R.id.idTVOutput)
        tv_infoView = findViewById(R.id.tv_informationFound)
        tv_stepsTaken = findViewById(R.id.tv_stepsTaken)

        listRestaurantName = globalVars.globalRestaurantName.toMutableList()
        //Define the menu button
        menuButton = findViewById(R.id.irReturnMenuButton)
        if (globalVars.globalLangAPP == "jp") {
            menuButton.text = globalVars.globalText_jp["menu"]
        } else {
            menuButton.text = globalVars.globalText_eng["menu"]
        }
        menuButton.setOnClickListener {
            resetView()
            var intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }
        menuButton.setOnLongClickListener {
            tts!!.speak("メニューに戻るにはここをクリック", TextToSpeech.QUEUE_FLUSH, null,"")
            true
        }

    }

    //      *********** PAUSE AND DESTROY ***********

    override fun onResume() {
        super.onResume()
        running = true

        // Returns the number of steps taken by the user since the last reboot while activated
        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
        // So don't forget to add the following permission in AndroidManifest.xml present in manifest folder of the app.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (stepSensor == null) {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // Rate suitable for the user interface
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        tts = TextToSpeech(this, this)
        outputTV = findViewById(R.id.idTVOutput)
        tv_infoView = findViewById(R.id.tv_informationFound)
        tv_stepsTaken = findViewById(R.id.tv_stepsTaken)
    }

    override fun onStop() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onStop()
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    //      *********** SAVE AND LOAD ***********

    private fun saveData() {

        // Shared Preferences will allow us to save
        // and retrieve data in the form of key,value pair.
        // In this function we will save data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {

        // In this function we will retrieve data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        // Log.d is used for debugging purposes
        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    //      *********** RESET INTERACTION ***********

    private fun reset() {
        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        tv_stepsTaken.setOnClickListener {
            // This will give a toast message if the user want to reset the steps
            Toast.makeText(this, "ロングタップで距離のリセットが可能", Toast.LENGTH_SHORT).show()
            tts!!.speak("ロングタップで距離のリセットが可能", TextToSpeech.QUEUE_FLUSH, null,"")
        }

        tv_stepsTaken.setOnLongClickListener {
            resetView()

            true
        }
    }

    private fun resetView() {
        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        var tv_infoView = findViewById<TextView>(R.id.tv_informationFound)

        // When the user will click long tap on the screen,
        // the steps will be reset to 0
        tv_stepsTaken.text = 0.toString().plus(" m")
        previousTotalSteps = totalSteps
        //The information found will be reset.
        tv_infoView.text = ("レストランは見つかりませんでした。")
        outputTV.text = ("Output will appear here")
        ttsCodeInfo = -1
        listUserResponse.clear()
        listRestaurantName = globalVars.globalRestaurantName.toMutableList()
        tts!!.speak("", TextToSpeech.QUEUE_FLUSH, null,"")

        // This will save the data
        saveData()
    }

    //      *********** SENSOR INTERACTION ***********

    override fun onSensorChanged(event: SensorEvent?) {

        // Calling the TextView that we made in activity_main.xml
        // by the id given to that TextView
        var restaurantName = ""

        if (running) {
            totalSteps = event!!.values[0]

            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            // It will show the current travelled distance to the user
            var dCal = currentSteps * asl
            tv_stepsTaken.text = dCal.toString().plus(" m")

            if ((currentSteps % ifd == 0) and (listRestaurantName.size > 0 ) and (!communicationServer)) {
                restaurantName = listRestaurantName.random()
                var tmpRName = restaurantName.split("|")
                suggestionName = if (globalVars.globalLangAPP == "jp") tmpRName[0] else tmpRName[1]
                var index = listRestaurantName.indexOf(restaurantName)
                Log.e("Size",listRestaurantName.size.toString())
                listRestaurantName.removeAt(index)
                Log.e("Size",listRestaurantName.size.toString())
                if (globalVars.globalMethodNumber == 1) {
                    verifyLikedRestaurant()
                } else if (globalVars.globalMethodNumber == 2){
                    tv_infoView.text = (suggestionName.plus("が見つかりました。"))
                    ttsCodeInfo = -1
                    tts!!.speak((tv_infoView.text).toString(), TextToSpeech.QUEUE_FLUSH, null,"")
                }
            }
        }
    }

    //      *********** USER INTERACTION ***********

    private fun userInteraction() {
        var response = false

        if (ttsCodeInfo == 0) {
            while (!response) {
                response = waitForResponse()
            }

            outputTV.text = listUserResponse.last()

            ttsCodeInfo = -1
        }
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
                    this@InfoReco, " " + e.message,
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

                // put the response in the listUserResponse
                listUserResponse.add(Objects.requireNonNull(res)[0])
                // Change the response user variable to true
                outputTV.text = listUserResponse.last()
                var update = -1
                if ((listUserResponse.last().contains("大好き", ignoreCase = true))
                    or (listUserResponse.last().contains("好き", ignoreCase = true))
                    or (listUserResponse.last().contains("はい", ignoreCase = true))) {
                    update = 1
                }
                updatePreference(update)
            }
        }
    }

    //      *********** RESPONSE ANALYSE ***********
    /** Announce to the user that it will guide him to the location of the restaurant */
    private fun guideUser() {
        ttsCodeInfo = 2
        tts!!.speak("わかりました、ではこのレストランにご案内します。", TextToSpeech.QUEUE_FLUSH, null,"")
    }

    /** Announce to the user that it will search for another restaurant */
    private fun anotherSearch() {
        ttsCodeInfo = 3
        tts!!.speak("わかりました、では別のレストランを探します。", TextToSpeech.QUEUE_FLUSH, null,"")
    }

    //      *********** CAMERA ***********

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        return(context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
    }

    //      *********** DATABASE INTERACTION ***********

    private fun verifyLikedRestaurant() {
        communicationServer = true
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"verifyLikedRestaurant.php"
        var userID = globalVars.globalUserID
        val requestBody = "restaurant_name=$suggestionName&userID=$userID"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    var arrayResponse = response.replace("\"","").split(",", ": ")
                    if (arrayResponse[1] == "success") {
                        var dbData = arrayResponse[5].replace("}","")
                        dbData = dbData.replace("\n","")
                        if (dbData!= "dislike") {
                            tv_infoView.text = (suggestionName.plus("が見つかりました。"))
                            ttsCodeInfo = 0
                            tts!!.speak((tv_infoView.text).toString().plus("\n好きですか？"), TextToSpeech.QUEUE_FLUSH, null,"")
                        }
                    } else {
                        if ("The user didn't rated this restaurant" in arrayResponse[3]) {
                            tv_infoView.text = (suggestionName.plus("が見つかりました。"))
                            ttsCodeInfo = 0
                            tts!!.speak((tv_infoView.text).toString().plus("\n好きですか？"), TextToSpeech.QUEUE_FLUSH, null,"")
                        } else {
                            Toast.makeText(this, arrayResponse[3], Toast.LENGTH_SHORT).show()
                        }
                    }
                    communicationServer = false
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    Toast.makeText(this, strError, Toast.LENGTH_SHORT).show()
                    communicationServer = false
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    private fun updatePreference(update: Int) {
        communicationServer = true
        val queue = Volley.newRequestQueue(this)
        var url = globalVars.globalAPILink+"recommendation.php"
        var userID = globalVars.globalUserID
        val requestBody = "restaurant_name=$suggestionName&userID=$userID&update=$update"
        val stringReq : StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    var arrayResponse = response.replace("\"","").split(",", ": ")
                    if (arrayResponse[1] == "failed") {
                        Toast.makeText(this, arrayResponse[3], Toast.LENGTH_SHORT).show()
                    } else {
                        ttsCodeInfo = -1
                        tts!!.speak("おすすめのレストランが更新されました", TextToSpeech.QUEUE_FLUSH, null,"")
                    }
                    communicationServer = false
                },
                Response.ErrorListener { error ->
                    var strError = error.toString()
                    Toast.makeText(this, strError, Toast.LENGTH_SHORT).show()
                    communicationServer = false
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
                    if ("success" in strResp) {
                        strResp[3].trim()
                        asl = strResp[3].toDouble()
                        Log.d("Debbuging Display", asl.toString())
                        ifd = (5.0/asl).toInt()
                        Log.d("Debbuging Display", ifd.toString())
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
    //      *********** USELESS ***********

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }
}
