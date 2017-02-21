package com.example.michael.fitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
/**
 * A sign up screen that creates an account stored in sharedPreferences
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mFirstName;
    private EditText mLastName;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = (EditText)findViewById(R.id.emailText);
        mPassword = (EditText)findViewById(R.id.passwordText);
        mFirstName = (EditText)findViewById(R.id.firstNameText);
        mLastName = (EditText)findViewById(R.id.lastNameText);
        mRegister = (Button)findViewById(R.id.registerButton);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String name = mFirstName.getText().toString() + " " + mLastName.getText().toString();

                //save account into sharedPReferences
                SharedPreferences sharedPref = SignUpActivity.this.getSharedPreferences(email, SignUpActivity.this.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("password", password);
                editor.putString("name", name);
                editor.putInt("totalDistance", 0);
                editor.putInt("dailyDistance", 0);
                editor.putInt("milestone", 1000);
                editor.clear();
                editor.commit();

                //go back to login screen
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
