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
        var globalRestaurantName = mutableListOf<String>("なか卯|Nakau","くら寿司|Kurasushi","マクドナルド|McDonald")
        /** API link */
        var globalAPILink = "http://ec2-15-168-13-179.ap-northeast-3.compute.amazonaws.com/thesis-app/"

        /** Application Method number */
        var globalMethodNumber = 1

        /** Eng Lang */
        var globalText_eng = mapOf<String,String>(
            "menu" to "MENU",
            "info" to "INFORMATION RECOMMENDATION",
            "para" to "PARAMETERS",
            "history" to "HISTORY",
            "disco" to "DISCONNECT",
            "login" to "LOGIN",
            "register" to "REGISTER",
            "on" to "TURN OFF",
            "off" to "TURN ON"
        )

        /** JP Lang */
        var globalText_jp = mapOf<String,String>(
            "menu" to "メニュー",
            "info" to "情報提供のすすめ",
            "para" to "パラメータ",
            "history" to "沿革",
            "disco" to "ディスコネクト",
            "login" to "ログイン",
            "register" to "レジスター",
            "on" to "止める",
            "off" to "点ける"
        )
    }

}