package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.kzilla.srmkzilla.db.RegisteredEvent
import java.util.*

class RegisteredEventRecyclerAdapter(private val mDataset: ArrayList<RegisteredEvent>) :
    RecyclerView.Adapter<RegisteredEventRecyclerAdapter.ViewHolder>() {

    internal lateinit var context: Context

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RegisteredEventRecyclerAdapter.ViewHolder {
        context = parent.context
        val v = LayoutInflater.from(context)
            .inflate(R.layout.registered_recycler, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event_name = holder.view.findViewById<View>(R.id.event_name) as TextView
        val event_venue = holder.view.findViewById<View>(R.id.event_venue) as TextView
        val event_date = holder.view.findViewById<View>(R.id.event_date) as TextView
        event_name.text = mDataset[position].eventName
        event_venue.text = String.format("Venue: %s", mDataset[position].eventVenue)
        event_date.text = String.format("Date: %s", mDataset[position].eventDate)
        val event_QRcode = holder.view.findViewById<View>(R.id.event_QRcode) as ImageView
        event_QRcode.setImageBitmap(Utils.generateQrCode(mDataset[position].registrationId))
        val card = holder.view.findViewById<View>(R.id.card) as CardView
        card.setOnClickListener {
            var intent = Intent(context,QRActivity::class.java)
            intent.putExtra("registration_id",mDataset.get(position).registrationId)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }
}