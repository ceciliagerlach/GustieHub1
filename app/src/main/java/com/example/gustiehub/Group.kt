package com.example.gustiehub

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Group(
    var name: String = "",
    private var creatorId: String = "",
    var members: MutableList<String> = mutableListOf(),
    var description: String = "") {

    private val db= FirebaseFirestore.getInstance()

//    init {
//        createGroup()
//    }

    //create a new group in firestore
    fun initializeGroup() {
        val groupData = hashMapOf(
            "name" to name,
            "creatorId" to creatorId,
            "members" to members,
            "description" to description
        )

        db.collection("groups").document(name)
            .set(groupData)
            .addOnSuccessListener { println("Group created: $name") }
            .addOnFailureListener { e -> println("Error creating group: ${e.message}") }

        // add group to local dictionary
        GlobalData.groupDict.put(name, this)

    }

    //add a member to a group
    fun addMember(userId:String,onComplete:(Boolean,String?)-> Unit){
        db.collection("groups").document(name)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                members.add(userId)
                println("User $userId added to group $name")
                onComplete(true,null)
            }
            .addOnFailureListener { e ->
                println("Error adding user to group: ${e.message}")
                onComplete(false,e.message)
            }
    }

    //remove a member from a group
    fun removeMember(userId:String, onComplete:(Boolean,String?)-> Unit){
        db.collection("groups").document(name)
            .update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                members.remove(userId)
                println("User $userId removed from group $name")
                onComplete(true,null)
                }
            .addOnFailureListener { e ->
                println("Error removing user from group: ${e.message}")
                onComplete(false,e.message)
            }
    }
    //get the members of a group
    //fun getMembers(): List<String> = members
    //get the creatorId of a group
    //fun getCreatorId(): String = creatorId
}

// TODO: Add post structure to Group + Firebase, capable of changing order
// TODO: Add setters + getters for profile pic
// TODO: Create functions createGroup + ones for posts