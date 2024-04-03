package com.example.happyplaces.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.Contacts.SettingsColumns.KEY
import android.provider.SettingsSlicesContract.KEY_LOCATION

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val create =
            "CREATE TABLE $TABLE_HAPPY_PLACE( " +
                    "$KEY_ID INTEGER PRIMARY KEY, " +
                    "$KEY_TITLE TEXT, " +
                    "$KEY_IMAGE TEXT, " +
                    "$KEY_DESCRIPTION TEXT, " +
                    "$KEY_DATE TEXT, " +
                    "$KEY_LOCATION TEXT, " +
                    "$KEY_LATITUDE TEXT, " +
                    "$KEY_LONGITUDE TEXT)"

        db?.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }

    fun addHappyPlace(happyPlaceModel: HappyPlaceModel): Long {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, happyPlaceModel.title)
        contentValues.put(KEY_IMAGE, happyPlaceModel.image)
        contentValues.put(KEY_DESCRIPTION, happyPlaceModel.description)
        contentValues.put(KEY_DATE, happyPlaceModel.date)
        contentValues.put(KEY_LOCATION, happyPlaceModel.location)
        contentValues.put(KEY_LATITUDE, happyPlaceModel.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlaceModel.longitude)

        val result = db.insert(TABLE_HAPPY_PLACE, null, contentValues)
        db.close()
        return result
    }

    fun getHappyPlacesList(): ArrayList<HappyPlaceModel> {
        val happyPlaceList = ArrayList<HappyPlaceModel>()
        val db = this.readableDatabase
        try {
            val cursor = db.rawQuery("SELECT * FROM $TABLE_HAPPY_PLACE", null)
            if (cursor.moveToFirst()) {
                do {
                    val place = HappyPlaceModel(
                        id = cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        title = cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        image = cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        date = cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        location = cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    happyPlaceList.add(place)
                } while (cursor.moveToNext())
            }
            db?.execSQL("SELECT * FROM $TABLE_HAPPY_PLACE")
        } catch (e: SQLiteException) {
            return ArrayList()
        }
        return happyPlaceList
    }

    fun updateHappyPlace(happyPlaceModel: HappyPlaceModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_TITLE, happyPlaceModel.title)
        contentValues.put(KEY_IMAGE, happyPlaceModel.image)
        contentValues.put(KEY_DESCRIPTION, happyPlaceModel.description)
        contentValues.put(KEY_DATE, happyPlaceModel.date)
        contentValues.put(KEY_LOCATION, happyPlaceModel.location)
        contentValues.put(KEY_LATITUDE, happyPlaceModel.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlaceModel.longitude)

        val result = db.update(
            TABLE_HAPPY_PLACE,
            contentValues,
            "$KEY_ID = ${happyPlaceModel.id}",
            null
        )
        db.close()
        return result
    }

    fun deleteHappyPlace(happyPlaceModel: HappyPlaceModel): Int {
        val db = this.writableDatabase
        val success = db.delete(
            TABLE_HAPPY_PLACE,
            "$KEY_ID = ${happyPlaceModel.id}",
            null
        )
        db.close()
        return success
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HappyPlacesDatabase"
        private const val TABLE_HAPPY_PLACE = "HappyPlaceTable"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
}