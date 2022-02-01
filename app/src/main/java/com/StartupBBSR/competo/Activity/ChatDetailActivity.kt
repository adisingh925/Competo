package com.StartupBBSR.competo.Activity

import androidx.appcompat.app.AppCompatActivity
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.Models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.StartupBBSR.competo.Adapters.NewChatAdapter
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.Toast
import java.lang.Void
import android.util.Log
import java.lang.Exception
import com.bumptech.glide.Glide
import android.net.Uri
import android.view.View
import com.StartupBBSR.competo.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.AbsListView
import androidx.lifecycle.ViewModelProvider
import com.StartupBBSR.competo.ViewModel.fcmViewModel
import com.StartupBBSR.competo.databinding.ActivityChatDetailBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import java.lang.Runnable
import java.io.IOException
import java.lang.Thread
import java.util.*

class ChatDetailActivity : AppCompatActivity() {
    private var binding: ActivityChatDetailBinding? = null
    private var constant: Constant? = null
    private var messageModel: MessageModel? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var firestoreDB: FirebaseFirestore? = null
    private var status: String? = ""
    private var newChatAdapter: NewChatAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var mMessage: MutableList<MessageModel?>? = null
    private val limit = 13
    private var lastVisible: DocumentSnapshot? = null
    private var isScrolling = false
    private var isLastItemReached = false
    private var isSeenlistenerRegistration1: ListenerRegistration? = null
    private var isSeenlistenerRegistration2: ListenerRegistration? = null
    private var eventListener1: EventListener<QuerySnapshot>? = null
    private var eventListener2: EventListener<QuerySnapshot>? = null
    private var senderID: String? = null
    private var receiverID: String? = null
    private var receiverName: String? = null
    private var receiverPhoto: String? = null
    private var collectionReference: CollectionReference? = null
    private var receiverRef: DocumentReference? = null
    private var userRef: DocumentReference? = null
    private var userMessageNumberRef: DocumentReference? = null
    private var seenRef1: CollectionReference? = null
    private var seenRef2: CollectionReference? = null


