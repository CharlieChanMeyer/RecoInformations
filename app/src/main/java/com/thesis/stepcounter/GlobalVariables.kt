package com.thesis.stepcounter

import android.app.Application
import java.util.*

class GlobalVariables : Application() {
    /** User ID
     *  -1   : not logged
     *  else : User ID */
    var globalUserID = -1

    var globalUserApiKEY = ""

    var globalUserEmail = ""

    /** Language of the text-to-speech functionality */
    var globalLang: Locale = Locale.JAPAN

    /** Global error code
     * 0 : no error
     * 1 : not logged
     * */
    var globalErrorCode = 0

    /** restaurants' list*/
    var globalRestaurantName = mutableListOf<String>("Saizeria", "くら寿司", "カレーハウスCoCo壱番屋", "Big Boy", "なか卯", "スシロー")

}