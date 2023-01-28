package com.kodego.velascoben.nrw.db

data class Users(
    val userFirst : String,
    val userLast : String,
    val userName : String,
    val userPass : String,
    val userType : String,
    val userPhoto : Int
    ) {
}