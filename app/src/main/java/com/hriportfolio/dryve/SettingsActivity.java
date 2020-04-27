package com.hriportfolio.dryve;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hriportfolio.dryve.Customer.CustomerMapActivity;
import com.hriportfolio.dryve.Driver.DriverMapActivity;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.setting_phone)
    EditText phoneNumber;
    @BindView(R.id.setting_car)
    EditText carName;
    @BindView(R.id.setting_name)
    EditText name;
    @BindView(R.id.profile_image)
    CircleImageView profile_image;
    @BindView(R.id.setting_change_pic)
    TextView changePic;

    private String checker = "";
    Uri imageUri;
    private String url = "";
    private String getType;
    private StorageTask uploadTask;

    private StorageReference storageProPicRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        getType = getIntent().getStringExtra("type");
        if (getType.equals("Drivers")) {
            carName.setVisibility(View.VISIBLE);
        }
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        storageProPicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "clicked";
                CropImage.activity().setAspectRatio(1, 1).start(SettingsActivity.this);
            }
        });

        getUserInformation();


    }

    @OnClick(R.id.setting_back_button)
    void backButtonpressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.setting_alright_button)
    void alrightButtonPressed() {

        if (checker.equals("clicked")) {
            validateControllers();
        } else {
            validateAndSaveOnlyInfo();
        }
    }

    private void validateAndSaveOnlyInfo() {
        if (TextUtils.isEmpty(name.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a phone number", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(carName.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a car name", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name", name.getText().toString());
            userMap.put("phone", phoneNumber.getText().toString());

            if (getType.equals("Drivers")) {
                userMap.put("name", carName.getText().toString());
            }
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            sendBack();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profile_image.setImageURI(imageUri);
        } else {
            if (getType.equals("Drivers")) {
                startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this, CustomerMapActivity.class));
            }
            Toast.makeText(SettingsActivity.this, "Error! Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateControllers() {
        if (TextUtils.isEmpty(name.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a phone number", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(carName.getText().toString())) {
            Toast.makeText(SettingsActivity.this, "Please provide a car name", Toast.LENGTH_SHORT).show();
        } else if (checker.equals("clicked")) {
            uploadProPic();
        }
    }

    private void uploadProPic() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Account Information");
        progressDialog.setMessage("Please wait while we update your information");
        progressDialog.show();
        if (imageUri != null) {
            final StorageReference fileRef = storageProPicRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        url = downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", mAuth.getCurrentUser().getUid());
                        userMap.put("name", name.getText().toString());
                        userMap.put("phone", phoneNumber.getText().toString());
                        userMap.put("image", url);

                        if (getType.equals("Drivers")) {
                            userMap.put("car", carName.getText().toString());
                        }
                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        progressDialog.dismiss();

                        sendBack();
                    }
                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBack(){
        if (getType.equals("Drivers")) {
            Intent i =new Intent(SettingsActivity.this, DriverMapActivity.class);
            i.putExtra("name",name.getText().toString());
            startActivity(i);
        } else {
            Intent i =new Intent(SettingsActivity.this, CustomerMapActivity.class);
            i.putExtra("name",name.getText().toString());
            startActivity(i);
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String nm = dataSnapshot.child("name").getValue().toString();
                    String ph = dataSnapshot.child("phone").getValue().toString();

                    name.setText(nm);
                    phoneNumber.setText(ph);

                    if (getType.equals("Drivers")) {
                        String cr = dataSnapshot.child("car").getValue().toString();
                        carName.setText(cr);
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String img = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(img).into(profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
