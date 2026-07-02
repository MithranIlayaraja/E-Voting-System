package com.example.votingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnViewResults, btnResetVotes, btnLogout;
    private DatabaseReference dbCandidateRef, dbVotersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnViewResults = findViewById(R.id.btnViewResults);
        btnResetVotes = findViewById(R.id.btnResetVotes);
        btnLogout = findViewById(R.id.btnLogout);

        dbCandidateRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Candidate");

        dbVotersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Voters");

        // 🗳️ View Results
        btnViewResults.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminResultsActivity.class);
            startActivity(intent);
        });

        // ♻️ Reset Votes + Reset Voted Flags
        btnResetVotes.setOnClickListener(v -> {
            new AlertDialog.Builder(AdminDashboardActivity.this)
                    .setTitle("Reset Confirmation")
                    .setMessage("Are you sure you want to reset ALL candidates' votes and voter statuses?")
                    .setPositiveButton("Yes", (dialog, which) -> resetAllData())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 🚪 Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ✅ Function to reset both candidates' votes and voters' Voted flag
    private void resetAllData() {
        // 1️⃣ Reset all candidates' votes
        dbCandidateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot candidateSnap) {
                if (candidateSnap.exists()) {
                    for (DataSnapshot candidate : candidateSnap.getChildren()) {
                        candidate.getRef().child("Votes").setValue(0);
                    }

                    // 2️⃣ Then reset all voters' Voted = false
                    dbVotersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot voterSnap) {
                            if (voterSnap.exists()) {
                                for (DataSnapshot voter : voterSnap.getChildren()) {
                                    voter.getRef().child("Voted").setValue(false);
                                }

                                Toast.makeText(AdminDashboardActivity.this,
                                        "✅ All votes and voter statuses have been reset.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "No voter data found to reset.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Error resetting voters: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "No candidate data found.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error resetting candidates: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
