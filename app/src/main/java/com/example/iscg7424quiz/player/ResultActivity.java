package com.example.iscg7424quiz.player;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;



import com.example.iscg7424quiz.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class ResultActivity extends AppCompatActivity {
    private TextView tvTournamentName, tvScore;
    private RatingBar ratingBar;
    private Button btnSubmitRating, btnBackDashboard;
    private String tournamentId, playerEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvScore = findViewById(R.id.tvScore);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        btnBackDashboard = findViewById(R.id.btnBackDashboard);
        tournamentId = getIntent().getStringExtra("tournamentId");
        String tournamentName = getIntent().getStringExtra("tournamentName");
        playerEmail = getIntent().getStringExtra("playerEmail");
        int score = getIntent().getIntExtra("score", 0);
        if (tournamentId == null || playerEmail == null || tournamentId.isEmpty() || playerEmail.isEmpty()) {
            Toast.makeText(this, "Result information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTournamentName.setText(tournamentName);
        tvScore.setText(score + "/10");
        btnSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRating();
            }
        });
        btnBackDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void saveRating() {
        int rating = (int) ratingBar.getRating();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        String playerKey = playerEmail.replace("@", "_").replace(".", "_");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("participations/" + playerKey + "/" + tournamentId + "/rating", rating);
        updates.put("ratings/" + tournamentId + "/" + playerKey, rating);
        btnSubmitRating.setEnabled(false);
        reference.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(ResultActivity.this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
            ratingBar.setIsIndicator(true);
            btnSubmitRating.setText("Rating Submitted");
        }).addOnFailureListener(e -> {
            btnSubmitRating.setEnabled(true);
            Toast.makeText(ResultActivity.this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}