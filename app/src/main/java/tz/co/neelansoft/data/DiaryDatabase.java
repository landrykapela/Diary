package tz.co.neelansoft.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

/**
 * Created by landre on 27/06/2018.
 */

@Database(entities = {DiaryEntry.class}, version=1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class DiaryDatabase extends RoomDatabase {

    private static final String TAG = "DiaryDatabase";
    private static final String DATABASE_NAME = "diary";
    private static final Object LOCK = new Object();

    private static DiaryDatabase sDbInstance;

    public static DiaryDatabase getDatabaseInstance(Context context){
        if(sDbInstance == null){
            synchronized (LOCK){
                Log.e(TAG, "Creating database");
                sDbInstance = Room.databaseBuilder(context.getApplicationContext(),DiaryDatabase.class, DiaryDatabase.DATABASE_NAME)
                        .build();

            }
        }
        Log.d(TAG,"Retrieving database instance");
        return sDbInstance;
    }

    public abstract DiaryEntryDao entryDao();
}
