package com.example.michael.fitness;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private Button mSignIn;
    private EditText mEmail;
    private EditText mPassword;
    private TextView mFailed;
    private Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignIn = (Button)findViewById(R.id.email_sign_in_button);
        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mFailed = (TextView)findViewById(R.id.loginFailed);
        mFailed.setVisibility(View.INVISIBLE);
        mSignUp = (Button) findViewById(R.id.signUpButton);

        //MUST CREATE AN ACCOUNT BEFORE SIGNING IN
        mSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Authentication
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                boolean verified = validate(email, password);
                //if login successful
                if (verified == true) {
                    //intent to main
                    logIn(email);
                } else {
                    mFailed.setVisibility(View.VISIBLE);
                }
            }
        });

        mSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Starts the Main Activity using the verified email
     * @param email of the user logging in
     */
    private void logIn(String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    /**
     * Checks to see if the email and password match
     * @param email input from user
     * @param password input from user
     * @return true if credentials match, false if not
     */
    private boolean validate(String email, String password) {
        SharedPreferences sharedPref = this.getSharedPreferences(email, this.MODE_PRIVATE);
        if (password.equals(sharedPref.getString("password", ""))) {
            return true;
        } else {
            return false;
        }
    }
}

