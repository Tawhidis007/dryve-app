package com.hriportfolio.dryve;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

    private String getType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        Log.d("Hello","Master branch updated");

        getType = getIntent().getStringExtra("type");
        if(getType.equals("Drivers")){
            carName.setVisibility(View.VISIBLE);
        }


    }

    @OnClick(R.id.setting_back_button)
    void backButtonpressed(){
        super.onBackPressed();
    }

    @OnClick(R.id.setting_alright_button)
    void alrightButtonPressed(){

    }
}
