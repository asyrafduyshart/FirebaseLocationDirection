package com.asyraf.codan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.asyraf.codan.R;
import com.asyraf.codan.common.Constant;
import com.asyraf.codan.object.User;
import com.asyraf.codan.services.LocationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class RegisterActivity extends AppCompatActivity {
    public static String TAG = RegisterActivity.class.getSimpleName();

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;


    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtPass)
    EditText edtPass;
    @BindView(R.id.btnRegister)
    Button btnRegister;
    private String userFirstName;
    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                long createTime = new Date().getTime();
                double lat = 0;
                double lng = 0;
                Log.d("lam", "onAuthenticated: " + lat);
                FirebaseDatabase.getInstance().getReference().child(Constant.CHILD_USERS).child(user.getUid()).setValue(new User(user.getUid(), userFirstName, userEmail,
                        Constant.KEY_ONLINE, String.valueOf(createTime), lat, lng))
                        .addOnCompleteListener(task1 -> {
                            EventBus.getDefault().post(Constant.KEY_CLOSE);
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        });

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };

    }

    @OnClick(R.id.btnRegister)
    public void setBtnRegister() {
        final FirebaseAuth rootUrl = FirebaseAuth.getInstance();
        userFirstName = edtName.getText().toString();
        userEmail = edtEmail.getText().toString();
        userPassword = edtPass.getText().toString();
        if (userFirstName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {

        } else {
            rootUrl.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(task -> {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Auth Error", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    @Override
    public void onStart () {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop () {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}