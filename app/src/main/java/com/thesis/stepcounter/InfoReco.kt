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
import java.util.*

class InfoReco : AppCompatActivity(), SensorEventListener,TextToSpeech.OnInitListener {
    // Added SensorEventListener the MainActivity class
    // Implement all the members in the class MainActivity
    // after adding SensorEventListener

    lateinit var outputTV: TextView
    lateinit var menuButton: Button

    // we have assigned sensorManger to nullable
    private var sensorManager: SensorManager? = null

    private var tts: TextToSpeech? = null

    // Creating a variable which will give the running status
    // and initially given the boolean value as false
    private var running = false

    // Creating a variable which will counts total steps
    // and it has been given the value of 0 float
    private var totalSteps = 0f

    // Creating a variable which counts previous total
    // steps and it has also been given the value of 0 float
    private var previousTotalSteps = 0f

    //List of information already found
    private var listInfoFound: MutableList<String> = mutableListOf()

    // Create a constant for the code speech
    private val REQUEST_CODE_SPEECH_INPUT = 1

    //on below we create a variable to stock the user response
    private var listUserResponse: MutableList<String> = mutableListOf()

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

        loadData()

        // Adding a context of SENSOR_SERVICE aas Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Define the menu button
        menuButton = findViewById(R.id.irReturnMenuButton)
        menuButton.setOnClickListener {
            resetView()
            var intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set JP Japan as language for tts
            val result = tts!!.setLanguage(Locale.JAPAN)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }

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
        listInfoFound.clear()
        ttsCodeInfo = -1
        listUserResponse.clear()
        tts!!.speak("", TextToSpeech.QUEUE_FLUSH, null,"")

        // This will save the data
        saveData()
    }

    //      *********** SENSOR INTERACTION ***********

    override fun onSensorChanged(event: SensorEvent?) {

        // Calling the TextView that we made in activity_main.xml
        // by the id given to that TextView
        var tv_stepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)
        var tv_infoView = findViewById<TextView>(R.id.tv_informationFound)
        var info = mutableMapOf<Int,String>(20 to "Saizeria", 40 to "くら寿司")

        if (running) {
            totalSteps = event!!.values[0]

            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            // It will show the current travelled distance to the user
            var dCal = currentSteps * 0.20
            tv_stepsTaken.text = dCal.toString().plus(" m")

            if ((currentSteps > 20) and (currentSteps < 40) and (info[20] !in listInfoFound)) {
                tv_infoView.text = ("レストランが見つかりました :\n".plus(info[20]))
                listInfoFound.add(info[20].toString())
                ttsCodeInfo = 0
                tts!!.speak((tv_infoView.text).toString().plus("\n行きたいですか？"), TextToSpeech.QUEUE_FLUSH, null,"")
            } else if ((currentSteps > 40) and (info[40] !in listInfoFound)) {
                listInfoFound.add(info[40].toString())
                ttsCodeInfo = 0
                tv_infoView.text = ("レストランが見つかりました :\n".plus(info[40]))
                tts!!.speak((tv_infoView.text).toString().plus("\n行きたいですか？"), TextToSpeech.QUEUE_FLUSH, null,"")
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

                // put the response dans the listUserResponse
                listUserResponse.add(Objects.requireNonNull(res)[0])
                // Change the response user variable to true
                outputTV.text = listUserResponse.last()

                if ("はい" in listUserResponse.last()) {
                    guideUser()
                } else {
                    anotherSearch()
                }
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
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true
        } else {
            // no camera on this device
            return false
        }
    }

    //      *********** USELESS ***********

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }
}
