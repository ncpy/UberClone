package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State {
        SIGNUP, LOGIN
    }

    DrawerLayout drawerLayout;
    NavigationView navView;
    ActionBarDrawerToggle actionBarDrawerToggle;

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
        }


        drawerLayout = findViewById(R.id.drawerlayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navView = findViewById(R.id.navView);
        navView.setItemIconTintList(null);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_send_us) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ncpyolcu@gmail.com"});
                    emailIntent.setType("text/html");
                    emailIntent.setPackage("com.google.android.gm");
                    startActivity(Intent.createChooser(emailIntent, "Send e-mail"));
                }
                else if (item.getItemId() == R.id.item_share) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, "Hi"); // (googlePlay)
                    startActivity(Intent.createChooser(share, "Share this !! "));
                }
                else if (item.getItemId() == R.id.item_rate) {
                    Toast.makeText(getApplicationContext(), "..to Google Play", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        btnSignUpLogin = findViewById(R.id.btn_signup);
        btnSignUpLogin.setOnClickListener(this);
        btnOneTimeLogin = findViewById(R.id.btn_onetime);
        btnOneTimeLogin.setOnClickListener(this);

        passengerRadioButton = findViewById(R.id.rd_btn_psg);
        driverRadioButton = findViewById(R.id.rd_btn_drv);

        edtUserName = findViewById(R.id.ed_username);
        edtPassword = findViewById(R.id.ed_password);
        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edtDriverOrPassenger = findViewById(R.id.ed_D_P);

        state = State.SIGNUP;
        btnSignUpLogin.setBackgroundColor(Color.GREEN);

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


                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT).show();
                                transitionToActivity();
                            }
                        }
                    });

                    if (!appUser.isAuthenticated()) {
                        Toast.makeText(this, "Try to Login!", Toast.LENGTH_SHORT).show();
                    }

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
                            } else {
                                Toast.makeText(MainActivity.this, "Try to Sign up!", Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == R.id.loginItem) {
            if (state == State.SIGNUP) {
                state = State.LOGIN;
                item.setTitle("Sign Up");
                btnSignUpLogin.setText("Log In");
                btnSignUpLogin.setBackgroundColor(Color.YELLOW);
                driverRadioButton.setEnabled(false);
                passengerRadioButton.setEnabled(false);

            } else if (state == State.LOGIN) {
                state = State.SIGNUP;
                item.setTitle("Log In");
                btnSignUpLogin.setText("Sign Up");
                btnSignUpLogin.setBackgroundColor(Color.GREEN);
                driverRadioButton.setEnabled(true);
                passengerRadioButton.setEnabled(true);
            }
        } else if (item.getItemId() == R.id.info_item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setMessage("Version: " + BuildConfig.VERSION_NAME);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            hideKeyboard();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (Objects.equals(ParseUser.getCurrentUser().get("as"), "Passenger")) {
                startActivity(new Intent(MainActivity.this, PassengerActivity_Map.class));
            }
            else if (Objects.equals(ParseUser.getCurrentUser().get("as"), "Driver")) {
                startActivity(new Intent(MainActivity.this, DriverRequestListActivity.class));
            }
        }
    }

    public void constraintKeyboard(View v) {
        hideKeyboard();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager iMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            iMM.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}