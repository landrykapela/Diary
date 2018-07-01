package tz.co.neelansoft.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by landre on 26/06/2018.
 */

public class DiaryPreferenceUtils {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "diary_preference";
    private static final String USER_NAME = "diary_pref_name";
    private static final String USER_EMAIL = "diary_pref_email";
    private static final String USER_ID = "diary_pref_id";
    private static final String USER_IS_LOGGED_IN = "diary_pref_is_logged_in";

    //constructor
    @SuppressLint("CommitPrefEdits")
    public DiaryPreferenceUtils(Context context){
        pref = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isUserLoggedIn(){
        return pref.getBoolean(USER_IS_LOGGED_IN, false);
    }

    private void setUserIsLoggedIn(boolean loggedIn){
        editor.putBoolean(USER_IS_LOGGED_IN,loggedIn);
        editor.commit();
    }

    public void setUserName(String name){
        editor.putString(USER_NAME, name);
        editor.commit();
    }

    public String getUserName(){
        return pref.getString(USER_NAME,null);
    }

    private void setUserEmail(String email){
        editor.putString(USER_EMAIL,email);
        editor.commit();
    }

    public void setUserId(String id){
        editor.putString(USER_ID,id);
        editor.commit();
    }

    public String getUserId(){
        return pref.getString(USER_ID,null);
    }



    public void setUser(FirebaseUser user){
        setUserIsLoggedIn(true);
        setUserId(user.getUid());
        setUserEmail(user.getEmail());
        if(user.getDisplayName() != null) {
            setUserName(user.getDisplayName());
        }
        else{
            String[] splits = user.getEmail().split("@");
            if(splits != null && splits.length > 0) {
                String displayName = splits[0];
                setUserName(displayName);
            }
        }

    }


    public void unsetUser(){
        setUserIsLoggedIn(false);
        setUserId(null);
        setUserEmail(null);
        setUserName(null);
    }




}
