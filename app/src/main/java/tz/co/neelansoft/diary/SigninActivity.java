package tz.co.neelansoft.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import tz.co.neelansoft.Utils.EmailValidationUtil;

/**
 * Created by landre on 26/06/2018.
 */

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";
    private Button mSignInWithEmail;
    private TextView mDisplayError;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private CheckBox mCheckBox;
    private ProgressBar mProgressBar;
    private final int SIGN_IN_REQUEST_CODE = 1;
    private DiaryPreferenceUtils mDiaryPreferenceUtils;
    private FirebaseAuth mFirebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        //initialize preference utils
        mDiaryPreferenceUtils = new DiaryPreferenceUtils(this);
        //initialize firbase auth
        mFirebaseAuth = FirebaseAuth.getInstance();


        mProgressBar          = findViewById(R.id.progressBarSignIn);
        mDisplayError       = findViewById(R.id.tvSignInError);

        Button mSignup = findViewById(R.id.btnSignupWithEmailAndPassword);
        //user has not logged in
        if(!mDiaryPreferenceUtils.isUserLoggedIn()){
            SignInButton mGoogleSignInButton = findViewById(R.id.btnGoogleSignIn);
            mSignInWithEmail    = findViewById(R.id.btnSignInWithEmailAndPassword);

            mEditTextEmail      = findViewById(R.id.etSignInEmail);
            mEditTextPassword   = findViewById(R.id.etSignInPassword);

            mCheckBox           = findViewById(R.id.checkBox);


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

            mEditTextEmail.addTextChangedListener(new TextWatcher() {
                final EmailValidationUtil emailValidator = new EmailValidationUtil();
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(emailValidator.isValid(mEditTextEmail.getText().toString())){
                        showEmailError();
                    }
                    else{
                        enableSignin();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(emailValidator.isValid(mEditTextEmail.getText().toString())){
                        showEmailError();
                    }
                    else{
                        enableSignin();
                    }
                }
            });



            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        mEditTextPassword.setTransformationMethod(null);
                        mCheckBox.setText(getResources().getString(R.string.hide));
                    }
                    else{
                        mEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                        mCheckBox.setText(getResources().getString(R.string.show));


                    }
                }
            });
        }
        //user already logged in
        else{
            //retrieve logged in account

                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                updateUI(user);

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
            GoogleSignInAccount account = task.getResult(ApiException.class);

            mProgressBar.setVisibility(View.GONE);
            if(account != null){
                firebaseAuthWithGoogle(account);
            }
            else{
                showSignInError(getResources().getString(R.string.login_error_msg));
               // Toast.makeText(SigninActivity.this,getResources().getString(R.string.login_error_msg), Toast.LENGTH_SHORT).show();
            }
        }
        catch(ApiException e){
            e.printStackTrace();
            String  msg = e.getLocalizedMessage();
            Toast.makeText(SigninActivity.this,msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void signinWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Config.WEB_CLIENT_ID)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SigninActivity.this,gso);

        Intent googleSigninIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(googleSigninIntent,SIGN_IN_REQUEST_CODE);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleAccount){
        showProgress();
        AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(),null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            hideProgress();

                            updateUI(mFirebaseAuth.getCurrentUser());
                        }
                        else{
                            String error = "An error occured";
                            if(task.getException().getMessage() != null){
                                error = task.getException().getMessage();
                            }
                            Log.e(TAG,error,task.getException());
                            showSignInError(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    private void singinWithEmailAndPassword(){
        showProgress();
        if(!TextUtils.isEmpty(mEditTextEmail.getText().toString()) && !TextUtils.isEmpty(mEditTextPassword.getText().toString())){
            String email    = mEditTextEmail.getText().toString();
            String password = mEditTextPassword.getText().toString();
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                hideProgress();
                                FirebaseUser user = task.getResult().getUser();
                                updateUI(user);
                            }
                            else{
                                hideProgress();
                                Log.e(TAG, "Signin with Email and Password failed",task.getException());
                                showSignInError(getResources().getString(R.string.signin_error));
                            }
                        }
                    });
        }
        else{
            hideProgress();
            showSignInError("Password cannot be empty");
        }
    }

    private void goSignup(){
        //call signup page
        Intent signupPageIntent = new Intent(getApplicationContext(), SignupActivity.class);
       // new Intent()
        startActivity(signupPageIntent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // mFirebaseAuth.
    }

    private void updateUI(FirebaseUser user){
        mDisplayError.setText(getResources().getString(R.string.signin_success));
        mDiaryPreferenceUtils.setUser(user);
        startActivity(new Intent(SigninActivity.this,JournalEntriesActivity.class));
        finish();
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            updateUI(user);
        }
    }


    private void showEmailError(){
        mEditTextEmail.setError("Invalid email");
        mEditTextEmail.setTextColor(getResources().getColor(R.color.color_error));
        mSignInWithEmail.setTextColor(getResources().getColor(R.color.black));
        mSignInWithEmail.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
        mSignInWithEmail.setEnabled(false);
    }

    private void enableSignin(){

        mEditTextEmail.setTextColor(getResources().getColor(R.color.colorPrimary));
        mSignInWithEmail.setTextColor(getResources().getColor(R.color.white));
        mSignInWithEmail.setBackgroundResource(R.drawable.button_background);

        mSignInWithEmail.setEnabled(true);
    }

    private void hideProgress(){
        mProgressBar.setVisibility(View.GONE);
    }
    private void showProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showSignInError(String error){
        mDisplayError.setText(error);
    }

}
