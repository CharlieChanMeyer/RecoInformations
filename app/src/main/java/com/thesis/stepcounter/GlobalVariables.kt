package com.thesis.stepcounter

import android.app.Application
import java.util.*

class GlobalVariables : Application() {
    /** User ID
     *  -1   : not logged
     *  else : User ID */
    var globalUserID = -1

    /** Language of the text-to-speech functionality */
    var globalLang: Locale = Locale.JAPAN

    /** Global error code
     * 0 : no error
     * 1 : not logged
     * */
    var globalErrorCode = 0

}