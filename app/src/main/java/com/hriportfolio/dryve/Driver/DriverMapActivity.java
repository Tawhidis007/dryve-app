package com.hriportfolio.dryve.Driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.dryve.R;
import com.hriportfolio.dryve.SettingsActivity;
import com.hriportfolio.dryve.Utilities.KeyString;
import com.hriportfolio.dryve.Utilities.SharedPreferenceManager;
import com.hriportfolio.dryve.WelcomeActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    @BindView(R.id.driver_menu_button)
    ImageButton driver_menu_button;

    @BindView(R.id.driver_dummy_text)
    TextView driver_dummy_text;
    @BindView(R.id.name_for_driver_profile)
    TextView name_for_driver_profile;
    @BindView(R.id.pro_pic_for_driver_profile)
    CircleImageView pro_pic_for_driver_profile;


    @BindView(R.id.customer_name_text)
    TextView customer_name_text;
    @BindView(R.id.customer_destination_text)
    TextView customer_destination_text;
    @BindView(R.id.customer_found_card)
    CardView customer_found_card;
    @BindView(R.id.call_customer_iv)
    ImageView call_customer_iv;
    @BindView(R.id.customer_profile_image)
    CircleImageView customer_profile_image;

    private Button cancel_button;
    private Button accept_button;

    final static int REQUEST_CODE = 1;
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean driverLogoutStatus = false;
    private String driverId, customerId = "";
    private String naam = "";
    private String cusomerDestination = "";

    private DatabaseReference assignedCustomerRef;
    private DatabaseReference assignedCustomerPickUpRef;
    private DatabaseReference assignedCustomerDestinationRef;
    Marker pickUpMarker;
    private ValueEventListener assignedCustomerPickUpRefListener;

    SharedPreferenceManager preferenceManager;
    private String sp_name="";
    private String sp_phone="";
    private String sp_picUrl="";
    private String sp_car="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_map);
        ButterKnife.bind(this);
        cancel_button = findViewById(R.id.driver_cancel_button);
        accept_button = findViewById(R.id.driver_accept_button);
        initPref();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverId = mAuth.getCurrentUser().getUid();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        checkLocationPermission();

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        getAssignedCustomerRequest();

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void initPref() {
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        sp_name = preferenceManager.getValue(KeyString.NAME_DRIVER, "");
        sp_phone = preferenceManager.getValue(KeyString.PHONE_NUMBER_DRIVER, "");
        sp_picUrl = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL_DRIVER, "");
        sp_car = preferenceManager.getValue(KeyString.CAR_NAME, "");
        if (sp_name.equals("")) {
            redirectUserToSettings();
        } else {
            setupUserInfo();
        }
    }

    private void setupUserInfo() {
        if(!sp_picUrl.equals("")){
            Picasso.get().load(sp_picUrl).into(pro_pic_for_driver_profile);
        }
        name_for_driver_profile.setText(sp_name);
        driver_dummy_text.setText("Looking for customers..");
    }

    private void redirectUserToSettings() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Oops!");
        Button b = dialog.findViewById(R.id.navi_btn);
        b.setOnClickListener(view -> {
            Intent i = new Intent(DriverMapActivity.this, SettingsActivity.class);
            i.putExtra("type", "Drivers");
            startActivity(i);
        });
        dialog.show();
    }

    //retrieving the customerID for driver screen
    private void getAssignedCustomerRequest() {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverId).child("CustomerRideID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickUpLocation();

                    customer_found_card.setVisibility(View.VISIBLE);
                    driver_dummy_text.setText("Incoming Request!");
                    getAssignedCustomerInformation();
                } else {
                    customerId = "";
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
                    if (assignedCustomerPickUpRefListener != null) {
                        assignedCustomerPickUpRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }
                    customer_found_card.setVisibility(View.GONE);
                    driver_dummy_text.setText("Looking for customers..");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedCustomerPickUpLocation() {
        assignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests")
                .child(customerId).child("l");

        assignedCustomerPickUpRefListener = assignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double lat = 0;
                    double lng = 0;
                    if (customerLocationMap.get(0) != null) {
                        lat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) != null) {
                        lng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(lat, lng);
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Pick Up Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        assignedCustomerDestinationRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverId).child("CustomerDestination");
        assignedCustomerDestinationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    cusomerDestination = dataSnapshot.getValue().toString();

                } else {
                    cusomerDestination = "";
                    if (assignedCustomerPickUpRefListener != null) {
                        assignedCustomerPickUpRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.driver_menu_button)
    void menu_pressed() {
        PopupMenu popup = new PopupMenu(this, driver_menu_button);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.custommenu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_setting_button) {
                Intent i = new Intent(DriverMapActivity.this, SettingsActivity.class);
                i.putExtra("type", "Drivers");
                startActivity(i);
            }
            if (item.getItemId() == R.id.menu_logout_button) {
                driverLogoutStatus = true;
                disconnectDriver();
                mAuth.signOut();
                logout();
            }
            return true;
        });
    }

    private void logout() {
        preferenceManager.setValue(KeyString.SIGN_IN_FLAG, false);
        preferenceManager.setValue(KeyString.DRIVER_MODE, false);
        //preferenceManager.clear();
        Intent i = new Intent(DriverMapActivity.this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.driver_map_style);
        mMap.setMapStyle(mapStyleOptions);
        buildGoogleApiClient();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE);
            }
        } else {
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationWork();
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(DriverMapActivity.this,
                            "Please provide location permission.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    void locationWork() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            locationWork();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            if (!driverLogoutStatus) {
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                String userID = "";
                try{
                    if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null) {
                        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                //String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driverAvailabilityRef = FirebaseDatabase.getInstance()
                        .getReference().child("Drivers Available");
                GeoFire geoFireDriverAvailability = new GeoFire(driverAvailabilityRef);

                DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
                GeoFire geoFireDriverWorking = new GeoFire(driverWorkingRef);

                switch (customerId) {
                    case "":
                        geoFireDriverWorking.removeLocation(userID);
                        geoFireDriverAvailability.setLocation(userID, new GeoLocation(location.getLatitude(),
                                location.getLongitude()));
                        break;
                    default:
                        geoFireDriverAvailability.removeLocation(userID);
                        geoFireDriverWorking.setLocation(userID, new GeoLocation(location.getLatitude(),
                                location.getLongitude()));
                        break;
                }

            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!driverLogoutStatus) {
            disconnectDriver();
        }
    }

    private void disconnectDriver() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverAvailabilityRef = FirebaseDatabase.getInstance()
                .getReference().child("Drivers Available");
        GeoFire geoFire = new GeoFire(driverAvailabilityRef);
        geoFire.removeLocation(userID);
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }

    private void getAssignedCustomerInformation() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("Users").child("Customers")
                .child(customerId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String nm = dataSnapshot.child("name").getValue().toString();
                    String ph = dataSnapshot.child("phone").getValue().toString();

                    customer_name_text.setText("Name : " + nm);
                    customer_destination_text.setText("Destination : " + cusomerDestination);

                    if (dataSnapshot.hasChild("image")) {
                        String img = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(img).into(customer_profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
