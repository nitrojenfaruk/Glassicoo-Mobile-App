package com.sefacicek.glassicoapp

import androidx.lifecycle.LiveData

class AppRepository(private val userDao: UserDao, private val glassDao: GlasssDao) {

    val readAllData: LiveData<List<User>> = userDao.getAll()

    fun findUser(mail: String) : LiveData<User> {
        return userDao.findByMail(mail)
    }

    suspend fun addUser(user: User){
        userDao.insert(user)
    }

    suspend fun updateUser(user: User){
        userDao.update(user)
    }

    suspend fun deleteUser(user: User){
        userDao.delete(user)
    }


    fun findGlass(mail: String) : LiveData<List<Glass>> {
        return glassDao.findGlassByMail(mail)
    }

    suspend fun addGlass(glass: Glass){
        glassDao.insert(glass)
    }

    suspend fun updateGlass(glass: Glass){
        glassDao.update(glass)
    }

    suspend fun deleteGlass(glass: Glass){
        glassDao.delete(glass)
    }


}