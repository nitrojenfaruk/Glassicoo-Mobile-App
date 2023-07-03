package com.sefacicek.glassicoapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glass_table")
data class Glass(
    @PrimaryKey(autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "glass_owner") val glassOwner: String,
    @ColumnInfo(name = "glass_name") val glassName: String,
    @ColumnInfo(name = "glass_image") val glassImage: String,
    @ColumnInfo(name = "glass_date") val glassDate: String,
    @ColumnInfo(name = "glass_geo_center") val glassGeoCenter: String,
    @ColumnInfo(name = "glass_montage_line_length") val glassMontageLineLength: String,
    @ColumnInfo(name = "glass_montage_line_center") val glassMontageLineCenter: String,
    @ColumnInfo(name = "glass_montage_line_top_left") val glassMontageLineTopLeft: String,
    @ColumnInfo(name = "glass_montage_line_bottom_right") val glassMontageLineBottomRight: String,
    @ColumnInfo(name = "glass_montage_point_center") val glassMontagePointCenter: String?,
    @ColumnInfo(name = "glass_montage_point_radius") val glassMontagePointRadius: String?,
    @ColumnInfo(name = "glass_width") val glassWidth: String,
    @ColumnInfo(name = "glass_height") val glassHeight: String,
)

