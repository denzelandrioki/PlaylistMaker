package com.practicum.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistsDao(): PlaylistsDao
    abstract fun playlistTracksDao(): PlaylistTracksDao
}
