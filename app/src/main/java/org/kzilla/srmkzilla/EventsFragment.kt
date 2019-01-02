package org.kzilla.srmkzilla


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.kzilla.srmkzilla.viewModel.EventClass


class EventsFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var firestore_db: FirebaseFirestore
    lateinit var realtime_db: FirebaseDatabase
    lateinit var mAuth: FirebaseAuth
    lateinit var upcomingAdapter: RecyclerView.Adapter<UpcomingEventRecyclerAdapter.ViewHolder>
    lateinit var registeredAdapter: RecyclerView.Adapter<RegisteredEventRecyclerAdapter.ViewHolder>

    lateinit var eventsModel:EventClass


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventsModel = ViewModelProviders.of(this).get(EventClass::class.java)
        eventsModel.getUpcomingEvents(null).observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("upcoming_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
        })
        eventsModel.getRegisteredEvents(null).observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("registered_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
        })
        eventsModel.getCarousel(null).observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("carousel","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
        })
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        firestore_db = FirebaseFirestore.getInstance()
        realtime_db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false)
    }



}
