package com.myoptimind.get_express.features.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "declined_request")
data class DeclinedRequest(

    @PrimaryKey(autoGenerate = true)
    @Nonnull
    val id: Int,

    @ColumnInfo(name = "cart_id")
    var cartId: String?
){
    constructor(
        cartId: String?
    ): this(0, cartId)
}