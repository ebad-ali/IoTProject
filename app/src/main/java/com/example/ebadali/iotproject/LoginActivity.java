package com.example.ebadali.iotproject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_LOCATION_PERM = 122;


    Toolbar loginToolbar;

    TextInputLayout usernameEditText, passwordEditText;
    Button loginButton;
    Button forgotPassword;
    String username, password;

    private FirebaseAuth mAuth;

    MaterialDialog dialogs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginToolbar = (Toolbar) findViewById(R.id.logInToolbarRegister);
        setSupportActionBar(loginToolbar);
        setTitle(R.string.app_name);

        mAuth = FirebaseAuth.getInstance();

        usernameEditText = (TextInputLayout) findViewById(R.id.input_layout_username);
        passwordEditText = (TextInputLayout) findViewById(R.id.input_layout_password);

        loginButton = (Button) findViewById(R.id.login_button);

        forgotPassword = (Button) findViewById(R.id.forgotPassword);


        loginButton.setOnClickListener(loginButtonListener);
        forgotPassword.setOnClickListener(forgotPasswordListener);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Log In");
    }

    View.OnClickListener loginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();
            usernameEditText.setErrorEnabled(false);
            passwordEditText.setErrorEnabled(false);

            username = usernameEditText.getEditText().getText().toString();
            password = passwordEditText.getEditText().getText().toString();

            if (username.isEmpty()) {
                usernameEditText.setError("Please enter username!");
            } else if (password.isEmpty()) {
                passwordEditText.setError("Please enter password!");
            } else {

                usernameEditText.setErrorEnabled(false);
                passwordEditText.setErrorEnabled(false);

                if (isOnline()) {
                    callProgressDialog();
                    doLogin(username, password);
                } else {
                    showSnackBar();
                }
            }
        }

    };


    void doLogin(String emails, String passwords) {
        mAuth.signInWithEmailAndPassword(emails, passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialogs.dismiss();
                if (task.isSuccessful()) {
                    startService(new Intent(LoginActivity.this, EmergencyService.class));
                    startActivity(new Intent(LoginActivity.this, BlinkLogoActivity.class));
                    finish();

                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private View.OnClickListener forgotPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isOnline()) {
                new MaterialDialog.Builder(LoginActivity.this)
                        .title("Forgot password")
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .positiveText("Submit")
                        .negativeText("Cancel")
                        .cancelable(false)
                        .input("Enter your email", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                if (input.length() > 0) {
                                    forgotpasswordcaller("" + input);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).show();

            } else {
                showSnackBar();
            }
        }

    };


    private void forgotpasswordcaller(String email) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new MaterialDialog.Builder(LoginActivity.this)
                                    .content("Email containing Password Reset Link has been sent to your email.")
                                    .positiveText("OK")
                                    .show();

                        } else {
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                e.printStackTrace();
                            }

                        }
                    }
                });
    }


    private void callProgressDialog() {
        final MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .content(R.string.login)
                        .progress(true, 0)
                        .cancelable(false)
                        .show();

        dialogs = dialog;
    }


    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), "No internet connection.", Snackbar.LENGTH_LONG);


        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        snackbar.show();

    }


}

