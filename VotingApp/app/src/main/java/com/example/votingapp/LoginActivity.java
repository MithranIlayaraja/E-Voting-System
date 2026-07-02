package com.example.votingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private DatabaseReference databaseReference;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Users");

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            } else {
                checkUser(username, password);
            }
        });
    }

    // ✅ Unified login check for Admin and Booth users
    private void checkUser(final String username, final String password) {
        databaseReference.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String storedPass = snapshot.getValue(String.class);

                            if (storedPass != null && storedPass.equals(password)) {
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                // ✅ Admin login flow
                                if (username.equalsIgnoreCase("admin")) {
                                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish(); // prevent going back to login
                                }
                                // ✅ Booth login flow
                                else {
                                    Intent intent = new Intent(LoginActivity.this, CardVerificationActivity.class);
                                    intent.putExtra("username", username); // booth login ID
                                    intent.putExtra("boothId", username);  // booth ID (same)
                                    startActivity(intent);
                                    finish(); // prevent going back to login
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "User not found in Firebase", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ Clear username & password when returning to login screen
    @Override
    protected void onResume() {
        super.onResume();
        etUsername.setText("");
        etPassword.setText("");
    }

    // ✅ Optional: Prevent going back from login once logged in
    @Override
    public void onBackPressed() {
        // disable back button on login screen
        moveTaskToBack(true);
    }
}
