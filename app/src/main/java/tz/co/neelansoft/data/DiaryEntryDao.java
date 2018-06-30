package tz.co.neelansoft.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by landre on 27/06/2018.
 */

@Dao
public interface DiaryEntryDao {

    @Query("SELECT * FROM entry WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<DiaryEntry>> getEntries(String userId);

    @Query("SELECT * FROM entry WHERE userId = :userId ORDER BY id DESC")
    List<DiaryEntry> getEntriesForBackup(String userId);

    @Insert
    void addEntry(DiaryEntry entry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEntry(DiaryEntry entry);

    @Delete
    void deleteEntry(DiaryEntry entry);

    @Query("SELECT * FROM entry WHERE id = :id")
    LiveData<DiaryEntry> loadEntryById(int id);

}
