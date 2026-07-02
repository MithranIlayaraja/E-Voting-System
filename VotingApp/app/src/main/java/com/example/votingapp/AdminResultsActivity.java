package com.example.votingapp;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AdminResultsActivity extends AppCompatActivity {

    private LinearLayout resultsContainer;
    private DatabaseReference dbRefCandidates, dbRefConstituencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_results);

        resultsContainer = findViewById(R.id.resultsContainer);

        dbRefCandidates = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Candidate");

        dbRefConstituencies = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Constituency");

        loadResultsLive();
    }

    /**
     * ✅ Loads and computes election results by constituency.
     */
    private void loadResultsLive() {
        dbRefCandidates.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resultsContainer.removeAllViews();

                if (!snapshot.exists()) {
                    Toast.makeText(AdminResultsActivity.this, "No candidate data found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Group candidates by constituency
                HashMap<String, List<CandidateResult>> groupedResults = new HashMap<>();

                for (DataSnapshot candidateSnap : snapshot.getChildren()) {
                    String cId = candidateSnap.child("CId").getValue(String.class);
                    String name = candidateSnap.child("Name").getValue(String.class);
                    String party = candidateSnap.child("PartyName").getValue(String.class);
                    Long votes = candidateSnap.child("Votes").getValue(Long.class);

                    if (cId == null || name == null || party == null) continue;
                    if (votes == null) votes = 0L;

                    CandidateResult candidate = new CandidateResult(name, party, votes);

                    if (!groupedResults.containsKey(cId))
                        groupedResults.put(cId, new ArrayList<>());

                    groupedResults.get(cId).add(candidate);
                }

                // Now for each constituency, compute winner and lead
                for (String cId : groupedResults.keySet()) {
                    List<CandidateResult> candidates = groupedResults.get(cId);
                    Collections.sort(candidates, (a, b) -> Long.compare(b.votes, a.votes)); // sort descending by votes

                    CandidateResult winner = candidates.get(0);
                    long lead = 0;
                    if (candidates.size() > 1)
                        lead = winner.votes - candidates.get(1).votes;

                    addConstituencyResultView(cId, winner, lead, candidates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminResultsActivity.this, "Error loading results: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Builds a nice results card for each constituency.
     */
    private void addConstituencyResultView(String cId, CandidateResult winner, long lead, List<CandidateResult> allCandidates) {
        // Fetch constituency name for readability
        dbRefConstituencies.child(cId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String cName = snapshot.child("CName").getValue(String.class);
                if (cName == null) cName = cId;

                // 🏛 Header
                TextView header = new TextView(AdminResultsActivity.this);
                header.setText("🏛 Constituency: " + cName);
                header.setTextSize(22);
                header.setPadding(15, 25, 15, 15);
                header.setTextColor(getResources().getColor(R.color.marron));
                resultsContainer.addView(header);

                // 🥇 Winner section
                TextView winnerView = new TextView(AdminResultsActivity.this);
                winnerView.setText("🥇 Winner: " + winner.name + " (" + winner.party + ")\n" +
                        "🗳️ Votes: " + winner.votes + "\n" +
                        "📊 Lead by: " + lead + " votes");
                winnerView.setTextSize(18);
                winnerView.setPadding(30, 10, 30, 20);
                winnerView.setTextColor(getResources().getColor(R.color.black));
                winnerView.setBackgroundResource(R.drawable.rounded_box);
                resultsContainer.addView(winnerView);

                // 🧑‍🤝‍🧑 Other Candidates (optional)
                for (int i = 1; i < allCandidates.size(); i++) {
                    CandidateResult c = allCandidates.get(i);
                    TextView others = new TextView(AdminResultsActivity.this);
                    others.setText("• " + c.name + " (" + c.party + ") - " + c.votes + " votes");
                    others.setTextSize(16);
                    others.setPadding(50, 5, 50, 5);
                    others.setTextColor(getResources().getColor(R.color.marron));
                    resultsContainer.addView(others);
                }

                // Spacer between constituencies
                TextView spacer = new TextView(AdminResultsActivity.this);
                spacer.setText("\n");
                resultsContainer.addView(spacer);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminResultsActivity.this, "Error loading constituency name.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class to store candidate data
    private static class CandidateResult {
        String name, party;
        long votes;

        CandidateResult(String name, String party, long votes) {
            this.name = name;
            this.party = party;
            this.votes = votes;
        }
    }
}
