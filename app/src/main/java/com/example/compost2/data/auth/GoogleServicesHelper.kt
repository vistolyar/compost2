package com.example.compost2.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.Base64

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Draft
import com.google.api.services.gmail.model.Message as GmailMessage

// Алиасы для Tasks (РЕШЕНИЕ КОНФЛИКТА ИМЕН)
import com.google.api.services.tasks.Tasks as TasksService
import com.google.api.services.tasks.model.Task as GoogleTask

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GoogleServicesHelper(private val context: Context, private val account: GoogleSignInAccount) {

    private val credential = GoogleAccountCredential.usingOAuth2(
        context,
        Collections.singleton("https://www.googleapis.com/auth/calendar.events")
    ).apply {
        selectedAccount = account.account
    }

    private val jsonFactory = GsonFactory.getDefaultInstance()

    // Используем AndroidHttp (он есть в версии 1.35.2)
    private val httpTransport = AndroidHttp.newCompatibleTransport()

    private val appName = "ComPost"

    // --- CALENDAR ---
    suspend fun createCalendarEvent(title: String, description: String): String? = withContext(Dispatchers.IO) {
        try {
            val service = Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(appName)
                .build()

            val event = Event()
            // Используем явные сеттеры
            event.setSummary(title)
            event.setDescription(description)

            val startDateTime = EventDateTime().setDateTime(com.google.api.client.util.DateTime(System.currentTimeMillis()))
            val endDateTime = EventDateTime().setDateTime(com.google.api.client.util.DateTime(System.currentTimeMillis() + 3600000))

            event.setStart(startDateTime)
            event.setEnd(endDateTime)

            val createdEvent = service.events().insert("primary", event).execute()
            createdEvent.htmlLink
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- TASKS ---
    suspend fun createTask(taskTitle: String, taskNotes: String): String? = withContext(Dispatchers.IO) {
        try {
            // Используем алиас TasksService
            val service = TasksService.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(appName)
                .build()

            // Используем алиас GoogleTask
            val task = GoogleTask()
            // Используем явные сеттеры, чтобы избежать ошибки "Variable expected"
            task.setTitle(taskTitle)
            task.setNotes(taskNotes)

            // @default - это ID списка задач "Мои задачи"
            val createdTask = service.tasks().insert("@default", task).execute()
            createdTask.selfLink
            "Task Created"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- GMAIL ---
    suspend fun createDraft(subject: String, bodyText: String): String? = withContext(Dispatchers.IO) {
        try {
            val service = Gmail.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(appName)
                .build()

            val props = Properties()
            val session = Session.getDefaultInstance(props, null)
            val email = MimeMessage(session)

            email.setFrom(InternetAddress(account.email))
            email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(account.email))
            email.subject = subject
            email.setText(bodyText)

            val buffer = ByteArrayOutputStream()
            email.writeTo(buffer)
            val encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray())

            val message = GmailMessage()
            message.setRaw(encodedEmail) // Сеттер!

            val draft = Draft()
            draft.setMessage(message) // Сеттер!

            val createdDraft = service.users().drafts().create("me", draft).execute()
            createdDraft.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}