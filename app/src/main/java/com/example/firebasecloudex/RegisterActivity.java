package com.example.firebasecloudex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.auth.User;
import com.google.gson.internal.$Gson$Preconditions;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;


public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";

    //widgets
    private EditText mEmail, mPassword, mConfirmPassword;
    private Button mRegisterBtn;
    private ProgressBar mProgressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mRegisterBtn = findViewById(R.id.btn_register);
        mProgressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();



        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(mEmail.getText().toString(), mPassword.getText().toString());
            }
        });

    }

    /* 계정 생성하는 메소드 */
    private void createAccount(String email, String password){

        Map<String, String> User = new HashMap<>();

        User.put("Email", email);
        User.put("Password", password);


        DocumentReference newUserRef = db
                .collection("User")
                .document(email);

        newUserRef.set(User).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Document successfully Written");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Document Writing Failure");
                    }
                });

        if(!isValidEmail(email)){
            Log.d(TAG, "CreateAccount: email is not vaild");
            Toast.makeText(RegisterActivity.this, "Email is not valid",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isValidPasswd(password)){
            Log.d(TAG, "CreateAccount: password is not valid");
            Toast.makeText(RegisterActivity.this, "Password is not valid",
                    Toast.LENGTH_SHORT).show();

            return;
        }
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        /* firebase와 계정 연동하는곳 */
        mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete"+task.isSuccessful());

                        if(!task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    }

                });



        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }




    /* 유효성 검사 */
    private boolean isValidPasswd(String target){
        Pattern p = Pattern.compile("(^.*(?=.{6,100})(?=.*[0-9])(?=.*[a-zA-Z]).*$)");

        Matcher m = p.matcher(target);
        if (m.find() && !target.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")){
            return true;
        }else{
            return false;
        }
    }

    /* 이메일 유효성 검사 */
    private boolean isValidEmail(String target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }





}