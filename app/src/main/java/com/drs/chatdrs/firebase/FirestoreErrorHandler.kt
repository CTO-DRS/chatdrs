package com.drs.chatdrs.firebase

import android.util.Log

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE
}

object FirestoreErrorHandler {
    fun handleFirestoreError(exception: Exception, operation: OperationType, path: String) {
        val jsonDiagnostic = """
            {
                "event": "firestore_error",
                "operation": "${operation.name}",
                "path": "$path",
                "error_message": "${exception.localizedMessage ?: "Unknown error"}",
                "exception_type": "${exception.javaClass.simpleName}"
            }
        """.trimIndent()
        Log.e("FirestoreError", jsonDiagnostic, exception)
    }
}
