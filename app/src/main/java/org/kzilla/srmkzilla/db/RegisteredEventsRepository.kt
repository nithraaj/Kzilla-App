package org.kzilla.srmkzilla.db

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object RegisteredEventsRepository{

    private lateinit var eventsDatabase: EventsDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    private val max_refresh_period_seconds = 180 // 3 minutes

    fun init(applicationContext: Context):RegisteredEventsRepository{
        eventsDatabase = Room.databaseBuilder(applicationContext, EventsDatabase::class.java, "registered_events").allowMainThreadQueries().build()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mAuth = FirebaseAuth.getInstance()
        return this
    }

    fun getRegistrationById(registrationId: String, fromNetwork: Boolean?): LiveData<RegisteredEventsData>{
        val recentlyUpdated = eventsDatabase.registeredEventsDao().hasEvent(registrationId, getMaxRefreshTime()) !=null
        return if(fromNetwork!=null && fromNetwork){
            fetchRegistrationFromFirebase(registrationId)
        } else if(!recentlyUpdated){
            fetchRegistrationFromFirebase(registrationId)
        } else{
            fetchRegistrationFromDatabase(registrationId)
        }
    }

    fun getRegistrations(fromNetwork: Boolean?): LiveData<RegisteredEventsData>{
        val recentlyUpdated = eventsDatabase.registeredEventsDao().lastRefreshed()?.after(getMaxRefreshTime())
        return if(fromNetwork!=null && fromNetwork){
            fetchAllRegistrationsFromFirebase()
        } else if(recentlyUpdated==null || !recentlyUpdated){
            fetchAllRegistrationsFromFirebase()
        } else{
            fetchAllRegistrationsFromDatabase()
        }

    }

    private fun fetchAllRegistrationsFromDatabase():LiveData<RegisteredEventsData> {
        val data = MutableLiveData<RegisteredEventsData>()
        data.postValue(RegisteredEventsData(Status.Fetching,null,null))
        data.postValue(RegisteredEventsData(Status.FetchOK, eventsDatabase.registeredEventsDao().getAll(),"database"))
        return data
    }

    private fun fetchAllRegistrationsFromFirebase():LiveData<RegisteredEventsData> {
        val firestore_db = FirebaseFirestore.getInstance()
        val registrationRef = firestore_db.collection("registrations")
        val registeredQuery = registrationRef.whereEqualTo("user_id",mAuth.getUid()).whereLessThan("status",2)
        val eventsLive = MutableLiveData<RegisteredEventsData>()
        eventsLive.postValue(RegisteredEventsData(Status.Fetching,null,null))
        registeredQuery.get().addOnSuccessListener { queryDocumentSnapshots ->
            val registeredEvents = ArrayList<RegisteredEvent>()
            if(queryDocumentSnapshots.isEmpty){
                eventsLive.postValue(RegisteredEventsData(Status.FetchOK,registeredEvents,"firebase"))
            }
            for (document in queryDocumentSnapshots.documents) {
                val event_id = document.getString("event_id")
                val docRef = firestore_db.collection("events").document(event_id!!)
                docRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val doc = task.result
                        if (doc!!.exists()) {
                            val data = RegisteredEvent(
                                document.id,
                                event_id,
                                doc.getString("event_name"),
                                doc.getTimestamp("event_start")?.toDate(),
                                doc.getTimestamp("event_end")?.toDate(),
                                parseEventDate(doc.getTimestamp("event_start")!!.toDate()),
                                doc.getString("event_venue"),
                                Calendar.getInstance().time
                                )
                            registeredEvents.add(data)
                            eventsDatabase.registeredEventsDao().save(data)
                            eventsLive.postValue(RegisteredEventsData(Status.FetchOK,registeredEvents,"firebase"))
                        } else {
                            eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
                        }
                    }
                    else {
                        eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
                    }
                }
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
            eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
        }
        return eventsLive
    }

    private fun fetchRegistrationFromDatabase(registrationId: String):LiveData<RegisteredEventsData> {
        val data = MutableLiveData<RegisteredEventsData>()
        data.postValue(RegisteredEventsData(Status.Fetching,null,null))
        val list = ArrayList<RegisteredEvent>()
        list.add(eventsDatabase.registeredEventsDao().getById(registrationId))
        data.postValue(RegisteredEventsData(Status.FetchOK, list,"database"))
        return data
    }

    private fun fetchRegistrationFromFirebase(registrationId: String):LiveData<RegisteredEventsData> {
        val firestore_db = FirebaseFirestore.getInstance()
        val registrationRef = firestore_db.collection("registrations")
        val registeredQuery = registrationRef.document(registrationId)
        val eventsLive = MutableLiveData<RegisteredEventsData>()
        eventsLive.postValue(RegisteredEventsData(Status.Fetching,null,null))
        registeredQuery.get().addOnSuccessListener { document ->
            val registeredEvents = ArrayList<RegisteredEvent>()
            val event_id = document.getString("event_id")
            val docRef = firestore_db.collection("events").document(event_id!!)
            docRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val doc = task.result
                    if (doc!!.exists()) {
                        val data = RegisteredEvent(
                            document.id,
                            event_id,
                            doc.getString("event_name"),
                            doc.getTimestamp("event_start")?.toDate(),
                            doc.getTimestamp("event_end")?.toDate(),
                            parseEventDate(doc.getTimestamp("event_start")!!.toDate()),
                            doc.getString("event_venue"),
                            Calendar.getInstance().time
                        )
                        registeredEvents.add(data)
                        eventsDatabase.registeredEventsDao().save(data)
                        eventsLive.postValue(RegisteredEventsData(Status.FetchOK,registeredEvents,"firebase"))

                    } else {
                        eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
                    }
                }
                else {
                    eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
                }
            }

        }.addOnFailureListener { e ->
            e.printStackTrace()
            eventsLive.postValue(RegisteredEventsData(Status.Error,null,null))
        }
        return eventsLive
    }

    private fun getMaxRefreshTime(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, -max_refresh_period_seconds)
        return cal.time
    }

    private fun parseEventDate(date: Date): String {
        val sameDay = SimpleDateFormat("MMM dd, yyyy | hh:mm a")
        return sameDay.format(date)
    }

    data class RegisteredEventsData(var status:Status, var data:List<RegisteredEvent>?, var source:String?)
    enum class Status{
        Fetching,
        Error,
        FetchOK
    }
}