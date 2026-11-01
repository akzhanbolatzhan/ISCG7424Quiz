package com.example.iscg7424quiz.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.adapter.TournamentAdapter;
import com.example.iscg7424quiz.model.Tournament;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class ManageTournamentsActivity extends AppCompatActivity {
    private TournamentAdapter adapter;
    private List<Tournament> tournamentList;
    private DatabaseReference ratingsRef;
    private ValueEventListener ratingsListener;
    private DatabaseReference tournamentsRef;
    private ValueEventListener tournamentsListener;
    private RecyclerView rvTournaments;
    private TextView tvEmpty, btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_tournaments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvTournaments = findViewById(R.id.rvTournaments);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tournamentList = new ArrayList<>();
        tournamentsRef = FirebaseDatabase.getInstance().getReference("tournaments");
        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
        adapter = new TournamentAdapter(
                tournamentList,
                new TournamentAdapter.OnTournamentClickListener() {
                    @Override
                    public void onEditClick(Tournament tournament) {
                        Intent intent = new Intent(ManageTournamentsActivity.this,
                                EditTournamentActivity.class
                        );
                        intent.putExtra(
                                "tournamentId",tournament.getId()
                        );
                        startActivity(intent);
                    }
                    @Override
                    public void onDeleteClick(Tournament tournament) {
                        deleteTournament(tournament);
                    }
                }
        );
        rvTournaments.setLayoutManager(new LinearLayoutManager(this)
        );
        rvTournaments.setAdapter(adapter);
        loadTournaments();
        loadRatings();
    }
    private void loadTournaments() {
        tournamentsListener = tournamentsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        tournamentList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Tournament tournament = child.getValue(Tournament.class);
                            String id = child.getKey();
                            if (tournament != null && id != null) {
                                tournament.setId(id);
                                tournamentList.add(tournament);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (tournamentList.isEmpty()) {
                            tvEmpty.setText("No tournaments found");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {
                        tvEmpty.setText(
                                "Failed to load tournaments"
                        );
                        tvEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(
                                ManageTournamentsActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadRatings() {
        ratingsListener = ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> ratingTexts = new HashMap<>();

                for (DataSnapshot tournamentSnapshot : snapshot.getChildren()) {
                    String tournamentId = tournamentSnapshot.getKey();
                    int totalRating = 0;
                    int ratingCount = 0;
                    for (DataSnapshot playerSnapshot : tournamentSnapshot.getChildren()) {
                        Object value = playerSnapshot.getValue();
                        if (value instanceof Number) {
                            int rating = ((Number) value).intValue();
                            if (rating >= 1 && rating <= 5) {
                                totalRating += rating;
                                ratingCount++;
                            }
                        }
                    }

                    if (tournamentId != null && ratingCount > 0) {
                        double averageRating = (double) totalRating / ratingCount;
                        String ratingText = String.format(
                                Locale.US, "Rating: %.1f/5 (%d ratings)",
                                averageRating, ratingCount
                        );
                        ratingTexts.put(tournamentId, ratingText);
                    }
                }
                adapter.updateRatings(ratingTexts);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageTournamentsActivity.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //delete Tournament
    private void deleteTournament(Tournament tournament) {
        new AlertDialog.Builder(ManageTournamentsActivity.this)
                .setTitle("Delete Tournament")
                .setMessage(
                        "Are you sure you want to delete "
                                + tournament.getName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                tournamentsRef
                                        .child(tournament.getId())
                                        .removeValue()
                                        .addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(
                                                            @NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(
                                                                    ManageTournamentsActivity.this,
                                                                    "Tournament deleted successfully",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        } else {

                                                            Toast.makeText(
                                                                    ManageTournamentsActivity.this,
                                                                    "Failed to delete tournament",
                                                                    Toast.LENGTH_LONG
                                                            ).show();
                                                        }
                                                    }
                                                }
                                        );
                            }
                        }
                )
                .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tournamentsListener != null) {
            tournamentsRef.removeEventListener(tournamentsListener);
        }
        if (ratingsListener != null) {
            ratingsRef.removeEventListener(ratingsListener);
        }
    }
}