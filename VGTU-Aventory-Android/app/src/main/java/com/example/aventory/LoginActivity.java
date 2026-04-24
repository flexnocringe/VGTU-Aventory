package com.example.aventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aventory.network.ApiClient;
import com.example.aventory.network.ApiModels;
import com.example.aventory.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar loginProgress;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openMainScreen();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginProgress = findViewById(R.id.loginProgress);
        errorText = findViewById(R.id.loginErrorText);

        loginButton.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isBlank() || password.isBlank()) {
            showError(getString(R.string.login_error_required));
            return;
        }

        setLoading(true);
        hideError();

        ApiClient.getApi().login(new ApiModels.LoginRequest(email, password)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiModels.LoginResponse> call, Response<ApiModels.LoginResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    showError(getString(R.string.login_error_invalid));
                    return;
                }

                ApiModels.LoginResponse body = response.body();
                sessionManager.saveSession(body.token, body.id, body.email);
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                openMainScreen();
            }

            @Override
            public void onFailure(Call<ApiModels.LoginResponse> call, Throwable throwable) {
                setLoading(false);
                String detail = throwable.getMessage();
                if (detail == null || detail.isBlank()) {
                    showError(getString(R.string.login_error_network));
                    return;
                }

                showError(getString(R.string.login_error_network_with_detail, detail));
            }
        });
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
