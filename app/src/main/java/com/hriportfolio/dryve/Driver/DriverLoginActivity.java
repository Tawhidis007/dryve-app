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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hriportfolio.dryve.R;

public class DriverLoginActivity extends AppCompatActivity {

    @BindView(R.id.driver_login_email)
    EditText driverLoginEmail;
    @BindView(R.id.driver_login_password)
    EditText driverLoginPassword;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        ButterKnife.bind(this);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

    }

    @OnClick(R.id.driver_login_button)
    void driver_login_click() {
        String email = driverLoginEmail.getText().toString();
        String password = driverLoginPassword.getText().toString();

        signInDriver(email, password);
    }

    @OnClick(R.id.driver_register_button)
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
            loadingBar.setTitle("Driver lOGIN");
            loadingBar.setMessage("Please wait while we're authenticating your account!");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loadingBar.dismiss();
                                Intent driverLoginIntent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                                startActivity(driverLoginIntent);
                            } else {
                                Log.d("driverLoginProblem", task.getException().toString());
                                Toast.makeText(DriverLoginActivity.this,
                                        "Driver Sign in Failed!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}
