package org.kzilla.srmkzilla.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

object UpcomingEventsRepository{

    private lateinit var eventsDatabase: EventsDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private val max_refresh_period_seconds = 180 // 3 minutes

    fun init(applicationContext:Context):UpcomingEventsRepository{
        eventsDatabase = Room.databaseBuilder(applicationContext, EventsDatabase::class.java, "upcoming_events").allowMainThreadQueries().build()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        return this
    }

    fun getEventById(eventId: String, fromNetwork: Boolean?): LiveData<UpcomingEventsData>{
        val recentlyUpdated = eventsDatabase.upcomingEventsDao().hasEvent(eventId, getMaxRefreshTime())!=null
        return if(fromNetwork!=null && fromNetwork){
            fetchEventFromFirebase(eventId)
        } else if(!recentlyUpdated){
            fetchEventFromFirebase(eventId)
        } else{
            fetchEventFromDatabase(eventId)
        }
    }

    fun getEvents(fromNetwork: Boolean?):LiveData<UpcomingEventsData>{
        val recentlyUpdated = eventsDatabase.upcomingEventsDao().lastRefreshed()?.after(getMaxRefreshTime())
        return if(fromNetwork!=null && fromNetwork){
            fetchAllEventsFromFirebase()
        } else if(recentlyUpdated==null || !recentlyUpdated){
            fetchAllEventsFromFirebase()
        } else{
            fetchAllEventsFromDatabase()
        }
    }

    private fun fetchAllEventsFromDatabase():LiveData<UpcomingEventsData>{
        Log.d("eventsrepo","fetching all events from database")
        val data = MutableLiveData<UpcomingEventsData>()
        data.postValue(UpcomingEventsData(Status.Fetching,null,null))
        data.postValue(UpcomingEventsData(Status.FetchOK, eventsDatabase.upcomingEventsDao().getAll(),"database"))
        return data
    }

    private fun fetchEventFromDatabase(eventId:String):LiveData<UpcomingEventsData>{
        Log.d("eventsrepo","fetching  event $eventId from database")
        val data = MutableLiveData<UpcomingEventsData>()
        data.postValue(UpcomingEventsData(Status.Fetching,null,null))
        val list = ArrayList<UpcomingEvent>()
        list.add(eventsDatabase.upcomingEventsDao().getById(eventId))
        data.postValue(UpcomingEventsData(Status.FetchOK, list,"database"))
        return data
    }

    private fun fetchAllEventsFromFirebase():LiveData<UpcomingEventsData>{

        Log.d("eventsrepo","fetching all events from firebase")
        val firestore_db = FirebaseFirestore.getInstance()
        val eventsRef = firestore_db.collection("events")
        val upcomingQuery = eventsRef.whereArrayContains("allowed_depts", sharedPreferences.getString("user_dept","ANY")!!).whereEqualTo("status", "open")
        val eventsLive = MutableLiveData<UpcomingEventsData>()
        eventsLive.postValue(UpcomingEventsData(Status.Fetching,null,null))
        upcomingQuery.get().addOnSuccessListener { queryDocumentSnapshots ->
            val upcomingEvents = ArrayList<UpcomingEvent>()
            Log.d("firebase_docs",queryDocumentSnapshots.size().toString())
            for (document in queryDocumentSnapshots.documents) {
                Log.d("upcoming_doc",document.toString())
                val data = UpcomingEvent(document.id,
                    document.getString("content"),
                    document.getString("event_name"),
                    document.getTimestamp("event_start")?.toDate(),
                    document.getString("img_small"),
                    document.getString("img_large_land"),
                    document.getString("img_large_port"),
                    document.getTimestamp("event_end")?.toDate(),
                    document.getTimestamp("reg_open")?.toDate(),
                    document.getTimestamp("reg_close")?.toDate(),
                    document.getString("event_venue"),
                    document.get("allowed_depts") as ArrayList<String>?,
                    document.get("allowed_year") as ArrayList<Int>?,
                    document.getString("status"),
                    Calendar.getInstance().time
                    )
                upcomingEvents.add(data)
                eventsDatabase.upcomingEventsDao().save(data)
            }
            //eventsDatabase.upcomingEventsDao().save(*(upcomingEvents.toArray() as Array<out UpcomingEvent>))
            eventsLive.postValue(UpcomingEventsData(Status.FetchOK,upcomingEvents,"firebase"))

            Log.d("eventsrepo","eventsLive = "+eventsLive.value.toString())
        }.addOnFailureListener { e ->
            Log.d("AllEventsFromFirebase","failed with error "+e.message)
            //eventsLive.postValue(ArrayList<UpcomingEvent>())
            eventsLive.postValue(UpcomingEventsData(Status.Error,null,"firebase"))
        }
        return eventsLive
    }

    private fun fetchEventFromFirebase(eventId:String):LiveData<UpcomingEventsData>{

        Log.d("eventsrepo","fetching event $eventId from firebase")
        val firestore_db = FirebaseFirestore.getInstance()
        val eventsRef = firestore_db.collection("events")
        val upcomingQuery = eventsRef.document(eventId)
        val eventLive = MutableLiveData<UpcomingEventsData>()
        eventLive.postValue(UpcomingEventsData(Status.Fetching,null,null))
        upcomingQuery.get().addOnSuccessListener { document ->
            val data = UpcomingEvent(document.id,
                document.getString("content"),
                document.getString("event_name"),
                document.getTimestamp("event_start")?.toDate(),
                document.getString("img_small"),
                document.getString("img_large_land"),
                document.getString("img_large_port"),
                document.getTimestamp("event_end")?.toDate(),
                document.getTimestamp("reg_open")?.toDate(),
                document.getTimestamp("reg_close")?.toDate(),
                document.getString("event_venue"),
                document.get("allowed_depts") as ArrayList<String>?,
                document.get("allowed_year") as ArrayList<Int>?,
                document.getString("status"),
                Calendar.getInstance().time
            )
            eventsDatabase.upcomingEventsDao().save(data)
            var list = java.util.ArrayList<UpcomingEvent>()
            list.add(data)
            eventLive.postValue(UpcomingEventsData(Status.FetchOK,list,"firebase"))
        }.addOnFailureListener { e ->
            Log.d("EventFromFirebase","failed with error "+e.message)
            //eventLive.postValue(null)
            eventLive.postValue(UpcomingEventsData(Status.Error,null,"firebase"))
        }
        return eventLive
    }

    private fun getMaxRefreshTime():Date{
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, -max_refresh_period_seconds)
        return cal.time
    }


    data class UpcomingEventsData(var status:Status, var data:List<UpcomingEvent>?, var source:String?)
    enum class Status{
        Fetching,
        Error,
        FetchOK
    }
}