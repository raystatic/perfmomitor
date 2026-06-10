package com.example.androidapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.greetingButton.setOnClickListener {
            binding.greetingText.text = getString(R.string.greeting_clicked)
        }
    }
}
