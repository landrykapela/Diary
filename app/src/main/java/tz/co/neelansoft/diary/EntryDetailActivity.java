package tz.co.neelansoft.diary;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import tz.co.neelansoft.Utils.DiaryPreferenceUtils;
import tz.co.neelansoft.data.DiaryDatabase;
import tz.co.neelansoft.data.DiaryEntry;

/**
 * Created by landre on 29/06/2018.
 */

public class EntryDetailActivity extends AppCompatActivity {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String EXTRA_ENTRY_KEY = "entry_id";
    public static final int DEFAULT_ENTRY_ID = -1;
    private static final String TAG = "AddEntryActivity";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    private DiaryDatabase mDatabase;
    private DiaryPreferenceUtils mPreferenceUtil;

    private FloatingActionButton mFab;

    private TextView mTextViewTitle;
    private TextView mTextViewThoughts;
    private TextView mTextViewDate;

    private ImageView mMoodImage;



    private int mEntryId = DEFAULT_ENTRY_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry_detail);

        View headerView = findViewById(R.id.header_detail);
        View detailView = findViewById(R.id.body_detail);

        mFab    = findViewById(R.id.fab);

        mMoodImage        = headerView.findViewById(R.id.ivIcon);
        mTextViewTitle    = headerView.findViewById(R.id.tvTitle);
        mTextViewThoughts = detailView.findViewById(R.id.tvThoughts);
        mTextViewDate     = detailView.findViewById(R.id.tvDate);


        mPreferenceUtil = new DiaryPreferenceUtils(this);

        mEntryId = getIntent().getIntExtra(EXTRA_ENTRY_KEY,DEFAULT_ENTRY_ID);

        mDatabase = DiaryDatabase.getDatabaseInstance(this);

        if(savedInstanceState != null){
            mEntryId = savedInstanceState.getInt(EXTRA_ENTRY_KEY,DEFAULT_ENTRY_ID);
            LiveData<DiaryEntry> entry = mDatabase.entryDao().loadEntryById(mEntryId);
            entry.observe(EntryDetailActivity.this, new Observer<DiaryEntry>() {
                @Override
                public void onChanged(@Nullable DiaryEntry diaryEntry) {
                    Log.d(TAG,"Live data updated");
                    loadContent(diaryEntry);
                }
            });

        }
        if(mEntryId != DEFAULT_ENTRY_ID){

            LiveData<DiaryEntry> entry = mDatabase.entryDao().loadEntryById(mEntryId);
            entry.observe(EntryDetailActivity.this, new Observer<DiaryEntry>() {
                @Override
                public void onChanged(@Nullable DiaryEntry diaryEntry) {
                    Log.d(TAG,"Live data updated");
                    loadContent(diaryEntry);
                }
            });
        }


        //save data
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               modifyEntry();

            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_ENTRY_KEY,mEntryId);
    }

    private void modifyEntry(){
        Intent entryDetailIntent = new Intent(EntryDetailActivity.this,AddEntryActivity.class);
        entryDetailIntent.putExtra(AddEntryActivity.EXTRA_ENTRY_KEY,mEntryId);
        startActivity(entryDetailIntent);
    }

    private void loadContent(DiaryEntry entry){
        if(entry == null){
            return;
        }
        mTextViewThoughts.setText(entry.getThoughts());
        mTextViewTitle.setText(entry.getTitle());
        mTextViewDate.setText(dateFormat.format(entry.getTimeUpdated()));
        mMoodImage.setImageBitmap(entry.getMoodIcon(EntryDetailActivity.this));
        mMoodImage.setContentDescription("Mood "+getResources().getStringArray(R.array.moods)[entry.getMood()]);
    }
}
