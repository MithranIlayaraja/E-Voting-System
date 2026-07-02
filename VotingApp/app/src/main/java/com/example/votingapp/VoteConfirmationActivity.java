package com.example.votingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VoteConfirmationActivity extends AppCompatActivity {
    TextView tvChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_confirmation);

        tvChosen = findViewById(R.id.tvChosen);

        String party = getIntent().getStringExtra("party");
        if (party != null) {
            tvChosen.setText("You voted for: " + party);
        } else {
            tvChosen.setText("Vote confirmed successfully!");
        }

        // ✅ After 3 seconds, redirect to CardVerificationActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(VoteConfirmationActivity.this, CardVerificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // ✅ Close this activity so it doesn’t stay in back stack
        }, 3000); // 3 seconds delay
    }
}
