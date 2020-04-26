package com.hriportfolio.dryve.Customer;

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

public class CustomerLoginActivity extends AppCompatActivity {

    @BindView(R.id.customer_login_email)
    EditText customerLoginEmail;
    @BindView(R.id.customer_login_password)
    EditText customerLoginPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        ButterKnife.bind(this);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
    }
    @OnClick(R.id.customer_login_button)
    void customer_login_click(){
        String email = customerLoginEmail.getText().toString();
        String password = customerLoginPassword.getText().toString();

        signInCustomer(email,password);
    }
    @OnClick(R.id.customer_register_button)
    void customer_register_click(){
        Intent customerRegIntent = new Intent(CustomerLoginActivity.this, CustomerRegisterActivity.class);
        startActivity(customerRegIntent);
    }

    private void signInCustomer(String email,String password){
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please wait while we're authenticating your account!");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loadingBar.dismiss();
                                Intent customerLoginIntent = new Intent(CustomerLoginActivity.this,
                                        CustomerMapActivity.class);
                                startActivity(customerLoginIntent);
                            } else {
                                Log.d("driverLoginProblem",task.getException().toString());
                                Toast.makeText(CustomerLoginActivity.this,
                                        "Driver Sign in Failed!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }
}
