package tz.co.neelansoft.diary;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tz.co.neelansoft.Utils.DiaryPreferenceUtils;
import tz.co.neelansoft.Utils.Mood;
import tz.co.neelansoft.Utils.MoodSpinnerAdapter;
import tz.co.neelansoft.data.DiaryDatabase;
import tz.co.neelansoft.data.DiaryEntry;
import tz.co.neelansoft.data.DiaryExecutors;

/**
 * Created by landre on 26/06/2018.
 */

public class AddEntryActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_KEY = "entry_id";
    private static final int DEFAULT_ENTRY_ID = -1;
    private static final String TAG = "AddEntryActivity";

    private DiaryDatabase mDatabase;
    private DiaryPreferenceUtils mPreferenceUtil;

    private EditText mEditTextTitle;
    private EditText mEditTextThoughts;
    private Spinner mSpinnerMood;

    private boolean isNewEntry = true;

    private int mEntryId = DEFAULT_ENTRY_ID;


    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        Button mButtonDiscard = findViewById(R.id.btnDiscard);
        Button mButtonSave = findViewById(R.id.btnSave);
        mEditTextTitle    = findViewById(R.id.etTitle);
        mEditTextThoughts = findViewById(R.id.etThoughts);
        TextView mDisplayError = findViewById(R.id.tvError);
        TextView mHeadingLabel = findViewById(R.id.tvLabel);
        mSpinnerMood      = findViewById(R.id.spMood);
        
        mPreferenceUtil = new DiaryPreferenceUtils(this);

        String[] mood_names = getResources().getStringArray(R.array.moods);
        final  List<Mood> moodList = new ArrayList<>();
        for(int i=0; i < mood_names.length; i++){
            moodList.add(i, new Mood(mood_names[i],i));
        }

        MoodSpinnerAdapter moodAdapter = new MoodSpinnerAdapter(this,moodList);

        mSpinnerMood.setAdapter(moodAdapter);

        mDatabase = DiaryDatabase.getDatabaseInstance(this);

        //save data
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNewEntry) {
                    Log.e(TAG,"very true");
                    newDiaryToDatabase();
                }
                else{
                    Log.e(TAG,"Not true");
                    updateDiaryEntry();
                }
            }
        });

        //discard
        mButtonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Get calling intent
        Intent callingIntent = getIntent();
        if(callingIntent != null && callingIntent.hasExtra(EXTRA_ENTRY_KEY)) {
           mEntryId =  callingIntent.getIntExtra(EXTRA_ENTRY_KEY, DEFAULT_ENTRY_ID);

           if(mEntryId != DEFAULT_ENTRY_ID){
               isNewEntry = false;
               mHeadingLabel.setText(getResources().getString(R.string.update_entry_text));
               getSupportActionBar().setTitle(R.string.modify_entry);
               mButtonSave.setText(R.string.update);
               LiveData<DiaryEntry> entry = mDatabase.entryDao().loadEntryById(mEntryId);
               entry.observe(AddEntryActivity.this, new Observer<DiaryEntry>() {
                   @Override
                   public void onChanged(@Nullable DiaryEntry diaryEntry) {
                       Log.d(TAG,"Live data updated");
                       loadContent(diaryEntry);
                   }
               });

           }

            Log.e(TAG, "Selected entry has id: " + getIntent().getIntExtra(EXTRA_ENTRY_KEY, DEFAULT_ENTRY_ID));

        }


    }

    private void newDiaryToDatabase() {
        String title = mEditTextTitle.getText().toString();
        String thoughts = mEditTextThoughts.getText().toString();
        int mood = (int) mSpinnerMood.getSelectedItemId();
        Date dateUpdated = new Date();
        final DiaryEntry entry = new DiaryEntry(title,dateUpdated,thoughts,mood,mPreferenceUtil.getUserId());

        DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.entryDao().addEntry(entry);
            }
        });
        finish();
    }

    private void updateDiaryEntry() {
        String title = mEditTextTitle.getText().toString();
        String thoughts = mEditTextThoughts.getText().toString();
        int mood = mSpinnerMood.getSelectedItemPosition();
        Date dateUpdated = new Date();
        final DiaryEntry entry = new DiaryEntry(title,dateUpdated,thoughts,mood, mPreferenceUtil.getUserId());
        entry.setId(mEntryId);
        DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.entryDao().updateEntry(entry);
            }
        });
        finish();
    }

    private void loadContent(DiaryEntry entry){
        if(entry == null){
            return;
        }
        mEditTextThoughts.setText(entry.getThoughts());
        mEditTextTitle.setText(entry.getTitle());
        mSpinnerMood.setSelection(entry.getMood(),true);
    }
}
