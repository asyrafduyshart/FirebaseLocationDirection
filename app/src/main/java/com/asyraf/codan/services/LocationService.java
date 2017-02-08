package com.asyraf.codan.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.asyraf.codan.common.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.greenrobot.event.EventBus;

/**
 * Created by MyPC on 20/04/2016.
 */
public class LocationService extends Service {
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private FirebaseAuth rootUrl;
    private DatabaseReference urlCurrenUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static double lattitude=0;
    public static double longitude=0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.myLocationListener = new MyLocationListener();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                setAuthenticatedUser(firebaseAuth);
            }
        };
        rootUrl = FirebaseAuth.getInstance();
        rootUrl.addAuthStateListener(mAuthStateListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, myLocationListener);
    }

    private void setAuthenticatedUser(FirebaseAuth authData) {
        if (authData.getCurrentUser() != null) {
            urlCurrenUser =  FirebaseDatabase.getInstance().getReference().child(Constant.CHILD_USERS).child(authData.getCurrentUser().getUid());
        } else {
            urlCurrenUser=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            rootUrl.removeAuthStateListener(mAuthStateListener);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.removeUpdates(myLocationListener);
        } catch (Exception e) {
        }
    }
    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location.getLatitude()!=lattitude && location.getLongitude()!= longitude ){
                try {
                    urlCurrenUser.child(Constant.CHILD_LATITUDE).setValue(location.getLatitude());
                    urlCurrenUser.child(Constant.CHILD_LONGITUDE).setValue(location.getLongitude());
                }catch (Exception e){}
            }
            lattitude=location.getLatitude();
            longitude=location.getLongitude();
            EventBus.getDefault().post(location);
            Log.d("lam", "onLocationChanged:lam "+location.getLatitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    }
}
