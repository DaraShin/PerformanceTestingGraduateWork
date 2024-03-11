package com.shinkevich.benchmarkable

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    fun insert(person: Person)
    @Delete
    fun delete(person: Person)
    @Query("DELETE FROM person")
    fun deleteAll()

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    fun getByNameStart(nameStart: String): List<Person>

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    fun getByNameStartSingle(nameStart: String): Single<List<Person>>

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    fun getByNameStartObservable(nameStart: String): Observable<List<Person>>

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    fun getByNameStartLiveData(nameStart: String): LiveData<List<Person>>

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    fun getByNameStartFlow(nameStart: String): Flow<List<Person>>

    @Query("SELECT * FROM person WHERE name LIKE (:nameStart || '%')")
    suspend fun getByNameStartSuspendable(nameStart: String): List<Person>
}