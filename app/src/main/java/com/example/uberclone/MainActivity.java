package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State {
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRAdioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPaseenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            //transition
            ParseUser.logOut();
        }

        btnSignUpLogin = findViewById(R.id.btn_signup);
        btnSignUpLogin.setOnClickListener(this);
        btnOneTimeLogin = findViewById(R.id.btn_onetime);
        btnOneTimeLogin.setOnClickListener(this);

        passengerRadioButton = findViewById(R.id.rd_btn_psg);
        driverRAdioButton = findViewById(R.id.rd_btn_drv);

        edtUserName = findViewById(R.id.ed_username);
        edtPassword = findViewById(R.id.ed_password);
        edtDriverOrPaseenger = findViewById(R.id.ed_D_P);

        state = State.SIGNUP;

        if (ParseUser.getCurrentUser() != null) {
            // Transition!

        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signup:
                if (state == State.SIGNUP) {
                    if (!driverRAdioButton.isChecked() && !passengerRadioButton.isChecked()) {
                        Toast.makeText(this, "Are yo a driver or a passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")) {
                        Toast.makeText(this, "Please fill the blanks area", Toast.LENGTH_SHORT).show();
                    }

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRAdioButton.isChecked())
                        appUser.put("as", "Driver");
                    else if (passengerRadioButton.isChecked())
                        appUser.put("as", "Passenger");

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (state == State.LOGIN) {
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toast.makeText(MainActivity.this, "User Logged In", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                break;
            case R.id.btn_onetime:
                if (edtDriverOrPaseenger.getText().toString().equals("Driver") || edtDriverOrPaseenger.getText().toString().equals("Passenger")) {
                    if (ParseUser.getCurrentUser() == null) {
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null && e == null) {
                                    Toast.makeText(MainActivity.this, "We have an Anonymous Userf", Toast.LENGTH_SHORT).show();

                                    user.put("as", edtDriverOrPaseenger.getText().toString());
                                    user.saveInBackground();
                                }
                            }
                        });
                    }
                }
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.loginItem:
                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Log In");

                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }


}