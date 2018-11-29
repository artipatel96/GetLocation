package com.example.art.getlocation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "locations.db";
    public static final String TABLE_NAME = "locations";
    public static final String TABLE_NAME2 = "relations";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "LATITUDE";
    public static final String COL_4 = "LONGITUDE";
    public static final String COL_5 = "ADDRESS";
    public static final String COL_6 = "TIME";
    public static final String COL_7 = "RELATEDTO";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,LATITUDE TEXT," +
                "LONGITUDE TEXT, ADDRESS TEXT, TIME TEXT)");
        db.execSQL("create table " + TABLE_NAME2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, RELATEDTO INTEGER, LATITUDE TEXT," +
                "LONGITUDE TEXT, TIME TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS "+TABLE_NAME);
        db.execSQL("DROP IF TABLE EXISTS "+TABLE_NAME2);
        onCreate(db);
    }

    public boolean insertData(String name, String latitude, String longitude, String address, String time){
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT * FROM " + TABLE_NAME;
        Cursor c = db.rawQuery(q,null);
        if(c.getCount()>0){
            while(c.moveToNext()){
                if(c.getString(1).equals(name) && c.getString(2).equals(latitude) && c.getString(3).equals(longitude) && c.getString(4).equals(address) && c.getString(5).equals(time)){
                    return false;
                }
            }
            c.close();
        }

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_2, name);
            contentValues.put(COL_3, latitude);
            contentValues.put(COL_4, longitude);
            contentValues.put(COL_5, address);
            contentValues.put(COL_6, time);
            long result = db.insert(TABLE_NAME, null, contentValues);
            if (result == -1) {
                return false;
            }
            else {
                return true;
            }
    }

    public boolean insertData2(Integer relatedto, String name, String latitude, String longitude, String time){
        SQLiteDatabase db = this.getWritableDatabase();

        String q = "SELECT * FROM " + TABLE_NAME2;
        Cursor c = db.rawQuery(q,null);
        if(c.getCount()>0){
            while(c.moveToNext()){
                if(c.getInt(1)==(relatedto) && c.getString(2).equals(name) && c.getString(3).equals(latitude) && c.getString(4).equals(longitude)&&c.getString(5).equals(time)){
                    return false;
                }
            }
            c.close();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,latitude);
        contentValues.put(COL_4,longitude);
        contentValues.put(COL_6,time);
        contentValues.put(COL_7,relatedto);

        long result = db.insert(TABLE_NAME2, null, contentValues);
        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getAllData2() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+TABLE_NAME2,null);
        if(c != null){
            c.moveToFirst();
        }
        return c;
    }

    public Cursor viewData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+TABLE_NAME,null);
        if(c != null){
            c.moveToFirst();
        }
        return c;
    }
}