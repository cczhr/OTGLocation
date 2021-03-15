package com.scigrace.controller.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cczhr.otglocation.database.LocationData

/**
 * @author cczhr
 * @description 坐标收藏数据库dao
 * @since 2021/3/15
 */
@Dao
interface LocationDataDao {
    @Query("SELECT * FROM LocationData  ")
    fun getAll(): List<LocationData>


    @Query("DELETE FROM LocationData")
    fun deleteAll()
    
    @Insert
    fun insert(locationData: LocationData)

    @Insert
    fun insertAll(vararg target: LocationData)

    @Insert
    fun insertAll(target: List<LocationData> )


    @Query("DELETE FROM LocationData WHERE  id =:id")
    fun deleteExpiredData(id:Long)
}