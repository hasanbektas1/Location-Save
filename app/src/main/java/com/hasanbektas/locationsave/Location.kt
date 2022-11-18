package com.hasanbektas.locationsave

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Location (
    @ColumnInfo(name="name")
    var name: String,

    @ColumnInfo(name="latitude")
    var latitude : Double,

    @ColumnInfo(name = "longitude")
    var longitude : Double,

    @ColumnInfo(name="image")
    var image : ByteArray

            ) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    }