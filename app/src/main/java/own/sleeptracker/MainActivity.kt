package own.sleeptracker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.wear.tiles.*
import own.sleeptracker.databinding.ActivityMainBinding
import java.util.*
import kotlin.math.sqrt

class MainActivity : Activity(), SensorEventListener {

    private var sensorMan: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var mAccel = 0.0
    private var mAccelCurrent = 0.0
    private var mAccelLast = 0.0

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
        val clickableId = intent.getStringExtra(TileService.EXTRA_CLICKABLE_ID)

        sensorMan = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorMan!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mAccel = 0.00
        mAccelCurrent = SensorManager.GRAVITY_EARTH.toDouble()
        mAccelLast = SensorManager.GRAVITY_EARTH.toDouble()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (clickableId == "start") {
            Log.i("owntrackerwatch", "tile start button clicked!!!")
            val start: Button = findViewById<View>(R.id.sleepservice) as Button
            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val edit = preferences.edit()
            val sleepService = Intent(this, SleepService::class.java)
            sensorMan?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            timerStart()
            running = true
            edit.putBoolean("running", true)
            edit.apply()
            startService(sleepService)
            start.text = "Stop"
        }


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
                sensorMan?.unregisterListener(this)
                timerStop()
                running = false
                edit.putBoolean("running", false)
                edit.apply()
                stopService(sleepService)
                start.text = "Start"
            } else {
                sensorMan?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
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
/*
        if (clickableId == "start") {
            Log.i("owntrackerwatch", "tile start button clicked!!!")
            val start: Button = findViewById<View>(R.id.sleepservice) as Button
            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val edit = preferences.edit()
            val sleepService = Intent(this, SleepService::class.java)
            sensorMan?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            timerStart()
            running = true
            edit.putBoolean("running", true)
            edit.apply()
            startService(sleepService)
            start.text = "Stop"
        }
  */



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


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        val x: Float = event.values.get(0)
        val y: Float = event.values.get(1)
        val z: Float = event.values.get(2)
        mAccelLast = mAccelCurrent
        mAccelCurrent = sqrt((x * x + y * y + z * z).toDouble())
        val delta = mAccelCurrent - mAccelLast
        mAccel = mAccel * 0.9f + delta // perform low-cut filter
        sleepService = Intent(this, SleepService::class.java)
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var running = preferences.getBoolean("running", false)
        val sensitive = 10.3f
        //Log.i("owntrackerwatch", "sensitive (weight): $sensitive")
        //Log.i("owntrackerwatch", "mAccel: $mAccel")
        if (mAccel > sensitive) {
            if (running) {
                Log.i("owntrackerwatch", "SleepService: watch movement: " + mAccel.toString())
                val editor = preferences.edit()
                editor.putBoolean("running", false)
                editor.apply()
                Log.i("owntrackerwatch", "SleepService: sleeptrack after watch moved: " + running)
                Log.i("owntrackerwatch", "sleep tracking should stop")
                timerStop()
                stopService(sleepService)
                running = false
            }
        } /*else {
            val editor = preferences.edit()
            editor.putBoolean("running", true)
            editor.apply()
            MainActivity.timerStart()
            MainActivity.running = true
        }
        */
    }




}