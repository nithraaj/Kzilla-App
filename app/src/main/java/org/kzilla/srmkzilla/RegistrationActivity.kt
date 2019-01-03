package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_registration.*
import org.kzilla.srmkzilla.Utils.getShareLink
import org.kzilla.srmkzilla.db.UpcomingEventsRepository
import org.kzilla.srmkzilla.viewModel.EventClass
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    lateinit var eventsModel:EventClass
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var currentTheme:String
    private lateinit var mAuth: FirebaseAuth
    lateinit var event_id:String
    lateinit var upcomingEvents: LiveData<UpcomingEventsRepository.UpcomingEventsData>
    lateinit var firestore_db:FirebaseFirestore

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
        setContentView(R.layout.activity_registration)
        context = this
        event_id = intent.extras.getString("event_id")
        eventsModel = ViewModelProviders.of(this).get(EventClass::class.java)
        upcomingEvents = eventsModel.getEventById(event_id,null)
        upcomingEvents.observe(this@RegistrationActivity, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("upcoming_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateUI(it)
        })
        mAuth = FirebaseAuth.getInstance()
        firestore_db = FirebaseFirestore.getInstance()
        checkRegistration()


    }

    private fun updateUI(data:UpcomingEventsRepository.UpcomingEventsData){
        when(data.status){
            UpcomingEventsRepository.Status.Error -> {

            }
            UpcomingEventsRepository.Status.Fetching -> {

            }
            UpcomingEventsRepository.Status.FetchOK -> {
                val orientation = resources.configuration.orientation
                if(orientation==Configuration.ORIENTATION_PORTRAIT){
                    Glide.with(context).load(data.data!![0].eventBannerPort).into(event_poster)
                }
                else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    Glide.with(context).load(data.data!![0].eventBannerLand).into(event_poster)
                }
                else{
                    Glide.with(context).load(data.data!![0].eventBannerPort).into(event_poster)
                }
                event_name.text = data.data!![0].eventName
                event_venue.text = data.data!![0].eventVenue
                event_date.text = parseEventDates(data.data!![0].eventStart,data.data!![0].eventEnd)
                event_desc.text = data.data!![0].eventContent
                bt_register.setOnClickListener { register() }
                bt_share.setOnClickListener { shareEvent() }
            }
        }

    }

    private fun register() {
        val registration = HashMap<String, Any>()
        registration["event_id"] = event_id
        registration["registered_at"] = FieldValue.serverTimestamp()
        registration["status"] = 0
        registration["user_id"] = mAuth.uid as String
        firestore_db.collection("registrations")
            .add(registration)
            .addOnSuccessListener {
                bt_register.text = "REGISTERED"
                bt_register.isEnabled = false
            }
            .addOnFailureListener { e ->
                bt_register.text = "REGISTER"
                bt_register.isEnabled = true
                Toast.makeText(context, "Unable to register", Toast.LENGTH_LONG).show()
                Log.d("Registration", e.message)
            }
    }

    private fun checkRegistration() {
        val ref = firestore_db.collection("registrations")
        val query = ref.whereEqualTo("event_id", event_id).whereEqualTo("user_id", mAuth.uid)
        query.get()
            .addOnCompleteListener { task ->
                var registered = false
                if (task.isSuccessful) {
                    val snapshot = task.result
                    for (doc in snapshot!!.documents) {
                        if (doc.exists()) {
                            bt_register.text = "REGISTERED"
                            bt_register.isEnabled = false
                            registered = true
                        }
                    }
                    if (!registered) {
                        bt_register.text = "REGISTER"
                        bt_register.isEnabled = true
                    }
                } else {
                    bt_register.text = "REGISTER"
                    bt_register.isEnabled = true
                }
            }
    }

    private fun parseEventDates(start_date: Date?, end_date: Date?): String {
        val dfDate = SimpleDateFormat("dd/MM/yyyy")
        return if (dfDate.format(start_date) == dfDate.format(end_date)) {
            val sameDay = SimpleDateFormat("MMM dd, yyyy | hh:mm a")
            sameDay.format(start_date)
        } else {
            val multiDay = SimpleDateFormat("MMM dd")
            String.format("%s - %s", multiDay.format(start_date), multiDay.format(end_date))
        }
    }

    private fun shareEvent() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.event_share_text) + getShareLink(event_id))
        startActivity(shareIntent)
    }
    override fun onResume() {
        super.onResume()
        val theme = sharedPreferences.getString("theme", "light")
        if (currentTheme != theme)
            recreate()
    }
}
