package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            transitionToActivity();
            //ParseUser.logOut();
        }

        btnSignUpLogin = findViewById(R.id.btn_signup);
        btnSignUpLogin.setOnClickListener(this);
        btnOneTimeLogin = findViewById(R.id.btn_onetime);
        btnOneTimeLogin.setOnClickListener(this);

        passengerRadioButton = findViewById(R.id.rd_btn_psg);
        driverRadioButton = findViewById(R.id.rd_btn_drv);

        edtUserName = findViewById(R.id.ed_username);
        edtPassword = findViewById(R.id.ed_password);
        edtDriverOrPassenger = findViewById(R.id.ed_D_P);

        state = State.SIGNUP;

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signup: {
                if (state == State.SIGNUP) {
                    if (!driverRadioButton.isChecked() && !passengerRadioButton.isChecked()) {
                        Toast.makeText(this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")) {
                        Toast.makeText(this, "Please fill the blanks area", Toast.LENGTH_SHORT).show();
                    }

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRadioButton.isChecked())
                        appUser.put("as", "Driver");
                    else if (passengerRadioButton.isChecked())
                        appUser.put("as", "Passenger");



                    /**
                     * driver veya passenger olarak login yapılan kullanıcının farklı olan driver/pasng olarak giriş yapması engellenmeli değil mi?
                      */
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT).show();
                                transitionToActivity();
                            }
                        }
                    });
                } else if (state == State.LOGIN) {
                    if (edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")) {
                        Toast.makeText(this, "Please fill the blanks area", Toast.LENGTH_SHORT).show();
                    }

                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                                transitionToActivity();
                            }
                        }
                    });
                }
                break;
            }
            case R.id.btn_onetime: {
                if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {
                    if (ParseUser.getCurrentUser() == null) {
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null && e == null) {
                                    Toast.makeText(MainActivity.this, "We have an Anonymous User", Toast.LENGTH_SHORT).show();

                                    user.put("as", edtDriverOrPassenger.getText().toString());
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            transitionToActivity();
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                break;
            }
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
                    driverRadioButton.setEnabled(false);
                    passengerRadioButton.setEnabled(false);

                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                    driverRadioButton.setEnabled(true);
                    passengerRadioButton.setEnabled(true);
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                startActivity(new Intent(MainActivity.this, PassengerActivity_Map.class));
            }
            else if (ParseUser.getCurrentUser().get("as").equals("Driver")) {
                startActivity(new Intent(MainActivity.this, DriverRequestListActivity.class));
            }
        }
    }

    public void constraintKeyboard(View v) {
        try {
            InputMethodManager iMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            iMM.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}