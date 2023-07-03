package com.sefacicek.glassicoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<User>>
    private val repository: AppRepository

    init {
        val userDao = AppDatabase.getInstance(application).userDao()
        val glasssDao = AppDatabase.getInstance(application).glassDao()
        repository = AppRepository(userDao, glasssDao)
        readAllData = repository.readAllData
    }

    // USER

    fun findUser(mail: String) : LiveData<User> {
        return repository.findUser(mail)
    }


    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user = user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user = user)
        }
    }

    // GLASS

    fun findGlass(mail: String) : LiveData<List<Glass>> {
        return repository.findGlass(mail)
    }


    fun addGlass(glass: Glass) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGlass(glass)
        }
    }

    fun updateGlass(glass: Glass) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGlass(glass = glass)
        }
    }

    fun deleteGlass(glass: Glass) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGlass(glass = glass)
        }
    }

}

class AppViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


//class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return AppViewModel(application) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
