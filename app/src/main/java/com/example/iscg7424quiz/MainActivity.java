package com.example.iscg7424quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iscg7424quiz.admin.AdminDashboardActivity;
import com.example.iscg7424quiz.player.PlayerDashboardActivity;


public class MainActivity extends AppCompatActivity {
    EditText txtEmail, txtPassword;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please enter email and password",
                            Toast.LENGTH_LONG
                    ).show();
                }
                else if (
                        email.equals("admin@gmail.com")
                                && password.equals("123456")
                ) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Admin login successful",
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(
                            getApplicationContext(),
                            AdminDashboardActivity.class
                    );
                    startActivity(intent);
                }
                else if (
                        email.equals("player@gmail.com")
                                && password.equals("123456")
                ) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Player login successful",
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(
                            getApplicationContext(),
                            PlayerDashboardActivity.class
                    );
                    intent.putExtra("playerEmail", email);
                    startActivity(intent);
                }
                else if (
                        email.equals("player2@gmail.com")
                                && password.equals("123456")
                ) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Player login successful",
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(
                            getApplicationContext(),
                            PlayerDashboardActivity.class
                    );
                    intent.putExtra("playerEmail", email);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Incorrect email or password",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}