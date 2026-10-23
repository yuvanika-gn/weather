package com.weatherapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class SearchActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        editText = findViewById(R.id.editText)
        val nextButton: Button = findViewById(R.id.nextButton)

        nextButton.setOnClickListener(View.OnClickListener {
            val cityName = editText.text.toString().trim()

            if(cityName.isNotEmpty()){
                val intent = Intent(this@SearchActivity, MainActivity::class.java)
                intent.putExtra("CITY_NAME", cityName)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        })
    }
}