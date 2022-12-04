package com.thesis.stepcounter

import android.content.pm.PackageManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.Manifest;
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class CoreDatabase: AppCompatActivity() {
    private val ip = ""// this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc
    private val port = "1433";// the port sql server runs on
    private val Classes =
        "net.sourceforge.jtds.jdbc.Driver" // the driver that is required for this connection use
    // "org.postgresql.Driver" for connecting to postgresql

    private val database = "CustomerCareSystem" // the data base name
    private val username = "natydb" // the user name
    private val password = "1234" // the password
    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"

    private var connection: Connection? = null

    fun start(view: View?) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.INTERNET),
            PackageManager.PERMISSION_GRANTED
        )
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(Classes)
            connection = DriverManager.getConnection(url, username, password)
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Class fail", Toast.LENGTH_SHORT).show()
        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, "Connected no", Toast.LENGTH_SHORT).show()
        }
    }

}