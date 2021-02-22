package com.myoptimind.get_express.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [
    DeclinedRequest::class
], version = 1)
abstract class GetExpressDB(): RoomDatabase(){
    abstract fun getDeclinedRequestDao(): DeclinedRequestDao
}