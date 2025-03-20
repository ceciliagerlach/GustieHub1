package com.example.gustiehub

data class Group(
    val name: String = "",
    val creatorId: String? = null, // store creator's user ID (UID)
    val members: MutableList<String> = mutableListOf(), // store member UIDs
    val description: String = ""
)

// TODO: Add post structure to Group + Firebase, capable of changing order
// TODO: Add setters + getters for profile pic
// TODO: Create functions createGroup + ones for posts