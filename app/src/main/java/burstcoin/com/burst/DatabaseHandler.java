package burstcoin.com.burst;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Aiyaz Parmar on 7/4/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    public DatabaseHandler(Context context) {
        super(context, "burstSQLite", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE burstDATA (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT,phrase TEXT,pin TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertItem(ContentValues contentValues,SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.insert("burstDATA",null,contentValues);
    }


    public ArrayList<DataModel> getItems(SQLiteDatabase sqLiteDatabase){
        Cursor cursor = sqLiteDatabase.rawQuery("select * from burstDATA",null);
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            ArrayList<DataModel> data = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                DataModel dataModel = new DataModel();
                dataModel.setId(cursor.getString(cursor.getColumnIndex("id")));
                dataModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                dataModel.setPhrase(cursor.getString(cursor.getColumnIndex("phrase")));
                dataModel.setPin(cursor.getString(cursor.getColumnIndex("pin")));
                data.add(dataModel);
                cursor.moveToNext();
            }
            return data;
        }else{
            return null;
        }
    }

    public long deleteRecord(SQLiteDatabase sqLiteDatabase, String condition)
    {

        if (sqLiteDatabase.isOpen())
        {
            return sqLiteDatabase.delete("burstDATA", condition, null);
        }
        return 0;
    }

}
