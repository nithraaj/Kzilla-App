package org.kzilla.srmkzilla.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.*


@Dao
interface CarouselDao {

    @Insert(onConflict = REPLACE)
    fun save(vararg carousel:CarouselData)

    @Delete
    fun delete(carousel:CarouselData)


    @Query("SELECT * FROM carousel")
    fun getAll(): List<CarouselData>



    @Query("SELECT * FROM carousel")
    fun getAllLive(): LiveData<List<CarouselData>>


    @Query("SELECT * FROM carousel where carousel_id = :carouselId")
    fun getById(carouselId:String): CarouselData

    @Query("SELECT * FROM carousel where carousel_id = :carouselId")
    fun getByIdLive(carouselId:String): LiveData<CarouselData>

    @Query("SELECT * FROM carousel WHERE carousel_id = :carouselId AND last_refresh > :lastRefreshMax LIMIT 1")
    fun hasCarousel(carouselId: String, lastRefreshMax: Date): CarouselData?

    @Query("SELECT min(last_refresh) FROM carousel")
    fun lastRefreshed():Date?

}