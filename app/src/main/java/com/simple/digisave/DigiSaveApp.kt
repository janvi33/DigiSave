package com.simple.digisave

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DigiSaveApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 🔥 Quick Firestore test
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf(
            "message" to "Hello Firestore 🚀",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("test")
            .add(testData)
            .addOnSuccessListener { docRef ->
                Log.d("FirestoreTest", "Document written with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreTest", "Error adding document", e)
            }
    }
}
