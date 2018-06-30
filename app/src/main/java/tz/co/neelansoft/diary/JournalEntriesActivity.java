package tz.co.neelansoft.diary;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import tz.co.neelansoft.Utils.Config;
import tz.co.neelansoft.Utils.DiaryPreferenceUtils;
import tz.co.neelansoft.data.DiaryDatabase;
import tz.co.neelansoft.data.DiaryEntry;
import tz.co.neelansoft.data.DiaryEntryAdapter;
import tz.co.neelansoft.data.DiaryExecutors;

/**
 * Created by landre on 26/06/2018.
 */

@SuppressWarnings("ConstantConditions")
public class JournalEntriesActivity extends AppCompatActivity implements DiaryEntryAdapter.ItemClickListener{

    private static final String TAG = "JournalEntriesActivity";
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFirebaseDatabase;
    private DiaryPreferenceUtils mDiaryPreferenceUtils;

    private DiaryDatabase mDatabase;
    private DiaryEntryAdapter mDiaryEntryAdapter;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_journal_entries);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_DB_REFERENCE);

        mRecyclerView         = findViewById(R.id.recyclerView);
        mFloatingActionButton = findViewById(R.id.fab);

        //open add new entry form
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDiaryEntry();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);


        //initialize DiaryPreference
        mDiaryPreferenceUtils = new DiaryPreferenceUtils(this);

        updateTitle();

        mDatabase = DiaryDatabase.getDatabaseInstance(this);

        mDiaryEntryAdapter = new DiaryEntryAdapter(this,this);
        mRecyclerView.setAdapter(mDiaryEntryAdapter);
        final String userId = mFirebaseAuth.getCurrentUser().getUid();
        retrieveDiaryEntries(userId);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JournalEntriesActivity.this)
                        .setTitle(R.string.dialog_title_delete)
                        .setMessage(R.string.dialog_confirm_delete)
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDiaryEntry(viewHolder);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                retrieveDiaryEntries(userId);
                            }
                        });
                        if(!builder.create().isShowing()) builder.create().show();
            }
        }).attachToRecyclerView(mRecyclerView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int clickItemId = item.getItemId();
        switch (clickItemId){
            case R.id.action_signout:
                signout();
                return true;
            case R.id.action_backup:
                backupToCloud();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTitle(){
        String[] splits = mDiaryPreferenceUtils.getUserName().split(" ");
        if(splits != null && splits.length > 0) {
            String title = splits[0]+"'s Diary";
            getSupportActionBar().setTitle(title);
        }
    }

    private void deleteDiaryEntry(final RecyclerView.ViewHolder vh){
        DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<DiaryEntry> entries = mDiaryEntryAdapter.getDiaryEntries();
                int index = vh.getAdapterPosition();
                DiaryEntry entryToDelete = entries.get(index);
                mDatabase.entryDao().deleteEntry(entryToDelete);
            }
        });
    }

    private void signout(){

        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(JournalEntriesActivity.this)
                    .setCancelable(true)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.confirm_signout)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which){

                                mFirebaseAuth.signOut();
                            GoogleSignInClient gsc = GoogleSignIn.getClient(JournalEntriesActivity.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build());
                            gsc.signOut();
                                mDiaryPreferenceUtils.unsetUser();
                                Intent signoutIntent = new Intent(getApplicationContext(), SigninActivity.class);
                                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(signoutIntent);
                                finish();
                            }

                    })
                    .setNegativeButton(R.string.no,new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            return;
                        }
                    });

            if(!builder.create().isShowing()) builder.create().show();

        }

    }

    private void retrieveDiaryEntries(final String user_id){
        LiveData<List<DiaryEntry>> entries = mDatabase.entryDao().getEntries(mDiaryPreferenceUtils.getUserId());
        entries.observe(this, new Observer<List<DiaryEntry>>() {
            @Override
            public void onChanged(@Nullable List<DiaryEntry> diaryEntries) {
                Log.d(TAG,"Live data is working....");
                if(diaryEntries != null) {
                    mDiaryEntryAdapter.setDiaryEntries(diaryEntries);
                    if (diaryEntries.size() == 0) {
                        checkBackup(user_id);
                    }
                }
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent entryDetailIntent = new Intent(JournalEntriesActivity.this,EntryDetailActivity.class);
        entryDetailIntent.putExtra(EntryDetailActivity.EXTRA_ENTRY_KEY,itemId);
        startActivity(entryDetailIntent);
    }

    private void createNewDiaryEntry(){
        startActivity(new Intent(JournalEntriesActivity.this,AddEntryActivity.class));
    }

    private void backupToCloud(){
        Intent firebaseBackupIntent = new Intent(JournalEntriesActivity.this,FirebaseBackupActivity.class);
        startActivity(firebaseBackupIntent);
    }

    private void checkBackup(final String user_id){

       if(mDiaryPreferenceUtils.isUserLoggedIn()){

           mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   for(DataSnapshot ds: dataSnapshot.getChildren()){
                       if(ds.getKey().equals(mFirebaseAuth.getCurrentUser().getUid())){
                           Log.i(TAG,"backup exists: "+ds.getKey());
                           suggestRestore(user_id);
                           break;
                       }
                       else{
                           Log.i(TAG,"backup does not exist: "+mFirebaseAuth.getCurrentUser().getUid()+"/"+ds.getKey());
                       }
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });

       }

    }

    private void suggestRestore(final String user_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(JournalEntriesActivity.this)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.dialog_restore_suggestion)
                .setPositiveButton(R.string.restore, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which){
                        restoreData(user_id);
                    }

                })
                .setNegativeButton(R.string.no,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });

        if(!builder.create().isShowing()) builder.create().show();
    }

    private void restoreData(String user_id){
        mFirebaseDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    final DiaryEntry entry = ds.getValue(DiaryEntry.class);
                    DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {

                            mDatabase.entryDao().addEntry(entry);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
