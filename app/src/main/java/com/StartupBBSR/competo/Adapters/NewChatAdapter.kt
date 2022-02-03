package com.StartupBBSR.competo.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.StartupBBSR.competo.Models.chatOfflineModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.databinding.ReceiverTextItemBinding
import com.StartupBBSR.competo.databinding.SenderTextItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class NewChatAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private lateinit var receiverBinding: ReceiverTextItemBinding

    private lateinit var senderBinding: SenderTextItemBinding

    private var messageList = emptyList<chatOfflineModel>()

    val auth = Firebase.auth

    internal val VIEW_TYPE_ONE = 1

    internal val VIEW_TYPE_TWO = 2

    private inner class ViewHolder1 internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var senderTextView: TextView = itemView.findViewById(R.id.tv_sender_text)
        val senderSeen: TextView = itemView.findViewById(R.id.tv_text_seen)
        val senderTime: TextView = itemView.findViewById(R.id.tv_sender_text_time)

        internal fun bind(position: Int) {
            // This method will be called anytime a list item is created or update its data
            //Do your stuff here
            senderTextView.text = messageList[position].msg
            senderSeen.text = messageList[position].fcmSendStatus
            senderTime.text = messageList[position].sendTime
        }
    }

    private inner class ViewHolder2 internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val receiverTextView: TextView = itemView.findViewById(R.id.tv_receiver_text)
        val receiverTime: TextView = itemView.findViewById(R.id.tv_receiver_text_time)

        internal fun bind(position: Int) {
            // This method will be called anytime a list item is created or update its data
            //Do your stuff here
            val time = getDate(messageList[position].receiveTime.toLong(), "dd/MM/yyyy hh:mm aa")
            receiverTextView.text = messageList[position].msg
            receiverTime.text = time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ONE) {
            ViewHolder1(LayoutInflater.from(context).inflate(R.layout.sender_text_item, parent, false))
        } else ViewHolder2(LayoutInflater.from(context).inflate(R.layout.receiver_text_item,parent,false)) //if it's not VIEW_TYPE_ONE then its VIEW_TYPE_TWO

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messageList[position].senderId == auth.uid) { // put your condition, according to your requirements
            (holder as ViewHolder1).bind(position)
        } else {
            (holder as ViewHolder2).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // here you can get decide from your model's ArrayList, which type of view you need to load. Like
        return if (messageList[position].senderId == auth.uid) { // put your condition, according to your requirements
            VIEW_TYPE_ONE
        } else VIEW_TYPE_TWO
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setdata(data : List<chatOfflineModel>)
    {
        this.messageList = data
        notifyDataSetChanged()
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

}