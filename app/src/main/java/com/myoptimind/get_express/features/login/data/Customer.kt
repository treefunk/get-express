package com.myoptimind.get_express.features.login.data

data class Customer(
    var addresses: List<Address> = listOf()
): User()