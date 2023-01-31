package com.kodego.velascoben.nrw.db

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UsersDao {

    private val dbReference = FirebaseFirestore.getInstance()

    fun add(user: Users) {
        dbReference.collection("users").document().set(user)
    }

    fun get(): Query {
        return dbReference.collection("users")
    }
//
//    fun remove(key: String) {
//        dbReference.child(key).removeValue()
//    }
//
    fun update(id : String, map : Map <String,String>) {
        //dbReference.child(key).updateChildren(map)
        dbReference.collection("users").document(id).update(map)
    }
}