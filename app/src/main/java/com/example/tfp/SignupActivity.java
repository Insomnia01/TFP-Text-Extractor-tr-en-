package com.example.tfp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.auth.User;

public class SignupActivity extends AppCompatActivity {
/*
    ImageButton btnConfirm, btnBack;
    EditText editName, editPhone, editMail, editPassword;
    //FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    CustomProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

      //  firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        dialog = new CustomProgressDialog(this);

        editName = findViewById(R.id.editFullname);
        editMail = findViewById(R.id.editMail);
        editPassword = findViewById(R.id.editPass);
        editPhone = findViewById(R.id.editNumber);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = editName.getText().toString();
                String Phone = editPhone.getText().toString();
                String Mail = editMail.getText().toString().trim();
                String Password = editPassword.getText().toString();

               firebaseAuth.createUserWithEmailAndPassword(Mail,Password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(SignupActivity.this,MainActivity.class));
                                dialog.cancel();

                                firebaseFirestore.collection("User")
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .set(new UserModel(Name,Phone,Mail));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        });
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }*/
}