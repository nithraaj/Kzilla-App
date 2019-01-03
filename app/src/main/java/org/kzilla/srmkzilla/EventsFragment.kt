package org.kzilla.srmkzilla


import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.fragment_events.*
import org.kzilla.srmkzilla.db.CarouselRepository
import org.kzilla.srmkzilla.db.RegisteredEventsRepository
import org.kzilla.srmkzilla.db.UpcomingEventsRepository
import org.kzilla.srmkzilla.viewModel.EventClass


class EventsFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var firestore_db: FirebaseFirestore
    lateinit var realtime_db: FirebaseDatabase
    lateinit var mAuth: FirebaseAuth
    lateinit var upcomingAdapter: RecyclerView.Adapter<UpcomingEventRecyclerAdapter.ViewHolder>
    lateinit var registeredAdapter: RecyclerView.Adapter<RegisteredEventRecyclerAdapter.ViewHolder>
    lateinit var eventsModel:EventClass
    lateinit var rootView:View
    lateinit var upcomingEvents: LiveData<UpcomingEventsRepository.UpcomingEventsData>
    lateinit var registeredEvents: LiveData<RegisteredEventsRepository.RegisteredEventsData>


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventsModel = ViewModelProviders.of(this).get(EventClass::class.java)
        upcomingEvents = eventsModel.getUpcomingEvents(null)
        upcomingEvents.observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("upcoming_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateUpcomingEvents(it)
        })
        registeredEvents = eventsModel.getRegisteredEvents(null)
        registeredEvents.observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("registered_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateRegisteredEvents(it)
        })
        val carousel = eventsModel.getCarousel(null)
        carousel.observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("carousel","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateCarousel(it)
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
        rootView = inflater.inflate(R.layout.fragment_events, container, false)
        val registeredLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.findViewById<RecyclerView>(R.id.recycler_registered).layoutManager = registeredLayoutManager
        val upcomingLayoutManager = LinearLayoutManager(context)
        rootView.findViewById<RecyclerView>(R.id.recycler_upcoming).layoutManager = upcomingLayoutManager
        rootView.findViewById<SwipeRefreshLayout>(R.id.swiperefresh).setOnRefreshListener {
            updateEventsFromFirebase()
        }
        //todo: load default image to carousel
        //todo: show no events image
        (activity as MainActivity).setActionBarTitle("Events")
        return rootView
    }

    private fun updateCarousel(data: CarouselRepository.Carousel){
        when(data.status){
            CarouselRepository.Status.Error -> {
                //todo: show default image

                // todo: try loading from database
            }
            CarouselRepository.Status.Fetching -> {
                //todo: show default image
            }
            CarouselRepository.Status.FetchOK -> {
                val orientation = resources.configuration.orientation
                val viewListener = ViewListener { position ->
                    val customView = layoutInflater.inflate(R.layout.carousel_custom, null)
                    val cr = customView.findViewById<ImageView>(R.id.image_carousel)
                    val url = if(orientation==Configuration.ORIENTATION_PORTRAIT){
                        data.data!![position].urlPort!!
                    } else if(orientation==Configuration.ORIENTATION_LANDSCAPE){
                        data.data!![position].urlLand!!
                    } else{
                        data.data!![position].urlPort!!
                    }
                    Glide.with(context!!).load(url).into(cr)
                    customView
                }
                carouselView.setViewListener(viewListener)
                carouselView.pageCount = data.data!!.size
            }
        }
    }

    private fun updateRegisteredEvents(data: RegisteredEventsRepository.RegisteredEventsData){
        when(data.status){
            RegisteredEventsRepository.Status.Error -> {
                title_registered.visibility = View.GONE
                recycler_registered.visibility = View.GONE
                swiperefresh.isRefreshing = false
            }
            RegisteredEventsRepository.Status.Fetching -> {
                title_registered.visibility = View.GONE
                recycler_registered.visibility = View.GONE
                swiperefresh.isRefreshing = true
            }
            RegisteredEventsRepository.Status.FetchOK -> {
                if(!data.data!!.isEmpty()){
                    title_registered.visibility = View.VISIBLE
                    recycler_registered.visibility = View.VISIBLE
                    registeredAdapter = RegisteredEventRecyclerAdapter(ArrayList(data.data!!))
                    recycler_registered.adapter = registeredAdapter
                }
                else{
                    title_registered.visibility = View.GONE
                    recycler_registered.visibility = View.GONE
                }
                swiperefresh.isRefreshing = upcomingEvents.value?.status?.equals(UpcomingEventsRepository.Status.Fetching) == true

            }
        }
    }

    private fun updateUpcomingEvents(data: UpcomingEventsRepository.UpcomingEventsData){
        when(data.status){
            UpcomingEventsRepository.Status.Error -> {
                title_upcoming.visibility = View.GONE
                recycler_upcoming.visibility = View.GONE
                swiperefresh.isRefreshing = false
            }
            UpcomingEventsRepository.Status.Fetching -> {
                title_upcoming.visibility = View.GONE
                recycler_upcoming.visibility = View.GONE
                swiperefresh.isRefreshing = true
            }
            UpcomingEventsRepository.Status.FetchOK -> {
                if(!data.data!!.isEmpty()){
                    title_upcoming.visibility = View.VISIBLE
                    recycler_upcoming.visibility = View.VISIBLE
                    upcomingAdapter = UpcomingEventRecyclerAdapter(ArrayList(data.data!!))
                    recycler_upcoming.adapter = upcomingAdapter
                }
                else{
                    title_upcoming.visibility = View.GONE
                    recycler_upcoming.visibility = View.GONE
                }
                swiperefresh.isRefreshing = registeredEvents.value?.status?.equals(RegisteredEventsRepository.Status.Fetching) == true
            }
        }
    }

    private fun updateEventsFromFirebase(){
        upcomingEvents.removeObservers(this@EventsFragment)
        registeredEvents.removeObservers(this@EventsFragment)
        upcomingEvents = eventsModel.getUpcomingEvents(true)
        registeredEvents = eventsModel.getRegisteredEvents(true)
        upcomingEvents.observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("upcoming_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateUpcomingEvents(it)
        })
        registeredEvents.observe(this@EventsFragment, Observer {
            if(it==null){
                return@Observer
            }
            Log.d("registered_events","status="+it.status.name+" source="+it.source+" data="+it.data.toString())
            updateRegisteredEvents(it)
        })
    }

    override fun onResume() {
        super.onResume()
        updateEventsFromFirebase()
        Log.d("EventsFragment", "resuming")
    }



}
