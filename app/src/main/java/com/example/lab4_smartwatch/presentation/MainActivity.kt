package com.example.lab4_smartwatch

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.startExercise
import com.example.lab4_smartwatch.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var heartRateText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var timerText: TextView

    private val exerciseCallback = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            TODO("Not yet implemented")
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val heartRateData = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
            val caloriesData = update.latestMetrics.getData(DataType.CALORIES_TOTAL)

            val heartRate = heartRateData.firstOrNull()?.value?.toFloat()
            val caloriesList = update.latestMetrics.getData(DataType.CALORIES_TOTAL)
            val duration = update.activeDurationCheckpoint?.activeDuration?.toMillis() ?: 0
            val minutes = duration / 1000 / 60
            val seconds = (duration / 1000) % 60

            runOnUiThread {
                timerText.text = String.format("%02d:%02d", minutes, seconds)
                heartRateText.text = heartRate?.toString() ?: "--"
                val calories = null
                caloriesText.text = calories?.toString() ?: "--"
            }
        }


        override fun onLapSummaryReceived(summary: ExerciseLapSummary) {}
        override fun onRegistered() {}
        override fun onRegistrationFailed(throwable: Throwable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        exerciseClient = HealthServices.getClient(this).exerciseClient

        timerText = findViewById(R.id.timer)
        heartRateText = findViewById(R.id.heartRate)
        caloriesText = findViewById(R.id.calories)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startExercise()
        }

        findViewById<Button>(R.id.pauseButton).setOnClickListener {
            pauseExercise()
        }
    }

    private fun startExercise() {
        val config = ExerciseConfig(
            exerciseType = ExerciseType.WALKING,
            dataTypes = setOf(
                DataType.HEART_RATE_BPM,
                DataType.CALORIES_TOTAL
            ),
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = false
        )

        lifecycleScope.launch {
            exerciseClient.setUpdateCallback(exerciseCallback)
            exerciseClient.startExercise(config)
        }
    }

    private fun pauseExercise() {
        lifecycleScope.launch {
            exerciseClient.pauseExercise()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            exerciseClient.clearUpdateCallback(exerciseCallback)
        }
    }
}