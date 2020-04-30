package com.hriportfolio.dryve;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hriportfolio.dryve.Customer.CustomerLoginActivity;
import com.hriportfolio.dryve.Customer.CustomerMapActivity;
import com.hriportfolio.dryve.Driver.DriverLoginActivity;
import com.hriportfolio.dryve.Driver.DriverMapActivity;
import com.hriportfolio.dryve.Utilities.CodeForTimeSaving;
import com.hriportfolio.dryve.Utilities.KeyString;
import com.hriportfolio.dryve.Utilities.SharedPreferenceManager;

public class WelcomeActivity extends AppCompatActivity {
    SharedPreferenceManager preferenceManager;
    CardView cardView,cardView2;
    boolean signedInFlag;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        initPref();

        cardView = findViewById(R.id.card_for_dryver);
        cardView2 = findViewById(R.id.card_for_customer);
        cardView.setOnTouchListener((view, motionEvent) -> {
            Intent driverIntent = new Intent(WelcomeActivity.this, DriverLoginActivity.class);
            startActivity(driverIntent);
            return true;
        });
        cardView2.setOnTouchListener((view, motionEvent) -> {
            Intent driverIntent = new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
            startActivity(driverIntent);
            return true;
        });

    }

    private void initPref(){
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
         signedInFlag = preferenceManager.getValue(KeyString.SIGN_IN_FLAG,false);
         Log.d("hririsignedinflag",String.valueOf(signedInFlag));
         boolean d = preferenceManager.getValue(KeyString.DRIVER_MODE,false);
         Log.d("hririDriMode",String.valueOf(d));
        Log.d("hririCusMode",String.valueOf(preferenceManager.getValue(KeyString.CUSTOMER_MODE,false)));

         if(signedInFlag){
             if(preferenceManager.getValue(KeyString.DRIVER_MODE,false)){
                 Intent driverIntent = new Intent(WelcomeActivity.this, DriverMapActivity.class);
                 startActivity(driverIntent);
             }else if(preferenceManager.getValue(KeyString.CUSTOMER_MODE,false)){
                 Intent customerIntent = new Intent(WelcomeActivity.this, CustomerMapActivity.class);
                 startActivity(customerIntent);
             }else{
             }
         }
    }

    @OnClick(R.id.customer_button)
    void customer_button_click(){
        Intent customerIntent = new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
        startActivity(customerIntent);

    }
    @OnClick(R.id.driver_button)
    void driver_button_click(){
        Intent driverIntent = new Intent(WelcomeActivity.this, DriverLoginActivity.class);
        startActivity(driverIntent);
    }

    @Override
    public void onBackPressed() {
    }
}
