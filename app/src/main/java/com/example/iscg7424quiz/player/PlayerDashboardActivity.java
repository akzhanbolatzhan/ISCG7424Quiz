package com.example.iscg7424quiz.player;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.adapter.PlayerTournamentAdapter;
import com.example.iscg7424quiz.model.Tournament;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.example.iscg7424quiz.R;

public class PlayerDashboardActivity extends AppCompatActivity {
    private PlayerTournamentAdapter adapter;
    private List<Tournament> tournamentList;
    private Set<String> participatedIds;

    private DatabaseReference tournamentsRef, participationsRef;
    private ValueEventListener tournamentsListener, participationsListener;

    private RecyclerView rvTournaments;
    private RadioGroup rgStatus;
    private TextView tvWelcome, tvEmpty, btnLogout;
    private String playerEmail;
    private boolean tournamentsLoaded = false;
    private boolean participationsLoaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvTournaments = findViewById(R.id.rvTournaments);
        rgStatus = findViewById(R.id.rgStatus);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnLogout = findViewById(R.id.btnLogout);
        playerEmail = getIntent().getStringExtra("playerEmail");
        if (playerEmail == null || playerEmail.isEmpty()) {
            Toast.makeText(this, "Player email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvWelcome.setText("Welcome, " + playerEmail);
        tournamentList = new ArrayList<>();
        participatedIds = new HashSet<>();
        tournamentsRef = FirebaseDatabase.getInstance().getReference("tournaments");
        String playerKey = playerEmail.replace("@", "_").replace(".", "_");
        participationsRef = FirebaseDatabase.getInstance().getReference("participations").child(playerKey);
        initView();
        initData();
    }
    private void initView() {
        adapter = new PlayerTournamentAdapter(new ArrayList<>(), new PlayerTournamentAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Tournament tournament) {
                joinTournament(tournament);
            }
        });
        rvTournaments.setLayoutManager(new LinearLayoutManager(this));
        rvTournaments.setAdapter(adapter);
        rgStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateTournamentList();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void initData() {
        tournamentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tournamentList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Tournament tournament = child.getValue(Tournament.class);
                    if (tournament != null && child.getKey() != null) {
                        tournament.setId(child.getKey());
                        tournamentList.add(tournament);
                    }
                }
                tournamentsLoaded = true;
                updateTournamentList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tournamentsLoaded = false;
                adapter.updateData(new ArrayList<>(), "Ongoing");
                tvEmpty.setText("Failed to load tournaments: " + error.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        };
        tournamentsRef.addValueEventListener(tournamentsListener);
        participationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                participatedIds.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey() != null) {
                        participatedIds.add(child.getKey());
                    }
                }
                participationsLoaded = true;
                updateTournamentList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                participationsLoaded = false;
                adapter.updateData(new ArrayList<>(), "Ongoing");
                tvEmpty.setText("Failed to load participation records: " + error.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        };
        participationsRef.addValueEventListener(participationsListener);
    }
    private void updateTournamentList() {
        if (!tournamentsLoaded || !participationsLoaded) {
            return;
        }
        String selectedStatus = "Ongoing";
        int checkedId = rgStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.rbUpcoming) {
            selectedStatus = "Upcoming";
        } else if (checkedId == R.id.rbPast) {
            selectedStatus = "Past";
        } else if (checkedId == R.id.rbParticipated) {
            selectedStatus = "Participated";
        }
        List<Tournament> filteredList = new ArrayList<>();
        for (Tournament tournament : tournamentList) {
            String status = getTournamentStatus(tournament);
            if (status.equals(selectedStatus)) {
                filteredList.add(tournament);
            }
        }
        adapter.updateData(filteredList, selectedStatus);
        if (filteredList.isEmpty()) {
            tvEmpty.setText("No " + selectedStatus.toLowerCase(Locale.US) + " tournaments");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
    private String getTournamentStatus(Tournament tournament) {
        if (participatedIds.contains(tournament.getId())) {
            return "Participated";
        }
        Date startDate = parseDate(tournament.getStartDate());
        Date endDate = parseDate(tournament.getEndDate());
        Date today = parseDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
        if (startDate == null || endDate == null || today == null) {
            return "Invalid";
        }

        if (today.before(startDate)) {
            return "Upcoming";
        } else if (today.after(endDate)) {
            return "Past";
        } else {
            return "Ongoing";
        }
    }
    private Date parseDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        String pattern = "dd/MM/yyyy";

        if (date.contains("-")) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    private void joinTournament(Tournament tournament) {

        if (!tournamentsLoaded || !participationsLoaded) {
            return;
        }
        if (!getTournamentStatus(tournament).equals("Ongoing")) {
            Toast.makeText(this, "This tournament is not available", Toast.LENGTH_SHORT).show();
            updateTournamentList();
            return;
        }
        if (tournament.getQuestions() == null || tournament.getQuestions().size() != 10) {
            Toast.makeText(this, "This tournament does not have 10 questions", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(PlayerDashboardActivity.this, QuizActivity.class);
        intent.putExtra("tournamentId", tournament.getId());
        intent.putExtra("tournamentName", tournament.getName());
        intent.putExtra("playerEmail", playerEmail);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (rgStatus != null) {
            updateTournamentList();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tournamentsRef != null && tournamentsListener != null) {
            tournamentsRef.removeEventListener(tournamentsListener);
        }
        if (participationsRef != null && participationsListener != null) {
            participationsRef.removeEventListener(participationsListener);
        }
    }
}