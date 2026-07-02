package com.example.votingapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class CardVerificationActivity extends AppCompatActivity {

    private TextView tvName, tvVoterId, tvDob, tvBooth, tvMatch;
    private Button btnProceedFingerprint;
    private NfcAdapter nfcAdapter;
    private boolean isCardVerified = false;
    private static final String TAG = "CardVerification";

    private String boothId;
    private String voterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_verification);

        // ✅ Get booth ID from login
        boothId = getIntent().getStringExtra("username");

        initializeUI();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        btnProceedFingerprint.setOnClickListener(v -> {
            if (isCardVerified) {
                Intent intent = new Intent(this, FaceVerificationActivity.class);
                intent.putExtra("boothId", boothId);
                intent.putExtra("VoterId", voterId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please verify a voter first!", Toast.LENGTH_SHORT).show();
            }
        });

        resetUI();
    }

    private void initializeUI() {
        tvName = findViewById(R.id.tvName);
        tvVoterId = findViewById(R.id.tvVoterId);
        tvDob = findViewById(R.id.tvDob);
        tvBooth = findViewById(R.id.tvBooth);
        tvMatch = findViewById(R.id.tvMatch);
        btnProceedFingerprint = findViewById(R.id.btnProceedFingerprint);
    }

    private void resetUI() {
        tvName.setText("NAME: ");
        tvVoterId.setText("VOTER ID: ");
        tvDob.setText("DOB: ");
        tvBooth.setText("BOOTH NO: ");
        tvMatch.setText("Tap the NFC card...");
        tvMatch.setTextColor(ContextCompat.getColor(this, R.color.yellow));
        isCardVerified = false;
        btnProceedFingerprint.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            readNfcTag(intent);
        }
    }

    private void readNfcTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            Toast.makeText(this, "NDEF is not supported by this tag", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ndef.connect();
            NdefMessage message = ndef.getNdefMessage();

            if (message != null) {
                NdefRecord[] records = message.getRecords();
                for (NdefRecord record : records) {
                    if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                            Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {

                        byte[] payload = record.getPayload();
                        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                        int langCodeLen = payload[0] & 0063;

                        voterId = new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding);
                        Log.d(TAG, "Voter ID from NFC: " + voterId);

                        verifyVoterFromFirebase(voterId);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading NFC tag", e);
        } finally {
            try {
                ndef.close();
            } catch (Exception ignored) {}
        }
    }

    private void verifyVoterFromFirebase(String voterId) {
        resetUI();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Voters")
                .child(voterId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    tvMatch.setText("❌ Voter not found");
                    tvMatch.setTextColor(ContextCompat.getColor(CardVerificationActivity.this, R.color.black));
                    return;
                }

                String boothFromFirebase = snapshot.child("BId").getValue(String.class);
                Boolean hasVoted = snapshot.child("Voted").getValue(Boolean.class);

                // ✅ 1️⃣ Already voted check FIRST
                if (hasVoted != null && hasVoted) {
                    tvMatch.setText("⚠️ Already Voted");
                    tvMatch.setTextColor(ContextCompat.getColor(CardVerificationActivity.this, R.color.black));
                    Toast.makeText(CardVerificationActivity.this, "This voter has already voted.", Toast.LENGTH_LONG).show();
                    isCardVerified = false;
                    btnProceedFingerprint.setEnabled(false);
                    return;
                }

                // ✅ 2️⃣ Booth Match AFTER Voted check
                if (boothFromFirebase == null || boothId == null) {
                    tvMatch.setText("❌ Booth Info Missing");
                    tvMatch.setTextColor(ContextCompat.getColor(CardVerificationActivity.this, R.color.black));
                    return;
                }

                if (!boothFromFirebase.trim().equalsIgnoreCase(boothId.trim())) {
                    tvMatch.setText("❌ Booth Mismatch");
                    tvMatch.setTextColor(ContextCompat.getColor(CardVerificationActivity.this, R.color.black));
                    Log.d(TAG, "Booth mismatch: Voter=" + boothFromFirebase + ", Login=" + boothId);
                    return;
                }

                // ✅ 3️⃣ All Good
                displayVoterDetails(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CardVerificationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVoterDetails(DataSnapshot snapshot) {
        String name = snapshot.child("VoterName").getValue(String.class);
        String dob = snapshot.child("DOB").getValue(String.class);
        String booth = snapshot.child("BId").getValue(String.class);

        tvName.setText("NAME: " + (name != null ? name : "-"));
        tvVoterId.setText("VOTER ID: " + voterId);
        tvDob.setText("DOB: " + (dob != null ? dob : "-"));
        tvBooth.setText("BOOTH NO: " + (booth != null ? booth : "-"));

        tvMatch.setText("✅ Verified");
        tvMatch.setTextColor(ContextCompat.getColor(this, R.color.yellow));

        isCardVerified = true;
        btnProceedFingerprint.setEnabled(true);
    }
}
