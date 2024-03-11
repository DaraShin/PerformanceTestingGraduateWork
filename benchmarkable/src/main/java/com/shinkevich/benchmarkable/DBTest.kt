package com.shinkevich.benchmarkable

import android.content.Context
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class DBTest(context: Context) {
    val personDao = BenchmarkableDatabase.getDBInstance(context).personDao()

    fun initDatabase() {
        personDao.deleteAll()
        for (i in 1..100) {
            val name = (i % 26 + 'a'.code).toChar().toString().repeat(10)
            personDao.insert(Person(i, name))
        }
    }
}