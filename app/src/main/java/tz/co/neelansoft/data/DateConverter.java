package tz.co.neelansoft.data;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by landre on 27/06/2018.
 */

class DateConverter {

    @TypeConverter
    public static Date toDate(Long timeInMills){
       if(timeInMills != null){
           return new Date(timeInMills);
       }
       else{
           return null;
       }
    }

    @TypeConverter
    public static Long toTimestamp(Date date){
        return date.getTime();
    }
}
