package com.example.iscg7424quiz.player;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iscg7424quiz.R;
import com.example.iscg7424quiz.model.Question;
import com.google.firebase.database.DatabaseReference;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QuizActivity extends AppCompatActivity {

    private DatabaseReference questionsRef;
    private List<Question> questionList;

    private TextView tvTournamentName, tvProgress, tvQuestion, tvFeedback;
    private RadioGroup rgAnswers;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;
    private Button btnCheckAnswer, btnNext;

    private String tournamentId, tournamentName, playerEmail;
    private int currentQuestion = 0;
    private int score = 0;
    private boolean answered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTournamentName = findViewById(R.id.tvTournamentName);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);

        rgAnswers = findViewById(R.id.rgAnswers);
        rbAnswer1 = findViewById(R.id.rbAnswer1);
        rbAnswer2 = findViewById(R.id.rbAnswer2);
        rbAnswer3 = findViewById(R.id.rbAnswer3);
        rbAnswer4 = findViewById(R.id.rbAnswer4);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnNext = findViewById(R.id.btnNext);

        tournamentId = getIntent().getStringExtra("tournamentId");
        tournamentName = getIntent().getStringExtra("tournamentName");
        playerEmail = getIntent().getStringExtra("playerEmail");
        if (tournamentId == null || playerEmail == null || tournamentId.isEmpty() || playerEmail.isEmpty()) {
            Toast.makeText(this, "Quiz information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTournamentName.setText(tournamentName);
        questionList = new ArrayList<>();
        btnCheckAnswer.setEnabled(false);
        questionsRef = FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).child("questions");
        loadQuestions();
        btnCheckAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!answered) {
                    return;
                }
                if (currentQuestion < 9) {
                    currentQuestion++;
                    showQuestion();
                } else {
                    saveResult();
                }
            }
        });
    }
    private void loadQuestions() {
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Question question = child.getValue(Question.class);

                    if (question != null && question.getQuestion() != null && question.getCorrectAnswer() != null
                            && question.getIncorrectAnswers() != null && question.getIncorrectAnswers().size() == 3) {
                        questionList.add(question);
                    }
                }
                if (questionList.size() == 10) {
                    currentQuestion = 0;
                    score = 0;
                    showQuestion();
                } else {
                    Toast.makeText(QuizActivity.this, "This tournament does not have 10 valid questions", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizActivity.this, "Failed to load questions: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    private void showQuestion() {
        answered = false;
        Question question = questionList.get(currentQuestion);
        tvProgress.setText("Question " + (currentQuestion + 1) + " of 10");
        tvQuestion.setText(decodeHtml(question.getQuestion()));
        List<String> answers = new ArrayList<>();
        answers.addAll(question.getIncorrectAnswers());
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(answers);
        RadioButton[] buttons = {rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4};
        rgAnswers.clearCheck();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setText(decodeHtml(answers.get(i)));
            buttons[i].setEnabled(true);
        }
        tvFeedback.setText("");
        tvFeedback.setVisibility(View.GONE);
        btnCheckAnswer.setVisibility(View.VISIBLE);
        btnCheckAnswer.setEnabled(true);
        btnNext.setVisibility(View.GONE);
        btnNext.setEnabled(true);
        if (currentQuestion == 9) {
            btnNext.setText("Finish Quiz");
        } else {
            btnNext.setText("Next Question");
        }
    }
    private void checkAnswer() {
        if (answered || questionList.isEmpty()) {
            return;
        }
        int selectedId = rgAnswers.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedAnswer = findViewById(selectedId);
        Question question = questionList.get(currentQuestion);
        String correctAnswer = decodeHtml(question.getCorrectAnswer());
        String playerAnswer = selectedAnswer.getText().toString();
        if (playerAnswer.equals(correctAnswer)) {
            score++;
            tvFeedback.setText("Correct!");
            tvFeedback.setTextColor(Color.parseColor("#15803D"));
        } else {
            tvFeedback.setText("Incorrect! Correct answer: " + correctAnswer);
            tvFeedback.setTextColor(Color.parseColor("#DC2626"));
        }
        answered = true;
        tvFeedback.setVisibility(View.VISIBLE);
        rbAnswer1.setEnabled(false);
        rbAnswer2.setEnabled(false);
        rbAnswer3.setEnabled(false);
        rbAnswer4.setEnabled(false);
        btnCheckAnswer.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }
    private String decodeHtml(String text) {
        if (text == null) {
            return "";
        }
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
    }
    private void saveResult() {
        btnNext.setEnabled(false);
        tvFeedback.setText("Saving your result...");
        tvFeedback.setVisibility(View.VISIBLE);
        String playerKey = playerEmail.replace("@", "_").replace(".", "_");
        DatabaseReference participationRef = FirebaseDatabase.getInstance().getReference("participations").child(playerKey).child(tournamentId);
        Map<String, Object> participation = new HashMap<>();
        participation.put("tournamentId", tournamentId);
        participation.put("playerEmail", playerEmail);
        participation.put("score", score);
        participation.put("rating", 0);
        participation.put("completedAt", System.currentTimeMillis());
        participationRef.setValue(participation).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putExtra("tournamentId", tournamentId);
            intent.putExtra("tournamentName", tournamentName);
            intent.putExtra("playerEmail", playerEmail);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            btnNext.setEnabled(true);
            Toast.makeText(QuizActivity.this, "Failed to save result: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}