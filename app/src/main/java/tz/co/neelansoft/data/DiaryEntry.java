package tz.co.neelansoft.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Date;

import tz.co.neelansoft.diary.R;

/**
 * Created by landre on 27/06/2018.
 */

@Entity(tableName = "entry")
public class DiaryEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    @ColumnInfo(name = "time_updated")
    private Date timeUpdated;
    private String thoughts;
    private int mood;
    private String userId;

    //public constructor with no arguments. needed when retrieving firebase data
    @Ignore
    public DiaryEntry(){

    }

    //public constructor with arguments
    @Ignore
    public DiaryEntry(String title, Date timeUpdated, String thoughts, int mood, String userId){

        this.title = title;
        this.timeUpdated = timeUpdated;
        this.thoughts = thoughts;
        this.mood = mood;
        this.userId = userId;
    }

    public DiaryEntry(int id, String title, Date timeUpdated, String thoughts, int mood, String userId){
        this.id = id;
        this.title = title;
        this.timeUpdated = timeUpdated;
        this.thoughts = thoughts;
        this.mood = mood;
        this.userId = userId;
    }

    public int getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public Date getTimeUpdated(){
        return timeUpdated;
    }
    public String getThoughts(){
        return thoughts;
    }

    public int getMood(){
        return mood;
    }


    public void setId(int id){
        this.id = id;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setTimeUpdated(Date timeUpdated){
        this.timeUpdated = timeUpdated;
    }
    public void setThoughts(String thoughts){
        this.thoughts = thoughts;
    }

    public void setMood(int mood){
        this.mood = mood;
    }
    public Bitmap getMoodIcon(Context context){
        String[] moodIcons = context.getResources().getStringArray(R.array.mood_icons);
        int resId = context.getResources().getIdentifier(moodIcons[getMood()],"drawable",context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(),resId);
    }
    public void setUserId(String userid){
        this.userId = userid;
    }
    public String getUserId(){
        return this.userId;
    }
}
