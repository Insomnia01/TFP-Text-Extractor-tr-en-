package com.example.tfp;

import static android.content.ContentValues.TAG;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

   // FirebaseAuth firebaseAuth;
    ImageButton btnSnap, btnSignup;
    EditText editMail, editPassword;
    TextView txtForget;
    static boolean shouldRunOnCreate = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (shouldRunOnCreate) {
            //firebaseAuth = FirebaseAuth.getInstance();
            final CustomProgressDialog dialog = new CustomProgressDialog(MainActivity.this);
            SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

            editMail = findViewById(R.id.editMail);
            editPassword = findViewById(R.id.editPass);
            btnSnap = findViewById(R.id.btnSnap);
            btnSnap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* String Mail = editMail.getText().toString().trim();
                    String Password = editPassword.getText().toString().trim();

                    if (Mail.isEmpty() || Password.isEmpty()){
                        Toast.makeText(MainActivity.this, "Lütfen E-posta ve/veya Şifre Alanlarını Doldurunuz.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dialog.show();
                        firebaseAuth.signInWithEmailAndPassword(Mail, Password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        dialog.cancel();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("email", Mail);
                                        editor.putString("password", Password);
                                        editor.apply();
                                        startActivity(new Intent(MainActivity.this, snapActivity.class));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.cancel();
                                        Toast.makeText(MainActivity.this, "Lütfen E-posta ve/veya Şifrenizi doğru giriniz.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }*/
                    Intent intent = new Intent(MainActivity.this,snapActivity.class);
                    startActivity(intent);
                }
            });
            if (sharedPreferences.contains("email") && sharedPreferences.contains("password")) {
                String savedEmail = sharedPreferences.getString("email", "");
                String savedPassword = sharedPreferences.getString("password", "");
                if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                    editMail.setText(savedEmail);
                    editPassword.setText(savedPassword);
                    btnSnap.performClick();
                }
            }

            btnSignup = findViewById(R.id.btnSignup);
            btnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
                    startActivity(intent);
                }
            });

            txtForget = findViewById(R.id.txtForget);
            txtForget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    String Mail = editMail.getText().toString();

                    if (Mail.isEmpty()){
                        Toast.makeText(MainActivity.this, "Lütfen E-posta alanını doldurunuz.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        dialog.setTitle("Mail gönderiliyor...");
                        dialog.show();
                        firebaseAuth.sendPasswordResetEmail(Mail)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.cancel();
                                        Toast.makeText(MainActivity.this, "Şifre sıfırlama mail'i gönderildi. Spam kutunu kontrol et.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.cancel();
                                        Toast.makeText(MainActivity.this, "Lütfen e-postanızı düzgün giriniz.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }*/
                }
            });
        }
    }
}