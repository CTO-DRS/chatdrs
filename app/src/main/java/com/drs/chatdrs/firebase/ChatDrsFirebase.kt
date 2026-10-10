package com.drs.chatdrs.firebase

import android.content.Context
import com.drs.chatdrs.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object ChatDrsFirebase {

    @Volatile
    private var firestoreInstance: FirebaseFirestore? = null

    /**
     * Initializes and retrieves the custom FirebaseFirestore instance
     * bound strictly to the provisioned database ID from firebase_applet_config.xml.
     */
    fun getFirestore(context: Context): FirebaseFirestore {
        return firestoreInstance ?: synchronized(this) {
            firestoreInstance ?: run {
                val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
                val instance = FirebaseFirestore.getInstance(databaseId)
                firestoreInstance = instance
                instance
            }
        }
    }

    /**
     * Firebase Authentication instance for Google Sign-In user management.
     */
    val auth: FirebaseAuth
        get() = Firebase.auth
}
