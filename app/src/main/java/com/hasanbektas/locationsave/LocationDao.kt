package com.hasanbektas.locationsave

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


@Dao
interface LocationDao {

    @Query("SELECT * FROM Location")
    fun getAll() : Flowable<List<Location>>

    @Insert
    fun insert(place : Location) : Completable

    @Delete
    fun delete (place : Location) : Completable
}