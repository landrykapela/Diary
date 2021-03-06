package tz.co.neelansoft.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tz.co.neelansoft.Utils.Config;
import tz.co.neelansoft.Utils.DiaryPreferenceUtils;
import tz.co.neelansoft.data.DiaryDatabase;
import tz.co.neelansoft.data.DiaryEntry;
import tz.co.neelansoft.data.DiaryExecutors;

/**
 * Created by landre on 29/06/2018.
 */

public class FirebaseBackupActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseBackupActivity";
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFirebaseReference;
    private DiaryDatabase mDatabase;
    private DiaryPreferenceUtils mPreference;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firebase_backup);

        mProgressBar   = findViewById(R.id.progressBar);
        ImageView mImageCloud = findViewById(R.id.ivCloudBuckup);
        Button mButtonCancel = findViewById(R.id.cancel_backup_button);
        Button mButtonRestore = findViewById(R.id.restore_backup_button);

        mPreference = new DiaryPreferenceUtils(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_DB_REFERENCE);
        mDatabase = DiaryDatabase.getDatabaseInstance(this);

        final String userId = mFirebaseAuth.getCurrentUser().getUid();

        mButtonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        mImageCloud.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //check if user is logged in to app
                if(mPreference.isUserLoggedIn()){
                    //check if user exist
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                    if(firebaseUser == null){
                        requireLogin();
                    }
                    else{
                        final String user_id = firebaseUser.getUid();
                        confirmBackup(user_id);
                    }
                }
               // performBackup();
            }
        });
        mButtonRestore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                checkBackup(userId);
            }
        });
    }

    private void performBackup(final String userid){

                DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        List<DiaryEntry> myEntries = mDatabase.entryDao().getEntriesForBackup(userid);

                        mFirebaseReference.child(userid).setValue(myEntries)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "Backup successful");
                                        hideProgress();
                                        showSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        hideProgress();
                                        Log.e(TAG,"Backup failed: "+e);
                                        Toast.makeText(FirebaseBackupActivity.this, "Backup Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

    }

    private void confirmBackup(final String user_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseBackupActivity.this)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.dialog_backup_confirmation)
                .setPositiveButton(R.string.backup, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which){
                        performBackup(user_id);
                    }

                })
                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });

        if(!builder.create().isShowing()) builder.create().show();
    }
    private void confirmRestore(final String user_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseBackupActivity.this)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.dialog_restore_confirmation)
                .setPositiveButton(R.string.restore, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which){
                        restoreData(user_id);
                    }

                })
                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });

        if(!builder.create().isShowing()) builder.create().show();
    }

    private void restoreData(final String user_id){
        final List<DiaryEntry> planBEntries = new ArrayList<>();
        showProgress();

        DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                for(DiaryEntry de: mDatabase.entryDao().getEntriesForBackup(user_id)){
                    planBEntries.add(de);
                    mDatabase.entryDao().deleteEntry(de);
                }
            }
        });
        mFirebaseReference.child(user_id).addValueEventListener(new ValueEventListener() {
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
                DiaryExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0; i< planBEntries.size();i++){
                            mDatabase.entryDao().addEntry(planBEntries.get(i));
                        }
                    }
                });
            }

        });


        hideProgress();
        showSuccess();
    }

    private void requireLogin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseBackupActivity.this)
                .setTitle(R.string.dialog_require_login_title)
                .setMessage(R.string.dialog_require_login_message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.signin), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLogin();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        if(!builder.create().isShowing()) builder.create().show();
    }
    private void showSuccess(){
        hideProgress();
        AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseBackupActivity.this)
                .setTitle(R.string.backup_and_restore)
                .setIcon(R.mipmap.ic_cloud_upload_white_48dp)
                .setMessage(R.string.dialog_restore_success_message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        if(!builder.create().isShowing()) builder.create().show();
    }

    private void showLogin(){
        Intent loginIntent = new Intent(getApplicationContext(), SigninActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void checkBackup(final String user_id){

        if(mPreference.isUserLoggedIn()){

            mFirebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        if(ds.getKey().equals(user_id)){
                            Log.i(TAG,"backup exists: "+ds.getKey());
                            confirmRestore(user_id);
                            break;
                        }
                        else{
                            Log.i(TAG,"backup does not exist: "+user_id+"/"+ds.getKey());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    private void hideProgress(){
        mProgressBar.setVisibility(View.GONE);
    }
    private void showProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
