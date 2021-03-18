package com.cczhr.otglocation.database

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
    suspend  fun getAll(): List<LocationData>


    @Query("SELECT COUNT(*)   FROM LocationData WHERE info=:info ")
    suspend  fun queryCountByInfo(info:String):Long


    @Query("DELETE FROM LocationData")
    suspend  fun deleteAll()

    @Query("DELETE FROM LocationData WHERE info=:info")
    suspend  fun deleteByInfo(info:String)
    
    @Insert
    suspend   fun insert(locationData: LocationData):Long

    @Insert
    suspend   fun insertAll(vararg target: LocationData)

    @Insert
    suspend   fun insertAll(target: List<LocationData> )


    @Query("DELETE FROM LocationData WHERE  id =:id")
    suspend   fun deleteExpiredData(id:Long)
}