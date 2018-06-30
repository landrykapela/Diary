package tz.co.neelansoft.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import tz.co.neelansoft.Utils.Config;
import tz.co.neelansoft.Utils.DiaryPreferenceUtils;

/**
 * Created by landre on 26/06/2018.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //form controls
    private SignInButton mGoogleSignInButton;
    private Button mSignInWithEmail;
    private TextView mSignup;
    private ProgressBar mProgressBar;
    private final int SIGN_IN_REQUEST_CODE = 1;
    private GoogleSignInAccount mAccount;
    private FirebaseAuth mFirebaseAuth;

    private DiaryPreferenceUtils mDiaryPreferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //initialize preference utils
        mDiaryPreferenceUtils = new DiaryPreferenceUtils(this);
        mProgressBar          = (ProgressBar) findViewById(R.id.progressBar);


        //initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser() != null && mDiaryPreferenceUtils.isUserLoggedIn()) {
            Log.e(TAG,"userid: "+mFirebaseAuth.getCurrentUser().getUid());
           startActivity(new Intent(MainActivity.this, JournalEntriesActivity.class));
           finish();
        }
        else {
                mGoogleSignInButton = (SignInButton) findViewById(R.id.btnGoogleSignIn);
                mSignInWithEmail = (Button) findViewById(R.id.btnSignInWithEmail);
                mSignup = (TextView) findViewById(R.id.tvSignup);

                //click event to buttons

                mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signinWithGoogle();
                    }
                });

                mSignInWithEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        singinWithEmailAndPassword();
                    }
                });

                mSignup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goSignup();
                    }
                });
            }

    }

    @Override
    protected void onStart(){
        super.onStart();

        if(mFirebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, JournalEntriesActivity.class));
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            mProgressBar.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == SIGN_IN_REQUEST_CODE) {

            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task){
        try{
            mAccount = task.getResult(ApiException.class);

            mProgressBar.setVisibility(View.GONE);
            if(mAccount != null){
                firebaseAuthWithGoogle(mAccount);
               // startActivity(new Intent(MainActivity.this,JournalEntriesActivity.class));
               // finish();
            }
            else{
                Toast.makeText(MainActivity.this,getResources().getString(R.string.login_error_msg), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        catch(ApiException e){
            e.printStackTrace();
            String  msg = e.getLocalizedMessage();
            Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleAccount){

        //show progress
        mProgressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            mDiaryPreferenceUtils.setUser(firebaseUser);
                            startActivity(new Intent(MainActivity.this, JournalEntriesActivity.class));
                            finish();
                        }
                        else{
                            task.getException().printStackTrace();
                            Log.e(TAG, task.getException().getMessage());
                            Toast.makeText(MainActivity.this,getResources().getString(R.string.login_error_msg), Toast.LENGTH_SHORT).show();
                        }
                        //hide progress bar
                        mProgressBar.setVisibility(View.GONE);
                    }
                });

    }
    private void signinWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Config.WEB_CLIENT_ID)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this,gso);

        Intent googleSigninIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(googleSigninIntent,SIGN_IN_REQUEST_CODE);
    }

    private void singinWithEmailAndPassword(){
        //call signin page
        Intent signinPageIntent = new Intent(getApplicationContext(), SigninActivity.class);
        startActivity(signinPageIntent);
    }

    private void goSignup(){
        //call signup page
        Intent signupPageIntent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(signupPageIntent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
       // mFirebaseAuth.
    }

}
