package com.myoptimind.get_express.features.customer.cart.data

enum class CartStatus(val key: String, val label: String, val order: Int) {
    PENDING("pending","Pending",1),
    ACCEPTED("accepted","Accepted",2),
    GOT_ITEMS("got_items", "Got Items",3),
    OTW("otw","On the way",4),
    ARRIVED("arrived_at_destination", "Arrived",5),
    DELIVERED("completed", "Delivered",6),
    CANCELLED("cancelled", "Cancelled", -1),
    INIT("init","Initialized", -1)
}