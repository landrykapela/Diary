package tz.co.neelansoft.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import tz.co.neelansoft.diary.R;

/**
 * Created by landre on 28/06/2018.
 */

public class Mood {

    private String title;
    private int mood;

    public Mood(){

    }

    public Mood(String title, int mood){
        this.title = title;
        this.mood = mood;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public void setMood(int mood){
        this.mood = mood;
    }
    public String getTitle(){
        return this.title;
    }

    public int getMood(){
        return this.mood;
    }

    public Bitmap getMoodIcon(Context context){
        String[] moodIcons = context.getResources().getStringArray(R.array.mood_icons);

        int resId = context.getResources().getIdentifier(moodIcons[getMood()],"drawable",context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(),resId);
    }

}
