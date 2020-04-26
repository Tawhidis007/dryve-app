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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hriportfolio.dryve.R;

public class CustomerRegisterActivity extends AppCompatActivity {

    @BindView(R.id.customer_reg_email)
    EditText customerRegEmail;
    @BindView(R.id.cusotmer_reg_password)
    EditText customerRegPassword;

    private String onlineCustomerId;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference customerDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
    }

    @OnClick(R.id.customer_complete_reg_button)
    void completeCustomerRegistration() {
        String customerEmail = customerRegEmail.getText().toString();
        String customerPassword = customerRegPassword.getText().toString();

        registerCustomer(customerEmail, customerPassword);
    }

    private void registerCustomer(String customerEmail, String customerPassword) {
        if (TextUtils.isEmpty(customerEmail)) {
            Toast.makeText(CustomerRegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(customerPassword)) {
            Toast.makeText(CustomerRegisterActivity.this,
                    "Please fill out all the fields", Toast.LENGTH_SHORT).show();
        }
        if(customerPassword.length()<6){
            Toast.makeText(CustomerRegisterActivity.this,
                    "Length should be at least 6!", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Please wait while we register your account!");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(customerEmail, customerPassword).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onlineCustomerId = mAuth.getCurrentUser().getUid();
                                customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                                        .child(onlineCustomerId);
                                customerDatabaseRef.setValue(true);

                                Toast.makeText(CustomerRegisterActivity.this,
                                        "Customer Registration complete!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent i = new Intent(CustomerRegisterActivity.this,CustomerLoginActivity.class);
                                startActivity(i);
                            } else {
                                Log.d("customerRegProblem",task.getException().toString());
                                Toast.makeText(CustomerRegisterActivity.this,
                                        "Customer Registration Failed!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}
