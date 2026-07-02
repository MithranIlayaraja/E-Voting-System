package com.example.votingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "VotingDB";
    private static final int DB_VERSION = 1;

    // Table: Parties
    private static final String TABLE_PARTY = "parties";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "party_name";
    private static final String COL_CANDIDATE = "candidate_name";
    private static final String COL_SYMBOL = "symbol_path"; // store image path
    private static final String COL_PHOTO = "photo_path";   // store image path
    private static final String COL_VOTES = "votes";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PARTY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_CANDIDATE + " TEXT, " +
                COL_SYMBOL + " TEXT, " +
                COL_PHOTO + " TEXT, " +
                COL_VOTES + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTY);
        onCreate(db);
    }

    // Insert party
    public boolean addParty(String name, String candidate, String symbol, String photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_CANDIDATE, candidate);
        cv.put(COL_SYMBOL, symbol);
        cv.put(COL_PHOTO, photo);
        long result = db.insert(TABLE_PARTY, null, cv);
        return result != -1;
    }

    // Get all parties
    public Cursor getParties() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PARTY, null);
    }

    // Add vote
    public void addVote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PARTY + " SET " + COL_VOTES + " = " + COL_VOTES + " + 1 WHERE id=" + id);
    }

    // Get results
    public Cursor getResults() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_NAME + ", " + COL_VOTES + " FROM " + TABLE_PARTY, null);
    }
}
