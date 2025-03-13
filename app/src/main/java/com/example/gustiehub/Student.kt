package com.example.gustiehub

import android.graphics.Picture
import java.time.Year

class Student(val _email:String) {
    private var email: String;
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var profilePicture: Picture
    private lateinit var gradYear: Year
    private var joinedGroups:MutableList<Group>

    init {
        this.email = _email;
        // automatically join Gustavus Student Group
        this.joinedGroups = mutableListOf(Gusties)
    }

    // Setters
    fun set_firstName(_firstName:String) {
        this.firstName = _firstName
    }

    fun set_lastName(_lastName:String) {
        this.lastName = _lastName
    }

    fun set_profilePicture(_profilePicture:Picture) {
        this.profilePicture = _profilePicture
        // upload picture to firebase
    }

    fun set_gradYear(_gradYear:Year) {
        this.gradYear = _gradYear
    }

    // Getters
    fun get_firstName(): String {
        return this.firstName
    }

    fun get_lastName(): String {
        return this.lastName
    }

    fun set_profilePicture(): Picture {
        return this.profilePicture
    }

    fun set_gradYear(): Year {
        return this.gradYear
    }
}