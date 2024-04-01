package com.example.rescueconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegistrationActivity extends AppCompatActivity {
    EditText firstName, lastName, email, password;
    Button signUpButton;
    TextView signInButton;

    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private LoadingAlertDialog loadingAlertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInText);
        loadingAlertDialog = new LoadingAlertDialog(this,"");

        signUpButton.setOnClickListener(v -> {
            if(firstName.getText().toString().isEmpty()){
                firstName.setError("First Name can't be empty");
                return;
            }

            if(lastName.getText().toString().isEmpty()){
                lastName.setError("Last Name can't be empty");
                return;
            }

            if(password.getText().toString().length()<6){
                password.setError("Password must be more than 6 characters");
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                email.setError("Enter valid email");
                return;
            }
            loadingAlertDialog.startLoading();
            firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(RegistrationActivity.this, task -> {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                            .setDisplayName(firstName.getText().toString()+" "+lastName.getText().toString());

                    assert user != null;
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(firstName.getText().toString()+" "+lastName.getText().toString()).build());
                    Toast.makeText(RegistrationActivity.this,"Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    RegistrationActivity.this.finish();
                }
                else {
                    loadingAlertDialog.stopLoading();
                    Toast.makeText(RegistrationActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            });
        });

        signInButton.setOnClickListener(v -> {
            Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(i);
            RegistrationActivity.this.finish();
        });

        ((ConstraintLayout)(findViewById(R.id.registrationLayout))).setOnClickListener(v -> {
            ((InputMethodManager)(getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(v.getWindowToken(),0);
        });
    }
}
