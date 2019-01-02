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
import org.kzilla.srmkzilla.Utils.getShareLink
import org.kzilla.srmkzilla.db.UpcomingEvent
import java.text.SimpleDateFormat
import java.util.*

class UpcomingEventRecyclerAdapter(private val mDataset: ArrayList<UpcomingEvent>) :
    RecyclerView.Adapter<UpcomingEventRecyclerAdapter.ViewHolder>() {

    internal lateinit var context: Context

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UpcomingEventRecyclerAdapter.ViewHolder {
        context = parent.context
        val v = LayoutInflater.from(context)
            .inflate(R.layout.event_recycler, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event_name = holder.view.findViewById<View>(R.id.event_name) as TextView
        val event_date = holder.view.findViewById<View>(R.id.event_date) as TextView
        val share = holder.view.findViewById<ImageView>(R.id.share)
        event_name.text = mDataset[position].eventName
        event_date.text = String.format(
            "Date: %s",
            parseEventDates(mDataset[position].eventStart, mDataset[position].eventEnd)
        )
        val event_icon = holder.view.findViewById<View>(R.id.event_icon) as ImageView
        // todo: change default and error images
        //event_icon.setDefaultImageResId(R.drawable.kzilla_logo);
        //event_icon.setErrorImageResId(R.drawable.kzilla_logo);
        //event_icon.setImageUrl(mDataset.get(position).eventIconSmallURL);
        val card = holder.view.findViewById<View>(R.id.card) as CardView
        card.setOnClickListener {
            /*
                Intent intent = new Intent(context,RegistrationActivity.class);
                intent.putExtra("event_id",mDataset.get(position).eventID);
                intent.putExtra("event_name",mDataset.get(position).eventName);
                intent.putExtra("event_date",parseEventDates(mDataset.get(position).eventStartDate,mDataset.get(position).eventEndDate));
                intent.putExtra("event_venue",mDataset.get(position).eventVenue);
                intent.putExtra("event_description",mDataset.get(position).eventDetailedContent);
                intent.putExtra("poster_url",mDataset.get(position).eventIconLargeURL);
                context.startActivity(intent);
                */
        }
        share.setOnClickListener { shareEvent(mDataset[position].eventId) }

    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    private fun parseEventDates(start_date: Date?, end_date: Date?): String {
        val dfDate = SimpleDateFormat("dd/MM/yyyy")
        if (dfDate.format(start_date) == dfDate.format(end_date)) {
            val sameDay = SimpleDateFormat("MMM dd, yyyy | hh:mm a")
            return sameDay.format(start_date)
        } else {
            val multiDay = SimpleDateFormat("MMM dd")
            return String.format("%s - %s", multiDay.format(start_date), multiDay.format(end_date))
        }
    }

    private fun shareEvent(event_id: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.event_share_text) + getShareLink(event_id))
        context.startActivity(shareIntent)
    }

}