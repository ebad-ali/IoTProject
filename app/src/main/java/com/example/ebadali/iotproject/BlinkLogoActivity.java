package com.example.ebadali.iotproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;

public class BlinkLogoActivity extends AppCompatActivity {

    CardView layoutSmoke, layoutFire, layoutCigarette;
    static ImageView imageViewSteam, imageViewFire, imageViewCigarette;

    static BlinkLogoActivity activityLogo;

    private FirebaseAuth mAuth;
    Toolbar loginToolbar;


    static boolean  cigaretteBool = false, steamBool = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blink_logo);


        if(!isMyServiceRunning(EmergencyService.class))
        {
            startService(new Intent(BlinkLogoActivity.this, EmergencyService.class));
        }
        else {
            Log.e("service result","its running");
        }

        loginToolbar = (Toolbar) findViewById(R.id.mainToolbarRegister);
        setSupportActionBar(loginToolbar);
        setTitle(R.string.app_name);

        activityLogo = this;

        mAuth = FirebaseAuth.getInstance();


        //   setTitle("Emergency Alert System");


        layoutFire = (CardView) findViewById(R.id.cv);
        layoutCigarette = (CardView) findViewById(R.id.cvc);
        layoutSmoke = (CardView) findViewById(R.id.cvs);


        imageViewFire = (ImageView) findViewById(R.id.logo_fire);
        imageViewCigarette = (ImageView) findViewById(R.id.logo_cigarette);
        imageViewSteam = (ImageView) findViewById(R.id.logo_steam);


        layoutFire.setOnClickListener(fireListener);
        layoutCigarette.setOnClickListener(cigaretteListener);
        layoutSmoke.setOnClickListener(steamListener);


        onNewIntent(getIntent());


    }


    View.OnClickListener fireListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    View.OnClickListener cigaretteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            if (cigaretteBool) {
                imageViewCigarette.clearAnimation();
                callDialog("Smoke Emergency!", R.drawable.ciga);
                cigaretteBool = false;
            }


        }
    };

    View.OnClickListener steamListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (steamBool) {
                imageViewSteam.clearAnimation();
                callDialog("Steam Emergency!", R.drawable.steamsa);
                steamBool = false;
            }
        }
    };


    void callDialog(String title, int resID) {


        new MaterialDialog.Builder(BlinkLogoActivity.this)
                .title(title)
                .positiveText("IGNORE")
                .cancelable(false)
                .iconRes(resID)

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })

                .show();

    }


    public static void blinkLogoServiceReciever(String emergencyCode) {


        if (emergencyCode.equals("Only Smoke")) {
            callAnimation(imageViewCigarette);
            cigaretteBool = true;
        }

        else if (emergencyCode.equals("Fire Alarm")) {
            callAnimation(imageViewFire);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageViewFire.clearAnimation();
                    activityLogo.callSnackBar("Fire Notification has been sent.");
                }
            }, 7000);

        }

        else if (emergencyCode.equals("Kitchen Smoke")) {
            callAnimation(imageViewSteam);
            steamBool = true;

        }
    }

    public void blinkLogoServiceRecievere(String emergencyCode) {

        intializeViews();

        if (emergencyCode.equals("Only Smoke")) {
            callAnimation(imageViewCigarette);
            cigaretteBool = true;
        }

        else if (emergencyCode.equals("Fire Alarm")) {
            callAnimation(imageViewFire);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageViewFire.clearAnimation();
                    callSnackBar("Fire Notification has been sent.");

                }
            }, 7000);
        }

        else if (emergencyCode.equals("Kitchen Smoke")) {
            callAnimation(imageViewSteam);
            steamBool = true;
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.e("onNewIntent", "Im here");

        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.containsKey("emergencyValue")) {
                setContentView(R.layout.activity_blink_logo);
                // extract the extra-data in the Notification
                String msg = extras.getString("emergencyValue");
                blinkLogoServiceRecievere(msg);
                Log.e("emergencyValue", msg);
            }
        }
    }

    static void callAnimation(View view) {
        Log.e("Fucking imageViewFire", "" + "not null");

        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);

    }


    void callSnackBar(String text) {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG);


        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        snackbar.show();
    }

    void intializeViews() {

        layoutFire = (CardView) findViewById(R.id.cv);
        layoutCigarette = (CardView) findViewById(R.id.cvc);
        layoutSmoke = (CardView) findViewById(R.id.cvs);


        imageViewFire = (ImageView) findViewById(R.id.logo_fire);
        imageViewCigarette = (ImageView) findViewById(R.id.logo_cigarette);
        imageViewSteam = (ImageView) findViewById(R.id.logo_steam);


        layoutFire.setOnClickListener(fireListener);
        layoutCigarette.setOnClickListener(cigaretteListener);
        layoutSmoke.setOnClickListener(steamListener);

        loginToolbar = (Toolbar) findViewById(R.id.mainToolbarRegister);
        setSupportActionBar(loginToolbar);
        setTitle("Emergency Alert System");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {

            Intent myService = new Intent(BlinkLogoActivity.this, EmergencyService.class);
            stopService(myService);

            mAuth.signOut();
            startActivity(new Intent(this, GetStartedActivity.class));
            finish();
            //startActivity(new Intent(this, MainChatActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
            // finish activity
        } else {
            Toast.makeText(getApplicationContext(), "Press Back again to leave.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
