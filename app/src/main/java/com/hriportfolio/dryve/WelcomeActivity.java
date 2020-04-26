package com.hriportfolio.dryve;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.hriportfolio.dryve.Customer.CustomerLoginActivity;
import com.hriportfolio.dryve.Driver.DriverLoginActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.customer_button)
    void customer_button_click(){
        Intent customerIntent = new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
        startActivity(customerIntent);
        Log.d("Hello","d");
        Log.d("Hello","d");
        Log.d("Hello","d");
        Log.d("Hello","d");
        Log.d("Hello","d");
    }
    @OnClick(R.id.driver_button)
    void driver_button_click(){
        Intent driverIntent = new Intent(WelcomeActivity.this, DriverLoginActivity.class);
        startActivity(driverIntent);
    }
}
