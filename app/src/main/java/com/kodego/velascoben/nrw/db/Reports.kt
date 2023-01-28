package com.kodego.velascoben.nrw.db

import java.sql.Date
import java.sql.Time

data class Reports(
    val reportDate : String,
    val reportTime : String,
    val repairDate : String,
    val repairTime : String,
    val reportType : String,
    val reportLong : String,
    val reportLat : String,
    val reportUser : String,
    val reportAddress1 : String,
    val reportAddress2 : String,
    val reportPhoto : String
    ) {
}