package com.ethan.FamiCare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class HealthBloodPressureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_blood_pressure)
        //血壓有分收縮壓跟壓縮壓，而且單位很複雜，先跳過
    }
}