    //viewmodel
    lateinit var fcmViewModel: fcmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = applicationContext.getSharedPreferences("MyPref", MODE_PRIVATE)
        val editor = pref.edit()
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        //initialize fcmviewmodel
        fcmViewModel = ViewModelProvider(this).get(com.StartupBBSR.competo.ViewModel.fcmViewModel::class.java)

        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()
        constant = Constant()
        senderID = firebaseAuth!!.uid
        receiverID = intent.getStringExtra("receiverID")
        receiverName = intent.getStringExtra("receiverName")
        receiverPhoto = intent.getStringExtra("receiverPhoto")
        updateReceiverInfo(receiverName, receiverPhoto)
        userMessageNumberRef =
            firestoreDB!!.collection(constant!!.messageNumber).document(senderID!!)
        userRef = firestoreDB!!.collection(constant!!.users).document(senderID!!)
        receiverRef = firestoreDB!!.collection(constant!!.users).document(receiverID!!)
        receiverUpdates
        status("Online")
        collectionReference = firestoreDB!!.collection(constant!!.chats)
            .document(senderID!!)
            .collection(constant!!.messages)
            .document(receiverID!!)
            .collection(constant!!.messages)
        binding!!.btnBack.setOnClickListener {
            if (binding!!.sendMessageProgressBar.visibility != View.VISIBLE) {
                finish()
            } else {
                Toast.makeText(this@ChatDetailActivity, "Please wait", Toast.LENGTH_SHORT).show()
            }
        }
        binding!!.btnSendChat.setOnClickListener {
            if (binding!!.etMessage.text.toString().trim { it <= ' ' } != "") {
                val message = binding!!.etMessage.text.toString().trim { it <= ' ' }
                val timestamp = Date().time
                messageModel = MessageModel(senderID, receiverID, message, timestamp)
                messageModel!!.seen = false
                binding!!.etMessage.setText("")

//                    Show progress bar
                binding!!.btnSendChat.visibility = View.INVISIBLE
                binding!!.sendMessageProgressBar.visibility = View.VISIBLE
                firestoreDB!!.collection(constant!!.chats)
                    .document(senderID!!)
                    .collection(constant!!.messages)
                    .document(receiverID!!)
                    .collection(constant!!.messages)
                    .add(messageModel!!)
                    .addOnSuccessListener {
                        firestoreDB!!.collection(constant!!.chats)
                            .document(receiverID!!)
                            .collection(constant!!.messages)
                            .document(senderID!!)
                            .collection(constant!!.messages)
                            .add(messageModel!!)
                            .addOnSuccessListener { //                                                    Message send
                                binding!!.btnSendChat.visibility = View.VISIBLE
                                binding!!.sendMessageProgressBar.visibility = View.GONE

//                                                    Updating timestamp of users for sorting
                                firestoreDB!!.collection(constant!!.users)
                                    .document(senderID!!)
                                    .update("time", timestamp)
                                    .addOnCompleteListener {
                                        firestoreDB!!.collection(constant!!.users)
                                            .document(receiverID!!)
                                            .update("time", timestamp)
                                    }
                                fcmViewModel.notification(receiverID!!,senderID!!,message,"chat","NR",timestamp)
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this@ChatDetailActivity,
                                    "Could not deliver message",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding!!.btnSendChat.visibility = View.VISIBLE
                                binding!!.sendMessageProgressBar.visibility = View.GONE
                            }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@ChatDetailActivity,
                            "Could not send message",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding!!.btnSendChat.visibility = View.VISIBLE
                        binding!!.sendMessageProgressBar.visibility = View.GONE
                    }
            }
        }

//        For read receipts
        seenRef1 = firestoreDB!!.collection(constant!!.chats)
            .document(senderID!!)
            .collection(constant!!.messages)
            .document(receiverID!!)
            .collection(constant!!.messages)
        seenRef2 = firestoreDB!!.collection(constant!!.chats)
            .document(receiverID!!)
            .collection(constant!!.messages)
            .document(senderID!!)
            .collection(constant!!.messages)
        seenMessage(senderID, receiverID)
        if (isSeenlistenerRegistration1 == null && isSeenlistenerRegistration2 == null) {
            isSeenlistenerRegistration1 = seenRef1!!.addSnapshotListener(eventListener1!!)
            isSeenlistenerRegistration2 = seenRef2!!.addSnapshotListener(eventListener2!!)
        }
        initNewRecycler()
    }

    private fun updateReceiverInfo(receiverName: String?, receiverPhoto: String?) {
        binding!!.chatUserName.text = receiverName
        if (receiverPhoto != null) Glide.with(applicationContext)
            .load(Uri.parse(receiverPhoto))
            .into(binding!!.chatUserImage) else Glide.with(applicationContext)
            .load(R.drawable.ic_baseline_person_24)
            .into(binding!!.chatUserImage)
    }

    private val receiverUpdates: Unit
        private get() {
            receiverRef!!.addSnapshotListener(EventListener { value, error ->
                if (error != null) {
                    return@EventListener
                }
                val receiverName = value!!.getString("Name")
                val receiverPhoto = value.getString("Photo")
                updateReceiverInfo(receiverName, receiverPhoto)
            })
        }

    private fun initNewRecycler() {
        recyclerView = binding!!.chatRecyclerView
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = false
        linearLayoutManager.reverseLayout = true
        recyclerView!!.layoutManager = linearLayoutManager
    }

    private fun readMessage(senderID: String?, receiverID: String?) {
        mMessage = ArrayList()
        collectionReference!!.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit.toLong())
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                (mMessage as ArrayList<MessageModel?>).clear()
                for (snapshot in queryDocumentSnapshots) {
                    val model = snapshot.toObject(MessageModel::class.java)
                    if (model.receiverID == senderID && model.senderID == receiverID
                        || model.receiverID == receiverID && model.senderID == senderID
                    ) {
                        (mMessage as ArrayList<MessageModel?>).add(model)
                    }
                    newChatAdapter = NewChatAdapter(this@ChatDetailActivity, mMessage)
                    recyclerView!!.adapter = newChatAdapter

//                    Pagination
                    lastVisible =
                        queryDocumentSnapshots.documents[queryDocumentSnapshots.size() - 1]
                    Log.d(
                        "paginate",
                        "onSuccess-Last Visible: " + (queryDocumentSnapshots.size() - 1)
                    )
                    val onScrollListener: RecyclerView.OnScrollListener =
                        object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                super.onScrollStateChanged(recyclerView, newState)
                                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    isScrolling = true
                                }
                            }

                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                val linearLayoutManager =
                                    recyclerView.layoutManager as LinearLayoutManager?
                                val firstVisibleItemPosition =
                                    linearLayoutManager!!.findFirstVisibleItemPosition()
                                val visibleItemCount = linearLayoutManager.childCount
                                val totalItemCount = linearLayoutManager.itemCount
                                if (isScrolling && firstVisibleItemPosition + visibleItemCount == totalItemCount && !isLastItemReached) {
                                    isScrolling = false
                                    val nextQuery = collectionReference!!.orderBy(
                                        "timestamp",
                                        Query.Direction.DESCENDING
                                    ).startAfter(lastVisible).limit(limit.toLong())
                                    nextQuery.get().addOnCompleteListener { t ->
                                        if (t.isSuccessful) {
                                            for (d in t.result) {
                                                val messageModel = d.toObject(
                                                    MessageModel::class.java
                                                )
                                                (mMessage as ArrayList<MessageModel?>).add(messageModel)
                                            }
                                            newChatAdapter!!.notifyDataSetChanged()
                                            if (t.result.size() - 1 >= 0) {
                                                lastVisible =
                                                    t.result.documents[t.result.size() - 1]
                                            }
                                            if (t.result.size() < limit) {
                                                isLastItemReached = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    recyclerView!!.addOnScrollListener(onScrollListener)
                }
            }
    }

    private fun seenMessage(senderID: String?, receiverID: String?) {
        eventListener1 = EventListener { value, error ->
            if (error != null) {
                Log.w("error", "Listen failed.", error)
                return@EventListener
            }
            for (snapshot in value!!) {
                val messageModel = snapshot.toObject(
                    MessageModel::class.java
                )
                if (messageModel.receiverID == senderID && messageModel.senderID == receiverID) {
                    snapshot.reference.update("seen", true)
                }
            }
        }
        eventListener2 = EventListener { value, error ->
            if (error != null) {
                Log.w("error", "Listen failed.", error)
                return@EventListener
            }
            for (snapshot in value!!) {
                val messageModel = snapshot.toObject(
                    MessageModel::class.java
                )
                if (messageModel.receiverID == senderID && messageModel.senderID == receiverID) {
                    snapshot.reference.update("seen", true)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        collectionReference!!.addSnapshotListener(EventListener { value, error ->
            if (error != null) {
                Log.e("error", "onEvent: ", error)
                return@EventListener
            }
            readMessage(senderID, receiverID)
        })
        receiverRef!!.addSnapshotListener(MetadataChanges.INCLUDE, EventListener { value, error ->
            if (error != null) {
                Log.w("error", "listen:error", error)
                return@EventListener
            }
            receiverRef!!.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot.contains("status")) {
                        status = snapshot.getString("status")
                        if (status == "Online") binding!!.chatUserStatus.visibility =
                            View.VISIBLE else {
                            binding!!.chatUserStatus.visibility = View.GONE
                        }
                    }
                }
            }
        })
        isSeenlistenerRegistration1 = seenRef1!!.addSnapshotListener(eventListener1!!)
        isSeenlistenerRegistration2 = seenRef2!!.addSnapshotListener(eventListener2!!)
    }

    public override fun onStop() {
        super.onStop()
        if (isSeenlistenerRegistration1 != null) {
            isSeenlistenerRegistration1!!.remove()
        }
        if (isSeenlistenerRegistration2 != null) {
            isSeenlistenerRegistration2!!.remove()
        }
    }

    override fun onPause() {
        super.onPause()
        status("Offline")
        Log.d("status", "onPauseChat: Offline")
    }

    override fun onResume() {
        super.onResume()
        status("Online")
        Log.d("status", "onResumeChat: Online")
    }

    private fun status(status: String) {
        userRef!!.update("status", status)
    }
}