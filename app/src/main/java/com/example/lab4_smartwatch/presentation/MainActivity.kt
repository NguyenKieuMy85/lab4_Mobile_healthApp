package com.example.lab4_smartwatch

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MainActivity : ComponentActivity() {

    private lateinit var heartRateText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var timerText: TextView
    private lateinit var stepsText: TextView
    private lateinit var motivationalMessageText: TextView

    private val API_KEY = "AIzaSyClOpki1w3nqv10IWm2fwAqK0eST" // Thay thế với API Key thực tế của bạn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo các TextView
        timerText = findViewById(R.id.timer)
        heartRateText = findViewById(R.id.heartRate)
        caloriesText = findViewById(R.id.calories)
        stepsText = findViewById(R.id.steps)
        motivationalMessageText = findViewById(R.id.motivationalMessage)

        // Gán sự kiện cho các nút
        val startButton = findViewById<Button>(R.id.startButton)
        val pauseButton = findViewById<Button>(R.id.pauseButton)

        startButton.setOnClickListener {
            startExercise() // Bắt đầu bài tập
        }

        pauseButton.setOnClickListener {
            pauseExercise() // Tạm dừng bài tập
        }
    }

    private fun startExercise() {
        // Thực thi khi bắt đầu bài tập
        timerText.text = "00:00"
        heartRateText.text = "❤️ 75 bpm"
        caloriesText.text = "🔥 120 kcal"
        stepsText.text = "Steps: 500"

        // Gửi thông tin tới Gemini API
        sendDataToGeminiAPI(500, 120.0)
    }

    private fun pauseExercise() {
        // Thực thi khi tạm dừng bài tập
        timerText.text = "Paused"
        heartRateText.text = "❤️ -- bpm"
        caloriesText.text = "🔥 -- kcal"
        stepsText.text = "Steps: --"
    }

    private fun sendDataToGeminiAPI(steps: Int, calories: Double) {
        val httpClient = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GeminiApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.generateContent(
                    "models/gemini-2.0-flash:generateContent?key=$API_KEY", // Xây dựng URL động với API Key
                    RequestBody(
                        contents = listOf(
                            Content(
                                parts = listOf(
                                    Part(text = "Provide a workout suggestion based on $steps steps and $calories calories burned")
                                )
                            )
                        )
                    )
                )

                if (response.isSuccessful) {
                    val message = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    runOnUiThread {
                        motivationalMessageText.text = message ?: "Keep going!"
                    }
                } else {
                    Log.e("GeminiAPI", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("GeminiAPI", "Exception: ${e.message}", e)
            }
        }
    }

    interface GeminiApiService {
        @POST
        suspend fun generateContent(
            @retrofit2.http.Url url: String, // Sử dụng @Url để xây dựng URL động
            @Body requestBody: RequestBody
        ): retrofit2.Response<GeminiResponse>
    }

    data class RequestBody(val contents: List<Content>)
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
    data class GeminiResponse(val candidates: List<Candidate>)
    data class Candidate(val content: Content)
}
