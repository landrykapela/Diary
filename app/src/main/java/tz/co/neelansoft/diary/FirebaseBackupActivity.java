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
    private ImageView mImageCloud;
    private Button mButtonCancel;
    private Button mButtonRestore;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firebase_backup);

        mProgressBar   = findViewById(R.id.progressBar);
        mImageCloud    = findViewById(R.id.ivCloudBuckup);
        mButtonCancel  = findViewById(R.id.cancel_backup_button);
        mButtonRestore = findViewById(R.id.restore_backup_button);

        mPreference = new DiaryPreferenceUtils(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_DB_REFERENCE);
        mDatabase = DiaryDatabase.getDatabaseInstance(this);



        mButtonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        mImageCloud.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                performBackup();
            }
        });
        mButtonRestore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                confirmRestore();
            }
        });
    }

    private void performBackup(){
        //check if user is logged in to app
        if(mPreference.isUserLoggedIn()){
            //check if user exist
            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
            if(firebaseUser == null){
                requireLogin();
            }
            else{
                showProgress();
                final String userid = firebaseUser.getUid();
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
        }
    }

    private void confirmRestore(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseBackupActivity.this)
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.dialog_restore_confirmation)
                .setPositiveButton(R.string.restore, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which){
                        restoreData();
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

    private void restoreData(){
        mFirebaseReference.child(mFirebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                .setTitle(R.string.backup_to_cloud)
                .setIcon(R.mipmap.ic_cloud_upload_white_48dp)
                .setMessage(R.string.dialog_backup_success_message)
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


    private void hideProgress(){
        mProgressBar.setVisibility(View.GONE);
    }
    private void showProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
