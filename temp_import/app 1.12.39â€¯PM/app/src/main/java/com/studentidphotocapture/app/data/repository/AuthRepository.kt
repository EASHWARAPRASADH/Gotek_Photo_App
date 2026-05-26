package com.studentidphotocapture.app.data.repository

import com.studentidphotocapture.app.data.dao.UserDao
import com.studentidphotocapture.app.data.model.User

class AuthRepository(private val userDao: UserDao) {
    
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }
    
    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }
    
    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)
    }
}
