package com.hriportfolio.dryve;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hriportfolio.dryve.Customer.CustomerLoginActivity;
import com.hriportfolio.dryve.Driver.DriverLoginActivity;

public class WelcomeActivity extends AppCompatActivity {

    CardView cardView,cardView2;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
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
