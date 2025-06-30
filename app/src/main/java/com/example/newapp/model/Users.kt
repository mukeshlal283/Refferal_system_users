package com.example.newapp.model

data class Users(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val referralCode: String = "",
    val points: Int = 0,
    val referrerUsersList: ArrayList<String> = arrayListOf(),
    val newUserList: ArrayList<NewUser> = arrayListOf()
)
