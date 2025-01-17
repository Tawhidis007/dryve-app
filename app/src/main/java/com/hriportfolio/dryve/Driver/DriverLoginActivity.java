package com.hriportfolio.dryve.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hriportfolio.dryve.R;
import com.hriportfolio.dryve.Utilities.CodeForTimeSaving;
import com.hriportfolio.dryve.Utilities.KeyString;
import com.hriportfolio.dryve.Utilities.SharedPreferenceManager;
import com.hriportfolio.dryve.WelcomeActivity;

public class DriverLoginActivity extends AppCompatActivity {

    @BindView(R.id.driver_login_email)
    EditText driverLoginEmail;
    @BindView(R.id.driver_login_password)
    EditText driverLoginPassword;
    @BindView(R.id.driver_register_button)
    TextView driverRegisterButton;

    SharedPreferenceManager preferenceManager;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_login);
        ButterKnife.bind(this);
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        mAuth = FirebaseAuth.getInstance();

        driverRegisterButton.setOnClickListener(view -> driver_register_click());
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(DriverLoginActivity.this, WelcomeActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.driver_login_button)
    void driver_login_click() {
        String email = driverLoginEmail.getText().toString();
        String password = driverLoginPassword.getText().toString();

        signInDriver(email, password);
    }

    void driver_register_click() {
        Intent driverRegIntent = new Intent(DriverLoginActivity.this, DriverRegisterActivity.class);
        startActivity(driverRegIntent);
    }

    private void signInDriver(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        } else {
//            loadingBar.setTitle("Driver Login");
//            loadingBar.setMessage("Please wait while we're authenticating your account!");
            loadingBar = CodeForTimeSaving.createProgressDialog(this);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            preferenceManager.setValue(KeyString.SIGN_IN_FLAG,true);
                            preferenceManager.setValue(KeyString.DRIVER_MODE,true);
                            Intent driverLoginIntent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                            startActivity(driverLoginIntent);
                        } else {
                            Log.d("driverLoginProblem", task.getException().toString());
                            Toast.makeText(DriverLoginActivity.this,
                                    "Driver Sign in Failed!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }
}
