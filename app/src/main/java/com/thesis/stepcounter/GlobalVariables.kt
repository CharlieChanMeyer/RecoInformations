package com.thesis.stepcounter

import android.app.Application
import java.util.*

class GlobalVariables : Application() {
    companion object {
        /** User ID
         *  -1   : not logged
         *  else : User ID */
        var globalUserID = -1

        var globalUserApiKEY = ""

        var globalUserEmail = ""

        /** Language of the text-to-speech functionality */
        var globalLang: Locale = Locale.JAPAN

        /** Language of the application */
        var globalLangAPP = "jp"

        /** Global error code
         * 0 : no error
         * 1 : not logged
         * */
        var globalErrorCode = 0

        /** restaurants' list*/
        var globalRestaurantName = mutableListOf<String>("Saizeria", "くら寿司", "カレーハウスCoCo壱番屋", "Big Boy", "なか卯", "スシロー")

        /** API link */
        var globalAPILink = "http://ec2-15-168-13-179.ap-northeast-3.compute.amazonaws.com/thesis-app/"
    }

}