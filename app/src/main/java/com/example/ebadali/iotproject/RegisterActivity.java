package com.example.ebadali.iotproject;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    Toolbar registerToolbar;

    TextInputLayout emailEditText, passwordEditText,nameEditText;
    Button registerButton;
    String email, password,name;


    MaterialDialog dialogs;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerToolbar = (Toolbar) findViewById(R.id.signInToolbarRegister);
        setSupportActionBar(registerToolbar);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = (TextInputLayout) findViewById(R.id.input_layout_email);
        passwordEditText = (TextInputLayout) findViewById(R.id.input_layout_password);
        nameEditText = (TextInputLayout)findViewById(R.id.input_layout_name);


        registerButton = (Button) findViewById(R.id.register_button);

        registerButton.setOnClickListener(registerButtonListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Register");

    }

    View.OnClickListener registerButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();

            emailEditText.setErrorEnabled(false);
            passwordEditText.setErrorEnabled(false);
            nameEditText.setErrorEnabled(false);

            email = emailEditText.getEditText().getText().toString();
            password = passwordEditText.getEditText().getText().toString();
            name = nameEditText.getEditText().getText().toString();

            if (email.isEmpty()) {
                emailEditText.setError("Please enter username!");
            }

            else if (password.isEmpty()) {
                passwordEditText.setError("Please enter password!");

            }
            else if (name.isEmpty()) {
                nameEditText.setError("Please enter name!");

            }


            else {
                    emailEditText.setErrorEnabled(false);
                    passwordEditText.setErrorEnabled(false);
                    nameEditText.setErrorEnabled(false);

                    if(isOnline())
                    {
                        callDialog();
                        doSignup(email,password);
                    }
                    else {
                        showSnackBar();
                    }
                }

        }
    };

    private void doSignup(String emailText,String passwordText) {

        mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialogs.dismiss();

                Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                //hideProgressDialog();

                if (task.isSuccessful()) {

                    startService(new Intent(RegisterActivity.this, EmergencyService.class));
                    startActivity(new Intent(RegisterActivity.this, BlinkLogoActivity.class));
                    finish();
                }
                else {
                    Log.wtf(TAG, task.getException());
                    Toast.makeText(RegisterActivity.this, "Sign Up Failed",
                            Toast.LENGTH_SHORT).show();

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        emailEditText.setError(getString(R.string.error_invalid_email));
                    } catch (FirebaseAuthUserCollisionException e) {
                        emailEditText.setError(getString(R.string.error_email_exist));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void callDialog() {
        final MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .content("Creating account...")
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
