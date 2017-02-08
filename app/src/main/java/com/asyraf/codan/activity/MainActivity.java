package com.asyraf.codan.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asyraf.codan.Manifest;
import com.asyraf.codan.R;
import com.asyraf.codan.common.Constant;
import com.asyraf.codan.object.User;
import com.asyraf.codan.services.LocationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth rootUrl;
    private DatabaseReference urlCurrenUser;
    private DatabaseReference urlAllUser;
    private FirebaseAuth mAuthData;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String currenUserId;
    private String currenUserEmail;
    private ArrayList<User> arrUser;
    private AllUserAdapter allUserAdapter;
    private ArrayList<String> arrStringEmail;
    private ValueEventListener valueEventListenerUserConnected;
    private User currenUser;
    @BindView(R.id.btnLogout)
    Button btnLogout;
    @BindView(R.id.lvUser)
    ListView lvUser;
    @BindView(R.id.tvUserName)
    TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MainActivity.this.startService(new Intent(MainActivity.this, LocationService.class));
        arrStringEmail = new ArrayList<>();
        arrUser = new ArrayList<User>();
        allUserAdapter = new AllUserAdapter(MainActivity.this, 0, arrUser);
        lvUser.setAdapter(allUserAdapter);
        lvUser.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent=new Intent(MainActivity.this,MapsActivity.class);
            User user=arrUser.get(position);
            Gson gson=new Gson();
            intent.putExtra(Constant.KEY_SEND_USER, gson.toJson(user) +"---"+ gson.toJson(currenUser));
            startActivity(intent);
        });
        rootUrl = FirebaseAuth.getInstance();
        mAuthStateListener = this::setAuthenticatedUser;
        rootUrl.addAuthStateListener(mAuthStateListener);

        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance

        // Must be done during an initialization phase like onCreate
        rxPermissions.request(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        startService(new Intent(this, LocationService.class));
                        // I can control the camera now
                    } else {
                        // Oups permission denied
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setAuthenticatedUser(FirebaseAuth authData) {
        mAuthData = authData;
        if (authData!= null){
            if (authData.getCurrentUser() != null) {
                currenUserId = authData.getCurrentUser().getUid();
                currenUserEmail = authData.getCurrentUser().getEmail();
                getCurrenUser(authData);
                getAllUser(authData);
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    }

    public void getCurrenUser(FirebaseAuth authData) {
        if (authData.getCurrentUser()!=null){
            urlCurrenUser = FirebaseDatabase.getInstance().getReference().child(Constant.CHILD_USERS).child(authData.getCurrentUser().getUid());
        }
        urlCurrenUser.addValueEventListener(valueEventListenerCurrenUser);
        valueEventListenerUserConnected= FirebaseDatabase.getInstance().getReference().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    urlCurrenUser.child(Constant.CHILD_CONNECTION).setValue(Constant.KEY_ONLINE);
                    urlCurrenUser.child(Constant.CHILD_CONNECTION).onDisconnect().setValue(Constant.KEY_OFFLINE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    private ValueEventListener valueEventListenerCurrenUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            tvUserName.setText("Hello "+user.name);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };
    public void getAllUser(FirebaseAuth authData) {
        urlAllUser = FirebaseDatabase.getInstance().getReference().child(Constant.CHILD_USERS);
        urlAllUser.addChildEventListener(childEventListenerAllUser);
    }
    private ChildEventListener childEventListenerAllUser = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            User user = dataSnapshot.getValue(User.class);
            if (!dataSnapshot.getKey().equals(currenUserId)){
                arrStringEmail.add(user.email);
                arrUser.add(user);
                allUserAdapter.notifyDataSetChanged();
            }else {
                currenUser=user;
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (!dataSnapshot.getKey().equals(currenUserId)){
                User user = dataSnapshot.getValue(User.class);
                int index = arrStringEmail.indexOf(user.email);
                arrUser.set(index, user);
                allUserAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            rootUrl.removeAuthStateListener(mAuthStateListener);
        } catch (Exception e) {
        }
        try {
            urlCurrenUser.removeEventListener(valueEventListenerCurrenUser);
        } catch (Exception e) {
        }
        try {
            urlAllUser.removeEventListener(childEventListenerAllUser);
        } catch (Exception e) {
        }
        try {
            FirebaseDatabase.getInstance().getReference().child(".info/connected").removeEventListener(valueEventListenerUserConnected);
        }catch (Exception e){}
    }

    @OnClick(R.id.btnLogout)
    public void btnLogout() {
        if (this.mAuthData.getCurrentUser() != null) {
            stopService(new Intent(this, LocationService.class));
            urlCurrenUser.child(Constant.CHILD_CONNECTION).setValue(Constant.KEY_OFFLINE);
            rootUrl.removeAuthStateListener(mAuthStateListener);
            FirebaseAuth.getInstance().signOut();
            setAuthenticatedUser(null);
        }
    }


    public class AllUserAdapter extends ArrayAdapter<User> {
        private Activity mActivity;
        private ArrayList<User> mArrUser;
        @BindView(R.id.tvNameUser)
        TextView tvNameUser;
        @BindView(R.id.tvStatus)
        TextView tvStatus;

        public AllUserAdapter(Activity mActivity, int resource, ArrayList<User> mArrUser) {
            super(mActivity, resource, mArrUser);
            this.mActivity = mActivity;
            this.mArrUser = mArrUser;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = mActivity.getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.item_list_user, null);
            }
            ButterKnife.bind(this, convertView);
            tvNameUser.setText(mArrUser.get(position).name);
            tvStatus.setText(mArrUser.get(position).connection);
            if (mArrUser.get(position).connection.equals(Constant.KEY_ONLINE)){
                tvStatus.setTextColor(Color.parseColor("#00FF00"));
            }else {
                tvStatus.setTextColor(Color.parseColor("#FF0000"));
            }
            return convertView;
        }
    }

}
