package com.example.gustiehub

class Group(val _name: String, val _creator: Student?) {
    private val name: String
    private var members: MutableList<Student>

    init {
        this.name = _name
        // if a creator is provided, add them to the members list
        // otherwise, initialize an empty list
        members = if (_creator != null) mutableListOf(_creator) else mutableListOf()
    }
}