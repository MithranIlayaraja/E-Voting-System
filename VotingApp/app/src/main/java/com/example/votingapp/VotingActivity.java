package com.example.votingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VotingActivity extends AppCompatActivity {

    private RadioGroup rgParties;
    private Button btnCast;
    private TextView tvConstituencyName;
    private DatabaseReference dbRef;
    private String boothId;
    private String constituencyId;
    private String voterId; // ✅ Added to mark as voted later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        rgParties = findViewById(R.id.rgParties);
        btnCast = findViewById(R.id.btnCast);
        tvConstituencyName = findViewById(R.id.tvConstituencyName);

        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // ✅ Get boothId & voterId from previous screen
        boothId = getIntent().getStringExtra("boothId");
        voterId = getIntent().getStringExtra("VoterId");

        if (boothId == null || boothId.isEmpty()) {
            Toast.makeText(this, "Booth ID not provided!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (voterId == null || voterId.isEmpty()) {
            Toast.makeText(this, "Voter ID not provided!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Step 1️⃣: Get CId for this booth
        dbRef.child("Booth").child(boothId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            constituencyId = snapshot.child("CId").getValue(String.class);
                            if (constituencyId != null) {
                                loadConstituencyName(constituencyId);
                                loadCandidatesForConstituency(constituencyId);
                            } else {
                                Toast.makeText(VotingActivity.this, "No constituency found for this booth.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VotingActivity.this, "Booth not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VotingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Step 2️⃣: Handle vote button
        btnCast.setOnClickListener(v -> {
            int selectedId = rgParties.getCheckedRadioButtonId();
            if (selectedId == -1) {
                new AlertDialog.Builder(this)
                        .setTitle("No Selection")
                        .setMessage("Please choose a candidate to cast your vote.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            RadioButton rb = findViewById(selectedId);
            String candidateName = rb.getText().toString();
            String candidateKey = (String) rb.getTag(); // ✅ key of candidate node in Firebase

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Vote")
                    .setMessage("Are you sure you want to vote for " + candidateName + "?")
                    .setPositiveButton("Confirm", (dialog, which) -> castVote(candidateKey, candidateName))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // Step 3️⃣ - Load Constituency Name
    private void loadConstituencyName(String cId) {
        dbRef.child("Constituency").child(cId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String cName = snapshot.child("CName").getValue(String.class);
                            if (cName != null) {
                                tvConstituencyName.setText("Constituency: " + cName);
                                tvConstituencyName.setVisibility(TextView.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VotingActivity.this, "Error loading constituency name.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Step 4️⃣ - Load Candidates Dynamically (Works for any number of candidates)
    private void loadCandidatesForConstituency(String cId) {
        dbRef.child("Candidate").orderByChild("CId").equalTo(cId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rgParties.removeAllViews();

                        if (snapshot.exists()) {
                            for (DataSnapshot candidateSnap : snapshot.getChildren()) {
                                String candidateName = candidateSnap.child("Name").getValue(String.class);
                                String partyName = candidateSnap.child("PartyName").getValue(String.class);
                                String key = candidateSnap.getKey(); // ✅ Unique candidate key

                                if (candidateName == null || partyName == null) continue;

                                RadioButton rb = new RadioButton(VotingActivity.this);
                                rb.setText(candidateName + " (" + partyName + ")");
                                rb.setTextColor(getResources().getColor(R.color.marron));
                                rb.setTextSize(16);
                                rb.setPadding(8, 12, 8, 12);
                                rb.setTag(key); // ✅ Store candidate node key
                                rgParties.addView(rb);
                            }
                        } else {
                            Toast.makeText(VotingActivity.this, "No candidates found for this constituency.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VotingActivity.this, "Error loading candidates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Step 5️⃣ - Update vote count and mark voter as voted
    private void castVote(String candidateKey, String chosenCandidate) {
        if (candidateKey == null || candidateKey.isEmpty()) {
            Toast.makeText(this, "Invalid candidate selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference candidateRef = dbRef.child("Candidate").child(candidateKey).child("Votes");

        candidateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long currentVotes = snapshot.getValue(Long.class);
                if (currentVotes == null) currentVotes = 0L;

                // ✅ Step 1: Update candidate vote count
                candidateRef.setValue(currentVotes + 1)
                        .addOnSuccessListener(aVoid -> {
                            // ✅ Step 2: Mark voter as voted = true
                            DatabaseReference voterRef = dbRef.child("Voters").child(voterId);
                            voterRef.child("Voted").setValue(true)
                                    .addOnSuccessListener(v -> {
                                        Toast.makeText(VotingActivity.this, "Vote successfully recorded!", Toast.LENGTH_SHORT).show();

                                        // ✅ Step 3: Redirect to confirmation page
                                        Intent i = new Intent(VotingActivity.this, VoteConfirmationActivity.class);
                                        i.putExtra("party", chosenCandidate);
                                        startActivity(i);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(VotingActivity.this, "Error marking voter as voted: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(VotingActivity.this, "Error updating vote: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VotingActivity.this, "Error casting vote: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
