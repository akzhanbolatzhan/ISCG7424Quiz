package com.example.iscg7424quiz.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.api.OpenTDBApi;
import com.example.iscg7424quiz.model.Question;
import com.example.iscg7424quiz.model.QuestionResponse;
import com.example.iscg7424quiz.model.Tournament;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateTournamentActivity extends AppCompatActivity {
    private final String BASE_URL = "https://opentdb.com/";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    EditText txtTournamentName, txtStartDate, txtEndDate;
    Spinner spinnerCategory;
    RadioGroup rgDifficulty;
    NumberPicker numberPickerRating;
    Button btnCreateTournament;
    TextView btnBack, tvStatus;

    // Category options from OpenTDB
    String[] categories = {
            "Entertainment: Film",
            "Music",
            "Television",
            "Video Games",
            "Sports",
            "Animals"
    };
    int[] categoryIds = {11, 12, 14, 15, 21, 27};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_tournament);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
        txtTournamentName = findViewById(R.id.txt_tournamentName);
        txtStartDate = findViewById(R.id.txt_startDate);
        txtEndDate = findViewById(R.id.txt_endDate);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        numberPickerRating = findViewById(R.id.numberPickerRating);

        btnCreateTournament = findViewById(R.id.btn_createTournament);
        btnBack = findViewById(R.id.btn_back);
        tvStatus = findViewById(R.id.tvStatus);
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("tournaments");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerCategory.setAdapter(categoryAdapter);
        numberPickerRating.setMinValue(1);
        numberPickerRating.setMaxValue(5);
        numberPickerRating.setValue(3);

        //start Date
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(txtStartDate);
            }
        });
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(txtEndDate);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //create Tournament Button
        btnCreateTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTournament();
            }
        });
    }
    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateTournamentActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(
                            DatePicker view,
                            int year,
                            int month,
                            int dayOfMonth) {
                        String date = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );
                        dateInput.setText(date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    //create Tournament
    private void createTournament() {
        String name = txtTournamentName.getText().toString().trim();
        String startDate = txtStartDate.getText().toString();
        String endDate = txtEndDate.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        int categoryId = categoryIds[
                spinnerCategory.getSelectedItemPosition()
                ];
        String difficulty;
        int selectedDifficulty = rgDifficulty.getCheckedRadioButtonId();
        if (selectedDifficulty == R.id.rbMedium) {
            difficulty = "medium";
        } else if (selectedDifficulty == R.id.rbHard) {
            difficulty = "hard";
        } else {
            difficulty = "easy";
        }
        int rating = numberPickerRating.getValue();
        //validate Input
        if (name.isEmpty()
                || startDate.isEmpty()
                || endDate.isEmpty()) {
            Toast.makeText(
                    CreateTournamentActivity.this,
                    "Please enter all tournament details",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

//validate Dates
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormat.setLenient(false);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (start == null || end == null || start.after(end)) {
                Toast.makeText(
                        this,
                        "End date cannot be before start date",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(
                    this,
                    "Invalid date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        //disable button while loading
        btnCreateTournament.setEnabled(false);
        tvStatus.setText("Getting 10 questions from API");
        //create Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //create REST API Service
        OpenTDBApi api = retrofit.create(OpenTDBApi.class);
        //make GET Request
        Call<QuestionResponse> call = api.getQuestions(
                10,
                categoryId,
                difficulty,
                "multiple"
        );
        //receive API Response
        call.enqueue(new Callback<QuestionResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<QuestionResponse> call,
                    @NonNull Response<QuestionResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null) {
                    QuestionResponse questionResponse = response.body();
                    List<Question> questionList =
                            questionResponse.getResults();
                    //check API Result
                    if (questionResponse.getResponseCode() == 0
                            && questionList != null
                            && questionList.size() == 10) {
                        tvStatus.setText("Saving tournament to Firebase");
                        //generate ID
                        String id = reference.push().getKey();
//
//                        if (id == null) {
//                            tvStatus.setText("Failed to generate tournament ID");
//                            btnCreateTournament.setEnabled(true);
//                            return;
//                        }
                        //create Tournament
                        Tournament tournament = new Tournament(
                                id,
                                name,
                                category,
                                categoryId,
                                difficulty,
                                startDate,
                                endDate,
                                rating,
                                questionList
                        );
                        //save to Firebase
                        reference.child(id).setValue(tournament)
                                .addOnCompleteListener(task -> {
                                    btnCreateTournament.setEnabled(true);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(
                                                CreateTournamentActivity.this,
                                                "Tournament created successfully",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        finish();
                                    } else {
                                        tvStatus.setText(
                                                "Failed to save tournament"
                                        );
                                        Toast.makeText(
                                                CreateTournamentActivity.this,
                                                "Firebase save failed",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    } else {
                        btnCreateTournament.setEnabled(true);
                        tvStatus.setText(
                                "Failed to get 10 questions. API Code: "
                                        + questionResponse.getResponseCode()
                        );
                    }
                } else {
                    btnCreateTournament.setEnabled(true);
                    tvStatus.setText(
                            "API Error: " + response.code()
                    );
                }
            }
            @Override
            public void onFailure(
                    @NonNull Call<QuestionResponse> call,
                    @NonNull Throwable t) {
                btnCreateTournament.setEnabled(true);
                tvStatus.setText("Failed to load questions");
                Toast.makeText(
                        CreateTournamentActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
