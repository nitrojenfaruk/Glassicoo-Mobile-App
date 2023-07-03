package com.sefacicek.glassicoapp

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_mail") val userMail: String,
    @ColumnInfo(name = "user_avatar") val userAvatar: String,
)

