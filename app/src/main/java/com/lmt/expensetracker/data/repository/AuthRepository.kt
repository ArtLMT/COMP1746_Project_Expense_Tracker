package com.lmt.expensetracker.data.repository

class AuthRepository {
    fun login(email: String, password: String): Boolean{
        return email.contains("@") && password.length > 5
    }
}