package com.cczhr.otglocation.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/**
 * @author cczhr
 * @description 坐标收藏字段
 * @since 2021/3/15
 */
@Entity/*(indices = [Index(value = ["info"], unique = true)])*/
data class LocationData(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var lat: String = "",
    var lon: String = "",
    var info: String = ""){

    override fun toString(): String {
        return info

    }
}
