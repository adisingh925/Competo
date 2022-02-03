package com.StartupBBSR.competo.Firebasemessaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Intent
import com.StartupBBSR.competo.Activity.MainActivity
import android.app.PendingIntent
import android.app.NotificationManager
import android.os.Build
import android.app.NotificationChannel
import android.content.Context
import com.StartupBBSR.competo.R
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.lifecycle.ViewModelProvider
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.ViewModel.fcmViewModel
import com.StartupBBSR.competo.ViewModel.offlineDatabaseViewModel
import com.StartupBBSR.competo.teamosDatabase.Database
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.annotations.NotNull
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.coroutines.coroutineContext

class firebasemessagingservice : FirebaseMessagingService() {

    val auth = Firebase.auth

    lateinit var offlineDatabaseViewModel: offlineDatabaseViewModel

    override fun onMessageReceived(@NotNull remoteMessage: RemoteMessage) {

        val senderId = remoteMessage.data["id"]
        val receiverId = auth.uid.toString()
        val category = remoteMessage.data["category"]
        val timeStamp = remoteMessage.data["timeStamp"].toString().toLong()
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val senderName = remoteMessage.data["senderName"]
        val receiveTimestamp = System.currentTimeMillis().toString()

        val data = chatOfflineModel(0,senderId!!,senderName!!,receiverId,body!!,receiveTimestamp,timeStamp.toString(),
            isSeen = false,"pending"
        )

        when(category)
        {
            "event" ->{
                geteventmessage(title,body,senderId,receiverId,timeStamp,senderName)
            }

            "request" ->{
                getrequestmessage(title,body,senderId,receiverId,timeStamp,senderName)
            }

            "chat" ->{
                getchatmessage(title,body,senderId,receiverId,timeStamp,senderName)
                Database.getDatabase(applicationContext).teamosDao().insertMessageData(data)
            }

            "team" ->{
                getteammessage(title,body,senderId,receiverId,timeStamp,senderName)
            }
        }

        super.onMessageReceived(remoteMessage)
    }

    fun geteventmessage(title: String?, body: String?, senderId : String?, receiverId : String, timeStamp : Long, senderName : String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationmanager1 = getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                "event_notification",
                "Events",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel1.description = "this is fcm event channel"
            notificationmanager1.createNotificationChannel(channel1)
        }
        val builder = NotificationCompat.Builder(this, "event_notification")
            .setSmallIcon(R.drawable.teamos_one_point_four_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val oneTimeID = SystemClock.uptimeMillis().toInt()
        notificationmanager1.notify(oneTimeID, builder.build())
    }

