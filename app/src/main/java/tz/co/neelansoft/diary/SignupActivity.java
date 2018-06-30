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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import tz.co.neelansoft.Utils.DiaryPreferenceUtils;
import tz.co.neelansoft.Utils.EmailValidationUtil;

/**
 * Created by landre on 26/06/2018.
 */

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private CheckBox mCheckBox;
    private ProgressBar mProgressBar;

    private TextView mSigninButton;
    private TextView mErrorDisplay;

    private Button mSubmitButton;

    private FirebaseAuth mFirebaseAuth;
    private DiaryPreferenceUtils mDiaryPreferenceUtil;

    private boolean mEmailOk;
    private boolean mPasswordOk;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEditTextName     = findViewById(R.id.etName);
        mEditTextEmail    = findViewById(R.id.etEmail);
        mEditTextPassword = findViewById(R.id.etPassword);
        mCheckBox         = findViewById(R.id.checkBox);

        mProgressBar      = findViewById(R.id.progressBar);
        mErrorDisplay     = findViewById(R.id.tvError);

        mSubmitButton     = findViewById(R.id.btnSubmit);
        mSigninButton     = findViewById(R.id.tvSignin);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDiaryPreferenceUtil = new DiaryPreferenceUtils(this);

        //create user
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
        //add text change listener for email validation
        mEditTextEmail.addTextChangedListener(new TextWatcher() {
            final EmailValidationUtil emailValidator = new EmailValidationUtil();
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!emailValidator.validate(mEditTextEmail.getText().toString())){
                    showEmailError();
                }
                else{
                    mEmailOk = true;
                    enableSignup();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!emailValidator.validate(mEditTextEmail.getText().toString())){
                    showEmailError();
                }
                else{
                    mEmailOk = true;
                    enableSignup();
                }
            }
        });

        //add text change listener for password validation
        mEditTextPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //password length not below 8 characters
                if(mEditTextPassword.getText().length() < 8){
                    showEmailError();
                }
                else{
                    mPasswordOk = true;
                    enableSignup();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //password length not below 8 characters
                if(mEditTextPassword.getText().length() < 8){
                    showPasswordError();
                }
                else{
                    mPasswordOk = true;
                    enableSignup();
                }
            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mEditTextPassword.setTransformationMethod(null);
                    //mEditTextPassword.setSelection(mEditTextEmail.getText().length() -1);
                }
                else{
                    mEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                    //mEditTextPassword.setSelection(mEditTextEmail.getText().length() -1);

                }
            }
        });

        mSigninButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showLogin();
            }
        });

    }

    private void showLogin() {
        startActivity(new Intent(getApplicationContext(), SigninActivity.class));
        finish();
    }

    private void createUser(){
        mProgressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(mEditTextEmail.getText()) && !TextUtils.isEmpty(mEditTextPassword.getText())){
            String email = mEditTextEmail.getText().toString();
            String password = mEditTextPassword.getText().toString();
            mFirebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                final FirebaseUser newUser = mFirebaseAuth.getCurrentUser();
                                if(!TextUtils.isEmpty(mEditTextName.getText())){
                                    String displayName = mEditTextName.getText().toString();
                                    UserProfileChangeRequest updateUserProfile = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName)
                                            .build();
                                    newUser.updateProfile(updateUserProfile)
                                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        FirebaseUser updatedUser = mFirebaseAuth.getCurrentUser();
                                                        mProgressBar.setVisibility(View.GONE);
                                                        updateUI(updatedUser);
                                                    }
                                                    else{
                                                        Log.e(TAG,"Could not update profile", task.getException());
                                                        Toast.makeText(SignupActivity.this, "Display name not updated", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                }
                                else{
                                    mProgressBar.setVisibility(View.GONE);
                                    updateUI(newUser);
                                }
                            }
                            else{
                                Log.e(TAG,"Could not create user", task.getException());
                                mErrorDisplay.setText("An error occurred while creating account");
                                return;

                            }
                        }
                    });
        }
    }

    private void showPasswordError(){
        mEditTextPassword.setError("Password too short");
        mEditTextPassword.setTextColor(getResources().getColor(R.color.color_error));
        mSubmitButton.setTextColor(getResources().getColor(R.color.black));
        mSubmitButton.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
        mSubmitButton.setEnabled(false);
    }
    private void showEmailError(){
        mEditTextEmail.setError("Invalid email");
        mEditTextEmail.setTextColor(getResources().getColor(R.color.color_error));
        mSubmitButton.setTextColor(getResources().getColor(R.color.black));
        mSubmitButton.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
        mSubmitButton.setEnabled(false);
    }

    private void enableSignup(){

        if(mEmailOk){
            mEditTextEmail.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        if(mPasswordOk){

            mEditTextPassword.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        if(mEmailOk && mPasswordOk) {
            mSubmitButton.setTextColor(getResources().getColor(R.color.white));
            mSubmitButton.setBackgroundResource(R.drawable.button_background);
            mSubmitButton.setEnabled(true);
        }
    }

    private void updateUI(FirebaseUser user){
        mDiaryPreferenceUtil.setUser(user);
        startActivity(new Intent(SignupActivity.this,JournalEntriesActivity.class));
        finish();
    }
}
