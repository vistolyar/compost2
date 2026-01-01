package com.example.compost2.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

class GoogleAuthClient(private val context: Context) {

    // СКОУПЫ: Разрешения, которые мы просим у пользователя
    private val scopes = arrayOf(
        Scope("https://www.googleapis.com/auth/calendar.events"),
        Scope("https://www.googleapis.com/auth/gmail.compose"),
        Scope("https://www.googleapis.com/auth/tasks")
    )

    fun getSignInIntent(): Intent {
        // УБРАЛИ .requestIdToken(...)
        // Теперь мы просто просим email и права доступа.
        // Проверка приложения идет автоматически через SHA-1 в консоли Google.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(scopes[0], *scopes.sliceArray(1 until scopes.size))
            .build()

        val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun signOut(onComplete: () -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener { onComplete() }
    }
}