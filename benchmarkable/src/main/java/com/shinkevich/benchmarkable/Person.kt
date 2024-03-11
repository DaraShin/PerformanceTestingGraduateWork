package com.shinkevich.benchmarkable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Person(
    @PrimaryKey val id: Int,
    val name: String
)