    fun getrequestmessage(title: String?, body: String?, senderId : String?, receiverId : String, timeStamp : Long, senderName : String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationmanager2 = getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel2 = NotificationChannel(
                "request_notification",
                "Requests",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel2.description = "this is fcm request channel"
            notificationmanager2.createNotificationChannel(channel2)
        }
        val builder = NotificationCompat.Builder(this, "request_notification")
            .setSmallIcon(R.drawable.teamos_one_point_four_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val oneTimeID = SystemClock.uptimeMillis().toInt()
        notificationmanager2.notify(oneTimeID, builder.build())
    }

    fun getchatmessage(title: String?, body: String?,senderId : String?, receiverId : String, timeStamp : Long, senderName : String?) {
        val sharedPreferences = getSharedPreferences(senderId, MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        val count = sharedPreferences.getInt("chatCount", 0)
        val randomValue = (Calendar.getInstance().timeInMillis % 1000000000).toInt()
        val chat_notification_id = sharedPreferences.getInt("chat_notification_id", 0)
        if (chat_notification_id == 0) {
            myEdit.putInt("chat_notification_id", randomValue)
        }
        if (count == 0) {
            myEdit.putString("chatMsg1", body)
            myEdit.putInt("chatCount", 1)
        } else if (count == 1) {
            myEdit.putString("chatMsg2", body)
            myEdit.putInt("chatCount", 2)
        } else {
            myEdit.putString("chatMsg1", sharedPreferences.getString("chatMsg2", null))
            myEdit.putString("chatMsg2", body)
        }
        myEdit.apply()
        val user = Person.Builder().setIcon(null).setName("Chat").build()
        val style = NotificationCompat.MessagingStyle(user).addMessage(
            sharedPreferences.getString("chatMsg1", null),
            Calendar.getInstance().timeInMillis,
            senderName
        )
            .addMessage(
                sharedPreferences.getString("chatMsg2", null),
                Calendar.getInstance().timeInMillis,
                senderName
            )
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationmanager3 = getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel3 = NotificationChannel(
                "chat_notification",
                "Chats",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel3.description = "this is fcm chat channel"
            notificationmanager3.createNotificationChannel(channel3)
        }
        val builder = NotificationCompat.Builder(this, "chat_notification")
            .setSmallIcon(R.drawable.teamos_one_point_four_logo)
            .setStyle(style)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager3.notify(
            sharedPreferences.getInt("chat_notification_id", 0),
            builder.build()
        )
    }

    fun getteammessage(title: String?, body: String?, senderId : String?, receiverId : String, timeStamp : Long, senderName : String?) {
        val sharedPreferences = getSharedPreferences(senderId, MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        val count = sharedPreferences.getInt("teamCount", 0)
        val randomValue = (Calendar.getInstance().timeInMillis % 1000000000).toInt()
        val chat_notification_id = sharedPreferences.getInt("team_notification_id", 0)
        if (chat_notification_id == 0) {
            myEdit.putInt("team_notification_id", randomValue)
        }
        if (count == 0) {
            myEdit.putString("teamMsg1", body)
            myEdit.putString("teamTitle1", title)
            myEdit.putInt("teamCount", 1)
        } else if (count == 1) {
            myEdit.putString("teamMsg2", body)
            myEdit.putString("teamTitle2", title)
            myEdit.putInt("teamCount", 2)
        } else if (count == 2) {
            myEdit.putString("teamMsg3", body)
            myEdit.putString("teamTitle3", title)
            myEdit.putInt("teamCount", 3)
        } else {
            myEdit.putString("teamMsg1", sharedPreferences.getString("teamMsg2", null))
            myEdit.putString("teamMsg2", sharedPreferences.getString("teamMsg3", null))
            myEdit.putString("teamMsg3", body)
            myEdit.putString("teamTitle1", sharedPreferences.getString("teamTitle2", null))
            myEdit.putString("teamTitle2", sharedPreferences.getString("teamTitle3", null))
            myEdit.putString("teamTitle3", title)
        }
        myEdit.apply()
        val user = Person.Builder().setIcon(null).setName("Teams").build()
        val style = NotificationCompat.MessagingStyle(user).addMessage(
            sharedPreferences.getString("teamMsg1", null),
            Calendar.getInstance().timeInMillis,
            sharedPreferences.getString("teamTitle1", null)
        )
            .addMessage(
                sharedPreferences.getString("teamMsg2", null),
                Calendar.getInstance().timeInMillis,
                sharedPreferences.getString("teamTitle2", null)
            )
            .addMessage(
                sharedPreferences.getString("teamMsg3", null),
                Calendar.getInstance().timeInMillis,
                sharedPreferences.getString("teamTitle3", null)
            )
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationmanager4 = getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel4 = NotificationChannel(
                "team_notification",
                "Teams",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel4.description = "this is fcm team channel"
            notificationmanager4.createNotificationChannel(channel4)
        }
        val builder = NotificationCompat.Builder(this, "team_notification")
            .setSmallIcon(R.drawable.teamos_one_point_four_logo)
            .setStyle(style)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager4.notify(
            sharedPreferences.getInt("team_notification_id", 0),
            builder.build()
        )
    }
}