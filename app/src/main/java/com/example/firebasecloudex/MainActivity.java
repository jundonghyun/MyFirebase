package com.example.firebasecloudex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private final int MY_PERMISSION_REQUEST_LOCATION = 1001;
    //widgets
    private ProgressBar mProgressBar;
    private String email;

    int item;
    //vars

    public MainActivity() {
    }

    private String Email = ((LoginActivity)LoginActivity.context).GlobalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.progressBar);
        /*위치서비스 클라이언트*/
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        GetLastLocation();
    }

    public void GetLastLocation() {

        final Map<String, Double> User = new HashMap<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        /*위치 권환 확인*/
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        if (location != null) {
                            final GeoPoint geoPoint = new GeoPoint(
                                    location.getLatitude(), location.getLongitude()
                            );

                            db.collection("UserLocation")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){

                                                /* 새로운 UserLocation생성 */
                                                if(task.getResult().size() > 0){
                                                    User.put("Latitude", geoPoint.getLatitude());
                                                    User.put("Longitude", geoPoint.getLongitude());

                                                    DocumentReference newUserRef = db
                                                            .collection("UserLocation")
                                                            .document(Email);

                                                    newUserRef.set(User)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG, "GeoPoint successfully Written");

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "GeoPoint Writing Failure");

                                                                }
                                                            });
                                                }
                                            }
                                            /* email로 시작하는 UserLocation이 있는경우 */

                                            else{
                                                DocumentReference newUserLocationRef = db
                                                        .collection("UserLocation")
                                                        .document(Email);

                                                newUserLocationRef
                                                        .update("Longitude", geoPoint.getLongitude())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot added");

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "Error adding document", e);

                                                            }
                                                        });

                                                newUserLocationRef
                                                        .update("Latitude", geoPoint.getLatitude())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot added");

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "Error adding document", e);
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });



        /*firestore에서 데이터 받아오는법*/
        /*db.collection("UserLocation")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });*/

    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Toast toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG );

        switch(item.getItemId()){
            case R.id.menu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                toast.setText("Logout");
                break;

            case R.id.Location:
                GetLastLocation();
                toast.setText("위치 업데이트함");
                break;
        }
        toast.show();

        return super.onOptionsItemSelected(item);
    }

    /*위치권한 요청*/
    public void onRequestPermissionResult(int requestCode, String permission[], int[] grantResult){
        switch (requestCode){
            case MY_PERMISSION_REQUEST_LOCATION:{
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"승인이 허가되어 있습니다.",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this,"아직 승인받지 않았습니다.",Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}