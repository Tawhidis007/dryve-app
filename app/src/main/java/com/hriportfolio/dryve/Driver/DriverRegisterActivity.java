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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hriportfolio.dryve.R;
import com.hriportfolio.dryve.Utilities.CodeForTimeSaving;

public class DriverRegisterActivity extends AppCompatActivity {

    @BindView(R.id.driver_reg_email)
    EditText driverRegEmail;
    @BindView(R.id.driver_reg_password)
    EditText driverRegPassword;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private String onlineDriverId;
    private DatabaseReference driverDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

    }

    @OnClick(R.id.driver_complete_reg_button)
    void completeDriverRegistration() {
        String driverEmail = driverRegEmail.getText().toString();
        String driverPassword = driverRegPassword.getText().toString();

        registerDriver(driverEmail, driverPassword);
    }

    private void registerDriver(String driverEmail, String driverPassword) {
        if (TextUtils.isEmpty(driverEmail)) {
            Toast.makeText(DriverRegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(driverPassword)) {
            Toast.makeText(DriverRegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (driverPassword.length() < 6) {
            Toast.makeText(DriverRegisterActivity.this,
                    "Length should be at least 6!", Toast.LENGTH_SHORT).show();
        } else {
           // loadingBar.setTitle("Driver Registration");
          //  loadingBar.setMessage("Please wait while we register your account!");
            loadingBar = CodeForTimeSaving.createProgressDialog(this);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(driverEmail, driverPassword).
                    addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            onlineDriverId = mAuth.getCurrentUser().getUid();
                            driverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                                    .child(onlineDriverId);
                            driverDatabaseRef.setValue(true);

                            Toast.makeText(DriverRegisterActivity.this,
                                    "Driver Registration complete!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Intent i = new Intent(DriverRegisterActivity.this, DriverLoginActivity.class);
                            startActivity(i);
                        } else {
                            Log.d("driverRegProblem",task.getException().toString());
                            Toast.makeText(DriverRegisterActivity.this,
                                    "Driver Registration Failed!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }
}
