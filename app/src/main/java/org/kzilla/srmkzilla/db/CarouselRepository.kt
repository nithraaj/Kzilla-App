package org.kzilla.srmkzilla.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

object CarouselRepository{

    private lateinit var carouselDatabase: EventsDatabase
    private val max_refresh_period_seconds = 600 // 10 minutes

    fun init(applicationContext: Context):CarouselRepository{
        return this
    }

    fun getCarouselById(carouselId: String, fromNetwork:Boolean?):LiveData<Carousel>{
        val recentlyUpdated = carouselDatabase.carouselDao().hasCarousel(carouselId, getMaxRefreshTime())!=null
        return if(fromNetwork!=null && fromNetwork){
            fetchCarouselFromFirebase(carouselId)
        } else if(!recentlyUpdated){
            fetchCarouselFromFirebase(carouselId)
        } else{
            fetchCarouselFromDatabase(carouselId)
        }
    }

    fun getCarousels(fromNetwork:Boolean?):LiveData<Carousel>{
        val recentlyUpdated = carouselDatabase.carouselDao().lastRefreshed()?.after(getMaxRefreshTime())
        return if(fromNetwork!=null && fromNetwork){
            fetchAllCarouselsFromFirebase()
        } else if(recentlyUpdated==null || !recentlyUpdated){
            fetchAllCarouselsFromFirebase()
        } else{
            fetchAllCarouselsFromDatabase()
        }
    }

    fun fetchCarouselFromDatabase(carouselId: String):LiveData<Carousel>{
        val data = MutableLiveData<Carousel>()
        data.postValue(Carousel(Status.Fetching,null,null))
        val list = ArrayList<CarouselData>()
        list.add(carouselDatabase.carouselDao().getById(carouselId))
        data.postValue(Carousel(Status.FetchOK, list,"database"))
        return data
    }

    fun fetchCarouselFromFirebase(carouselId: String):LiveData<Carousel>{
        val firestore_db = FirebaseFirestore.getInstance()
        val carouselRef = firestore_db.collection("carousel").document(carouselId)
        val carouselLive = MutableLiveData<Carousel>()
        carouselLive.postValue(Carousel(Status.Fetching,null,null))
        carouselRef.get().addOnSuccessListener { document ->
            val carousels = ArrayList<CarouselData>()
            val carousel = CarouselData(
                document.id,
                document.getString("url_land"),
                document.getString("url_port"),
                Calendar.getInstance().time)
            carousels.add(carousel)
            carouselDatabase.carouselDao().save(carousel)
            carouselLive.postValue(Carousel(Status.FetchOK,carousels,"firebase"))
        }.addOnFailureListener{e ->
            e.printStackTrace()
            carouselLive.postValue(Carousel(Status.Error,null,null))
        }
        return carouselLive
    }

    fun fetchAllCarouselsFromDatabase():LiveData<Carousel>{
        val data = MutableLiveData<Carousel>()
        data.postValue(Carousel(Status.Fetching,null,null))
        data.postValue(Carousel(Status.FetchOK, carouselDatabase.carouselDao().getAll(),"database"))
        return data
    }

    fun fetchAllCarouselsFromFirebase():LiveData<Carousel>{
        val firestore_db = FirebaseFirestore.getInstance()
        val carouselRef = firestore_db.collection("carousel")
        val carouselLive = MutableLiveData<Carousel>()
        carouselLive.postValue(Carousel(Status.Fetching,null,null))
        carouselRef.get().addOnSuccessListener { querySnapshot ->
            val carousels = ArrayList<CarouselData>()
            for( document in querySnapshot.documents) {
                val carousel = CarouselData(
                    document.id,
                    document.getString("url_land"),
                    document.getString("url_port"),
                    Calendar.getInstance().time)
                carousels.add(carousel)
                carouselDatabase.carouselDao().save(carousel)
            }
            carouselLive.postValue(Carousel(Status.FetchOK,carousels,"firebase"))
        }.addOnFailureListener{e ->
            e.printStackTrace()
            carouselLive.postValue(Carousel(Status.Error,null,null))
        }
        return carouselLive
    }

    private fun getMaxRefreshTime(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, -max_refresh_period_seconds)
        return cal.time
    }


    data class Carousel(var status:Status, var data:List<org.kzilla.srmkzilla.db.CarouselData>?, var source:String?)
    enum class Status{
        Fetching,
        Error,
        FetchOK
    }
}