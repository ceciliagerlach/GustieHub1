package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Post (private val _creatorId: String,
            private val _group: String,
            private val _text: String){
    private val creatorId = _creatorId
    private val group = _group
    private var text = _text

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {

    }
//
//    fun getCreatorId(): String {
//        return this.creatorId
//    }
//
//    fun getGroup(): String {
//        return this.group
//    }
//
//    fun getText(): String {
//        return this.text
//    }
//
//    fun setText(newText: String) {
//        this.text = newText
//    }
}