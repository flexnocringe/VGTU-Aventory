package com.example.aventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aventory.session.SessionManager;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView loggedInAsText;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Toast.makeText(this, R.string.scan_cancelled, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent saleIntent = new Intent(this, SaleActivity.class);
        saleIntent.putExtra(Intent.EXTRA_TEXT, result.getContents());
        startActivity(saleIntent);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginScreen();
            return;
        }

        loggedInAsText = findViewById(R.id.loggedInAsText);
        Button scanButton = findViewById(R.id.scanButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        scanButton.setOnClickListener(view -> launchQrScanner());
        logoutButton.setOnClickListener(view -> {
            sessionManager.clearSession();
            openLoginScreen();
        });

        updateLoggedInInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager == null) {
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            openLoginScreen();
            return;
        }

        updateLoggedInInfo();
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_prompt));
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.addExtra(Intents.Scan.MISSING_CAMERA_PERMISSION, true);
        barcodeLauncher.launch(options);
    }

    private void updateLoggedInInfo() {
        String email = sessionManager.getEmail();
        loggedInAsText.setText(getString(R.string.logged_in_as, email));
    }

    private void openLoginScreen() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}