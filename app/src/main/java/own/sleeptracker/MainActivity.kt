package own.sleeptracker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import own.sleeptracker.databinding.ActivityMainBinding
import java.util.*

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var sleepService: Intent
    lateinit var preferences: SharedPreferences
    lateinit var edit: SharedPreferences.Editor


    companion object {
        var seconds = 0
        var running = false

        fun timerStart() {
            running = true
        }

        fun timerStop() {
            running = false
        }

        fun timerReset() {
            running = false
            seconds = 0
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        edit = preferences.edit()
        sleepService = Intent(this, SleepService::class.java)


        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds")
            running = savedInstanceState.getBoolean("running")
        }
        runTimer()


        val timeView = findViewById<View>(R.id.time) as TextView
        seconds = preferences.getInt("time", 0)
        val gethours: Int = seconds / 3600
        val getminutes: Int = seconds % 3600 / 60
        val getsecs: Int = seconds % 60
        val gettime: String = java.lang.String.format(Locale.getDefault(), "%d:%02d:%02d", gethours, getminutes, getsecs)
        timeView.text = gettime

        val start = findViewById<View>(R.id.sleepservice) as Button
        start.setOnClickListener {
            if (running) {
                timerStop()
                running = false
                edit.putBoolean("running", false)
                edit.apply()
                stopService(sleepService)
                start.text = "Start"
            } else {
                timerStart()
                running = true
                edit.putBoolean("running", true)
                edit.apply()
                startService(sleepService)
                start.text = "Stop"
            }
        }


        val reset = findViewById<View>(R.id.reset_button) as Button
        reset.setOnClickListener {
            timerReset()
            seconds = 0
            edit.putInt("time", 0)
            edit.apply()
        }


    }

    private fun runTimer() {
        val timeView = findViewById<View>(R.id.time) as TextView
        val start = findViewById<View>(R.id.sleepservice) as Button
        edit = preferences.edit()
        val handler = Handler()
        seconds = preferences.getInt("time", 0)
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60
                var time: String = java.lang.String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                timeView.text = time
                Log.i("owntrackerwatch", "time: $time")
                edit.putInt("time", seconds)
                edit.apply()
                if (running) {
                    start.text = "Stop"
                    seconds++
                } else {
                    start.text = "Start"
                }
                handler.postDelayed(this, 1000)
            }
        })
    }


}