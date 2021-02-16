package com.myoptimind.get_express.features.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeclinedRequestDao {

    @Query("SELECT * FROM declined_request")
    suspend fun getAllDeclinedRequests(): List<DeclinedRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDeclinedRequest(declinedRequest: DeclinedRequest)

    @Query("DELETE FROM declined_request where 1")
    suspend fun deleteAllDeclinedRequests()

}