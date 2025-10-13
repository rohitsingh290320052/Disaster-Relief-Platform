package com.example.disasterrelief

import android.app.Application
import com.google.firebase.FirebaseApp

class DisasterReliefApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
