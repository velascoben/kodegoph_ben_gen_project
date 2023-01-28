package com.kodego.velascoben.nrw.db

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportsDao {
    private val dbReference = FirebaseFirestore.getInstance()

    fun add(report: Reports) {
        dbReference.collection("reports").document().set(report)
    }

    fun get(): Query {
        return dbReference.collection("reports").orderBy("reportDate", Query.Direction.ASCENDING)
    }
//
//    fun remove(key: String) {
//        dbReference.child(key).removeValue()
//    }
//
//    fun update(key : String, map : Map <String,String>) {
//        dbReference.child(key).updateChildren(map)
//    }
}