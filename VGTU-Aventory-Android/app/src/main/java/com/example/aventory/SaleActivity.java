package com.example.aventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aventory.network.ApiClient;
import com.example.aventory.network.ApiModels;
import com.example.aventory.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaleActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private String authorizationHeader;

    private TextView productTitle;
    private TextView productDetails;
    private EditText quantityInput;
    private RadioButton saleRadio;
    private EditText noteInput;
    private Button confirmButton;
    private ProgressBar loadingProgress;

    private ApiModels.ProductScanResponse product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        String token = sessionManager.getToken();
        authorizationHeader = "Bearer " + token;

        productTitle = findViewById(R.id.productTitle);
        productDetails = findViewById(R.id.productDetails);
        quantityInput = findViewById(R.id.quantityInput);
        saleRadio = findViewById(R.id.saleRadio);
        noteInput = findViewById(R.id.noteInput);
        confirmButton = findViewById(R.id.confirmSaleButton);
        loadingProgress = findViewById(R.id.saleLoadingProgress);
        Button cancelButton = findViewById(R.id.cancelSaleButton);

        confirmButton.setOnClickListener(view -> submitSale());
        cancelButton.setOnClickListener(view -> finish());

        String scannedQr = getIntent().getStringExtra(android.content.Intent.EXTRA_TEXT);
        if (scannedQr == null || scannedQr.isBlank()) {
            Toast.makeText(this, R.string.scan_invalid_qr, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(scannedQr);
    }

    private void loadProduct(String qrCode) {
        setLoading(true);

        ApiClient.getApi().getProductByQr(authorizationHeader, qrCode).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiModels.ProductScanResponse> call, Response<ApiModels.ProductScanResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    showProductError();
                    return;
                }

                product = response.body();
                productTitle.setText(product.productName);
                String details = getString(
                        R.string.product_details_template,
                        product.quantity,
                        product.price,
                        product.productDescription == null ? "-" : product.productDescription
                );
                productDetails.setText(details);
                confirmButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ApiModels.ProductScanResponse> call, Throwable throwable) {
                setLoading(false);
                showProductError();
            }
        });
    }

    private void submitSale() {
        if (product == null) {
            return;
        }

        String quantityRaw = quantityInput.getText().toString().trim();
        if (quantityRaw.isBlank()) {
            Toast.makeText(this, R.string.sale_invalid_quantity, Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityRaw);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, R.string.sale_invalid_quantity, Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity <= 0) {
            Toast.makeText(this, R.string.sale_invalid_quantity, Toast.LENGTH_SHORT).show();
            return;
        }

        String saleType = saleRadio.isChecked() ? "SALE" : "RETURN";
        String note = noteInput.getText().toString().trim();

        ApiModels.SaleRequest request = new ApiModels.SaleRequest(
                product.productId,
                sessionManager.getUserId(),
                quantity,
                "MOBILE_APP",
                saleType,
                note
        );

        setLoading(true);
        ApiClient.getApi().createSale(authorizationHeader, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);
                if (!response.isSuccessful()) {
                    Toast.makeText(SaleActivity.this, R.string.sale_submit_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(SaleActivity.this)
                        .setTitle(R.string.sale_success_title)
                        .setMessage(R.string.sale_success_message)
                        .setPositiveButton(R.string.go_back, (dialogInterface, i) -> finish())
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                setLoading(false);
                Toast.makeText(SaleActivity.this, R.string.sale_submit_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductError() {
        productTitle.setText(R.string.product_not_found_title);
        productDetails.setText(R.string.product_not_found_message);
        confirmButton.setEnabled(false);
    }

    private void setLoading(boolean loading) {
        loadingProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        confirmButton.setEnabled(!loading && product != null);
    }
}
