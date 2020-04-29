package com.hriportfolio.dryve.Customer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import com.google.android.libraries.places.api.Places;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {


    final static int REQUEST_CODE = 1;
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String customerId;

    private DatabaseReference customerDatabaseRef;
    private DatabaseReference driversAvailableDatabaseRef;
    private DatabaseReference driverDatabaseRef;
    private DatabaseReference driverLocationRef;
    private LatLng customerPickUpLocation;

    Marker driverMarker, pickUpMarker;
    private Button findDriverButton;
    private int radius = 1;

    private boolean driverFound = false;
    private boolean requestType = false;
    private String driverFoundId;
    private String whereTo = "";

    private ValueEventListener driverLocationRefListener;
    GeoQuery geoQuery;
    AutocompleteSupportFragment autocompleteFragment;


    @BindView(R.id.customer_menu_button)
    ImageButton customer_menu_button;
    @BindView(R.id.customer_placeholder_text)
    TextView customer_placeholder_text;

    @BindView(R.id.driver_name_text)
    TextView driver_name_text;

    FragmentManager fm = getSupportFragmentManager();

    private String driver_phone_text = "";

    @BindView(R.id.driver_distance_text)
    TextView driver_distance_text;
    @BindView(R.id.driver_car_text)
    TextView driver_car_text;
    @BindView(R.id.driver_profile_image)
    CircleImageView driver_profile_image;
    @BindView(R.id.call_driver_iv)
    ImageView call_driver_iv;
    @BindView(R.id.driver_found_card)
    CardView driver_found_card;
    @BindView(R.id.customer_placeholder_card)
    CardView placeholder_card;

    @BindView(R.id.name_for_customer_home_page)
    TextView customer_name_for_profile_textView;
    @BindView(R.id.pro_pic_for_customer_home_page)
    CircleImageView pro_pic_for_customer_home_page;

    SharedPreferenceManager preferenceManager;
    private String sp_name;
    private String sp_phone;
    private String sp_picUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_customer_map);
        ButterKnife.bind(this);
        findDriverButton = findViewById(R.id.find_drivers_button);
        findDriverButton.setEnabled(false);
        initPref();

        Places.initialize(getApplicationContext(), "AIzaSyDdICGOo49l6fecWuE1iazZgAXzAyWL8TA");
        PlacesClient placesClient = Places.createClient(this);

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                findDriverButton.setEnabled(true);
                findDriverButton.setBackgroundColor(Color.parseColor("#FF00C853"));
                findDriverButton.setTextColor(Color.parseColor("#E8F5E9"));
                findDriverButton.setText("Find A Dryve");
                whereTo = "No Billing Set";
                hideSearchBar();
                customer_placeholder_text.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Status status) {
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests");
        driversAvailableDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");


        checkLocationPermission();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        findDriverButton.setOnClickListener(view -> {
            //requestType true meaning user has already requested car
            if (requestType) {
                requestType = false;
                geoQuery.removeAllListeners();
                //runtime error as not null when without dryvers at work
                driverLocationRef.removeEventListener(driverLocationRefListener);

                if (driverFoundId != null) {
                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Drivers")
                            .child(driverFoundId).child("CustomerRideID");
                    driverDatabaseRef.removeValue();
                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driverFoundId).child("CustomerDestination");
                    driverDatabaseRef.removeValue();

                    driverFoundId = null;
                }
                driverFound = false;
                radius = 1;
                customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                GeoFire geoFire = new GeoFire(customerDatabaseRef);
                geoFire.removeLocation(customerId);

                if (pickUpMarker != null) {
                    pickUpMarker.remove();
                }
                if (driverMarker != null) {
                    driverMarker.remove();
                }
                findDriverButton.setText("Select Destination First");
                findDriverButton.setTextColor(Color.parseColor("#242424"));
                findDriverButton.setBackgroundColor(Color.parseColor("#F1F1F1"));
                findDriverButton.setEnabled(false);
                driver_found_card.setVisibility(View.GONE);
                placeholder_card.setVisibility(View.VISIBLE);
                customer_placeholder_text.setVisibility(View.GONE);
                showSearchBar();


            } else {
                requestType = true;
                GeoFire geoFire = new GeoFire(customerDatabaseRef);
                geoFire.setLocation(customerId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPickUpLocation)
                        .title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                findDriverButton.setText("Getting Your Dryver...");
                getClosestDrivers();
            }
        });
    }

    private void initPref() {
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        sp_name = preferenceManager.getValue(KeyString.NAME, "");
        sp_phone = preferenceManager.getValue(KeyString.PHONE_NUMBER, "");
        sp_picUrl = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL, "");
        if (sp_name.equals("")) {
            redirectUserToSettings();
        } else {
            setupUserInfo();
        }
    }

    private void setupUserInfo() {
        customer_name_for_profile_textView.setText(sp_name);
        if (!sp_picUrl.equals("")) {
            Picasso.get().load(sp_picUrl).into(pro_pic_for_customer_home_page);
        }
    }

    private void redirectUserToSettings() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Oops!");
        Button b = dialog.findViewById(R.id.navi_btn);
        b.setOnClickListener(view -> {
            Intent i = new Intent(CustomerMapActivity.this, SettingsActivity.class);
            i.putExtra("type", "Customers");
            startActivity(i);
        });
        dialog.show();
    }

    private void hideSearchBar() {
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(autocompleteFragment)
                .commit();
    }

    private void showSearchBar() {
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .show(autocompleteFragment)
                .commit();
    }

    private void getClosestDrivers() {
        GeoFire geoFire = new GeoFire(driversAvailableDatabaseRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation
                (customerPickUpLocation.latitude, customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundId = key;
                    Log.d("driverfound", driverFoundId);

                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                            .child(driverFoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerId); //to show info about request to driver
                    driverMap.put("CustomerDestination", whereTo);
                    driverDatabaseRef.updateChildren(driverMap);

                    gettingDriverLocation();
                    findDriverButton.setText("Locating Dryvers..");

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    getClosestDrivers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingDriverLocation() {
        driverLocationRefListener = driverLocationRef.child(driverFoundId).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && requestType) {

                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double lat = 0;
                            double lng = 0;
                            findDriverButton.setBackgroundColor(Color.BLACK);
                            findDriverButton.setText("Tap to Cancel");
                            driver_found_card.setVisibility(View.VISIBLE);
                            placeholder_card.setVisibility(View.GONE);


                            getAssignedDriverInformation();

                            if (driverLocationMap.get(0) != null) {
                                lat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if (driverLocationMap.get(1) != null) {
                                lng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }
                            LatLng driverLatLng = new LatLng(lat, lng);
                            //if driver cancels,then to remove the marker
                            if (driverMarker != null) {
                                driverMarker.remove();
                            }
                            //for distance measure
                            Location location1 = new Location("");
                            location1.setLatitude(customerPickUpLocation.latitude);
                            location1.setLongitude(customerPickUpLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(driverLatLng.latitude);
                            location2.setLongitude(driverLatLng.longitude);

                            float distance = location1.distanceTo(location2);

                            if (distance < 90) {
                                driver_distance_text.setText("Driver's arrived.");
                            } else {
                                driver_distance_text.setText(String.valueOf(distance));
                            }

                            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                                    .title("Your Driver is Here").icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.dryver)));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @OnClick(R.id.customer_menu_button)
    void customer_menu_pressed() {
        PopupMenu popup = new PopupMenu(this, customer_menu_button);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.custommenu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_setting_button) {
                Intent i = new Intent(CustomerMapActivity.this, SettingsActivity.class);
                i.putExtra("type", "Customers");
                startActivity(i);
            }
            if (item.getItemId() == R.id.menu_logout_button) {
                mAuth.signOut();
                logoutCustomer();
            }
            return true;
        });
    }

    private void logoutCustomer() {
        preferenceManager.setValue(KeyString.SIGN_IN_FLAG, false);
        preferenceManager.setValue(KeyString.CUSTOMER_MODE, false);
        Intent i = new Intent(CustomerMapActivity.this, WelcomeActivity.class);
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
        buildGoogleApiClient();
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.customer_map_style);
        mMap.setMapStyle(mapStyleOptions);
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
                    Toast.makeText(CustomerMapActivity.this,
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

        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void getAssignedDriverInformation() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverFoundId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String nm = dataSnapshot.child("name").getValue().toString();
                    String ph = dataSnapshot.child("phone").getValue().toString();
                    String cr = dataSnapshot.child("car").getValue().toString();

                    driver_name_text.setText(nm);
                    driver_phone_text = ph;
                    driver_car_text.setText("Car : " + cr);

                    if (dataSnapshot.hasChild("image")) {
                        String img = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(img).into(driver_profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}
