package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_qr.*
import org.kzilla.srmkzilla.Utils.generateQrCode
import org.kzilla.srmkzilla.Utils.getShareLink
import org.kzilla.srmkzilla.db.RegisteredEventsRepository
import org.kzilla.srmkzilla.viewModel.EventClass
import java.text.SimpleDateFormat
import java.util.*

class QRActivity : AppCompatActivity() {
    lateinit var eventsModel: EventClass
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var currentTheme:String
    lateinit var registration_id:String
    lateinit var event_id:String
    lateinit var registeredEvents: LiveData<RegisteredEventsRepository.RegisteredEventsData>
    lateinit var firestore_db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString("theme", "light")
        val theme: Int = if ( currentTheme == "light") {
            R.style.LightThemeNoActionBar
        } else {
            R.style.DarkThemeNoActionBar
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        context=this
        eventsModel = ViewModelProviders.of(this).get(EventClass::class.java)
        registration_id = intent.extras.getString("registration_id")
        registeredEvents = eventsModel.getRegistrationById(registration_id,null)
        registeredEvents.observe(this@QRActivity, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("registered_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateUI(it)
        })
        qr_code.setImageBitmap(generateQrCode(registration_id))
        firestore_db = FirebaseFirestore.getInstance()
    }

    private fun unregister() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Event").setMessage("Are you sure you want to unregister?")
        builder.setPositiveButton("Unregister") { dialog, id ->
            // User clicked OK button
            bt_unregister.isEnabled = false
            bt_unregister.text = "UNREGISTERING"
            firestore_db.collection("registrations").document(registration_id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Unregistered successfully", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({
                        // wait for delete to be propagated in firebase
                        finish()
                    }, 1000)
                }
                .addOnFailureListener {
                    bt_unregister.isEnabled = true
                    bt_unregister.text = "UNREGISTER"
                    Toast.makeText(context, "Unable to unregister", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancel") { dialog, id ->
            // User cancelled the dialog
            bt_unregister.isEnabled = true
            bt_unregister.text = "UNREGISTER"
        }
        builder.create().show()
    }

    private fun updateUI(data:RegisteredEventsRepository.RegisteredEventsData){
        when(data.status){
            RegisteredEventsRepository.Status.Error -> {

            }
            RegisteredEventsRepository.Status.Fetching -> {

            }
            RegisteredEventsRepository.Status.FetchOK -> {
                event_name.text = data.data!![0].eventName
                event_venue.text = data.data!![0].eventVenue
                event_date.text = parseEventDates(data.data!![0].eventStart,data.data!![0].eventEnd)
                reg_id.text = data.data!![0].registrationId
                event_id = data.data!![0].eventId!!
                bt_share.setOnClickListener { shareEvent() }
                bt_unregister.setOnClickListener { unregister() }
            }
        }

    }

    private fun shareEvent() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.event_share_text) + getShareLink(event_id))
        startActivity(shareIntent)
    }
    private fun parseEventDates(start_date: Date?, end_date: Date?): String {
        val dfDate = SimpleDateFormat("dd/MM/yyyy")
        if (dfDate.format(start_date) == dfDate.format(end_date)) {
            val sameDay = SimpleDateFormat("MMM dd, yyyy | hh:mm a")
            return sameDay.format(start_date)
        } else {
            val multiDay = SimpleDateFormat("MMM dd, hh:mm a")
            return String.format("%s to` %s", multiDay.format(start_date), multiDay.format(end_date))
        }
    }
    override fun onResume() {
        super.onResume()
        val theme = sharedPreferences.getString("theme", "light")
        if (currentTheme != theme)
            recreate()
    }
}
