package com.asyraf.codan.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.asyraf.codan.R;
import com.asyraf.codan.common.Constant;
import com.asyraf.codan.object.User;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    public static String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private LatLng currenLocation;
    private LatLng friendLocation;
    private User friendUser;
    private User currenUser;
    private DatabaseReference friendUrl;
    @BindView(R.id.btnRoutting)
    Button btnRoutting;
    @BindView(R.id.btnCancelRoutting)
    Button btnCancelRoutting;
    private boolean routting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        String jsonReceiverUser = getIntent().getStringExtra(Constant.KEY_SEND_USER).split("---")[0];
        String jsonCurrenUser = getIntent().getStringExtra(Constant.KEY_SEND_USER).split("---")[1];
        Gson gson = new Gson();
        friendUser = gson.fromJson(jsonReceiverUser, User.class);
        currenUser = gson.fromJson(jsonCurrenUser, User.class);
        currenLocation = new LatLng(currenUser.latitude, currenUser.longitude);
        friendLocation = new LatLng(friendUser.latitude, friendUser.longitude);

    }

    @OnClick(R.id.btnRoutting)
    public void setBtnRoutting() {
        routting=true;
        routing(currenLocation,friendLocation);
    }

    @OnClick(R.id.btnCancelRoutting)
    public void setBtnCancelRoutting() {
        routting=false;
        routing(currenLocation,friendLocation);
    }


    private ValueEventListener valueEventListenerFriendUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            friendLocation = new LatLng(user.latitude, user.longitude);
            routing(currenLocation, friendLocation);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
            friendUrl.removeEventListener(valueEventListenerFriendUser);
        } catch (Exception e) {
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mMap.setMyLocationEnabled(true);
        friendUrl = FirebaseDatabase.getInstance().getReference().child(Constant.CHILD_USERS).child(friendUser.id);
        friendUrl.addValueEventListener(valueEventListenerFriendUser);
        EventBus.getDefault().register(this);
        Handler handler=new Handler();
        handler.postDelayed(() -> {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currenLocation);
            builder.include(friendLocation);
            LatLngBounds bounds = builder.build();
            try {
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngBounds(bounds, 80));
            } catch (Exception e) {
            }
        },500 );
    }



    public void onEvent(Location currenLocation) {
        LatLng lng = new LatLng(currenLocation.getLatitude(), currenLocation.getLongitude());
        routing(lng, friendLocation);
    }

    public void routing(LatLng a, LatLng b) {
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(MapsActivity.this)
                .alternativeRoutes(true)
                .waypoints(a, b)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d(TAG,"Routing Failure");
        if(e.getMessage() != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
        Log.d(TAG,"Routing Start");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        Log.d(TAG,"Routing Success");
        try {
            mMap.clear();
            PolylineOptions polyoptions = new PolylineOptions();
            polyoptions.color(Color.BLUE);
            polyoptions.width(10);
            polyoptions.addAll(arrayList.get(i).getPoints());
            if (routting){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(currenLocation);
                builder.include(friendLocation);
                LatLngBounds bounds = builder.build();
                try {
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngBounds(bounds, 80));
                } catch (Exception e) {
                }
                mMap.addPolyline(polyoptions);
            }
            String distance = arrayList.get(i).getDistanceText();
            mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(MapsActivity.this));

            Marker markerB = mMap.addMarker(new MarkerOptions().position(friendLocation)
                    .title(friendUser.name + "-" + distance).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            markerB.showInfoWindow();
        } catch (Exception e) {
        }
    }

    @Override
    public void onRoutingCancelled() {
        Log.d(TAG,"Routing Cancelled");
    }


    public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View myContentsView;

        public MyInfoWindowAdapter(Activity context) {
            myContentsView = context.getLayoutInflater().inflate(
                    R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvName = (TextView) myContentsView.findViewById(R.id.tvName);
            TextView tvDistance = (TextView) myContentsView.findViewById(R.id.tvDistance);
            TextView tvAddress = (TextView) myContentsView.findViewById(R.id.tvAddress);
            tvName.setText(marker.getTitle().split("-")[0]);
            tvDistance.setText(marker.getTitle().split("-")[1]);
            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }
}
