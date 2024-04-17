package com.example.rescueconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText emailText,passwordText;
    Button loginButton;
    TextView signUpButton;
    FirebaseAuth firebaseAuth;
    private LoadingAlertDialog loadingAlertDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        emailText = findViewById(R.id.loginEmail);
        passwordText = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpText);
        loadingAlertDialog = new LoadingAlertDialog(this,"Logging in...");

        loginButton.setOnClickListener(v -> {
            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();
            if(email.isEmpty()) {
                emailText.setError("Required");
                return;
            }
            if(password.isEmpty()) {
                passwordText.setError("Required");
                return;
            }
            firebaseAuth = FirebaseAuth.getInstance();
            loadingAlertDialog.startLoading();
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, task -> {
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,"Login Successful",Toast.LENGTH_LONG).show();
                    Intent i = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(i);
                    LoginActivity.this.finish();
                }
                else{
                    emailText.setText("");
                    passwordText.setText("");
                    emailText.requestFocus();
                    loadingAlertDialog.stopLoading();
                    Toast.makeText(LoginActivity.this,"Wrong Credentials",Toast.LENGTH_LONG).show();
                }
            });
        });

        signUpButton.setOnClickListener(v ->{
            Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(i);
            LoginActivity.this.finish();
        });

        ((ConstraintLayout)(findViewById(R.id.loginConstraintLayout))).setOnClickListener(v -> {
            ((InputMethodManager)(getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(v.getWindowToken(),0);
        });
    }
}
