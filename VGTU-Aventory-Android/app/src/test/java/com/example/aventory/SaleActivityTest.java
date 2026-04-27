package com.example.aventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.aventory.network.ApiClient;
import com.example.aventory.network.ApiModels;
import com.example.aventory.network.AventoryApi;
import com.example.aventory.session.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class SaleActivityTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sessionManager = new SessionManager(context);
        sessionManager.clearSession();
        sessionManager.saveSession("token-123", 17, "user@example.com");
    }

    @After
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    public void scannedQr_loadsProductAndShowsProductInfo() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 5;
        product.productName = "Notebook";
        product.price = 4.25;
        product.productDescription = "A5 spiral";
        product.quantity = 14;
        product.qrCode = "QR-123";

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-123"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("QR-123");

            TextView productTitle = findViewByIdName(activity, "productTitle");
            TextView productDetails = findViewByIdName(activity, "productDetails");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");

            assertEquals("Notebook", productTitle.getText().toString());
            assertTrue(productDetails.getText().toString().contains("14"));
            assertTrue(productDetails.getText().toString().contains("4.25"));
            assertTrue(confirmButton.isEnabled());
        }
    }

    @Test
    public void submitSale_afterProductLoad_sendsSaleRequest() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);
        @SuppressWarnings("unchecked")
        Call<Void> createSaleCall = (Call<Void>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 9;
        product.productName = "Marker";
        product.price = 2.0;
        product.productDescription = "Blue";
        product.quantity = 50;
        product.qrCode = "QR-999";

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-999"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        when(api.createSale(eq("Bearer token-123"), any(ApiModels.SaleRequest.class))).thenReturn(createSaleCall);
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onResponse(createSaleCall, Response.success(null));
            return null;
        }).when(createSaleCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("QR-999");

            EditText quantityInput = findViewByIdName(activity, "quantityInput");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");

            quantityInput.setText("3");
            confirmButton.performClick();

            ArgumentCaptor<ApiModels.SaleRequest> requestCaptor = ArgumentCaptor.forClass(ApiModels.SaleRequest.class);
            verify(api).createSale(eq("Bearer token-123"), requestCaptor.capture());

            ApiModels.SaleRequest request = requestCaptor.getValue();
            assertEquals(9, request.productId);
            assertEquals(17, request.ownerId);
            assertEquals(3, request.quantity);
            assertEquals("MOBILE_APP", request.saleLocation);
            assertEquals("SALE", request.saleType);
        }
    }

    @Test
    public void productNotFound_showsErrorAndDisablesConfirmation() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);

        when(api.getProductByQr(eq("Bearer token-123"), eq("MISSING-QR"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.error(404, okhttp3.ResponseBody.create(null, "")));
            return null;
        }).when(getProductCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("MISSING-QR");

            TextView productTitle = findViewByIdName(activity, "productTitle");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");

            assertEquals(getStringByName(activity, "product_not_found_title"), productTitle.getText().toString());
            assertTrue(!confirmButton.isEnabled());
        }
    }

    @Test
    public void blankQr_finishesAndShowsInvalidToast() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, SaleActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, "");

        SaleActivity activity = launchActivity(SaleActivity.class, intent);

        assertTrue(activity.isFinishing());
        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals(getStringByName(activity, "scan_invalid_qr"), toastText);
    }

    @Test
    public void submitSale_withoutConfirmation_doesNotCallApi() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 10;
        product.productName = "Paper";
        product.price = 1.5;
        product.quantity = 60;

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-NO-CONFIRM"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            launchSaleActivityWithQr("QR-NO-CONFIRM");

            verify(api, never()).createSale(eq("Bearer token-123"), any(ApiModels.SaleRequest.class));
        }
    }

    @Test
    public void submitSale_withReturnSelected_sendsReturnType() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);
        @SuppressWarnings("unchecked")
        Call<Void> createSaleCall = (Call<Void>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 11;
        product.productName = "Stapler";
        product.price = 7.0;
        product.quantity = 4;

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-RETURN"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        when(api.createSale(eq("Bearer token-123"), any(ApiModels.SaleRequest.class))).thenReturn(createSaleCall);
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onResponse(createSaleCall, Response.success(null));
            return null;
        }).when(createSaleCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("QR-RETURN");

            EditText quantityInput = findViewByIdName(activity, "quantityInput");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");
            Button returnRadio = findViewByIdName(activity, "returnRadio");

            returnRadio.performClick();
            quantityInput.setText("2");
            confirmButton.performClick();

            ArgumentCaptor<ApiModels.SaleRequest> requestCaptor = ArgumentCaptor.forClass(ApiModels.SaleRequest.class);
            verify(api).createSale(eq("Bearer token-123"), requestCaptor.capture());
            assertEquals("RETURN", requestCaptor.getValue().saleType);
        }
    }

    @Test
    public void submitSale_withZeroQuantity_doesNotCallApi() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 12;
        product.productName = "Tape";
        product.price = 1.0;
        product.quantity = 10;

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-ZERO"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("QR-ZERO");

            EditText quantityInput = findViewByIdName(activity, "quantityInput");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");

            quantityInput.setText("0");
            confirmButton.performClick();

            verify(api, never()).createSale(eq("Bearer token-123"), any(ApiModels.SaleRequest.class));
        }
    }

    @Test
    public void submitSale_success_showsResultMessageDialog() {
        AventoryApi api = mock(AventoryApi.class);
        @SuppressWarnings("unchecked")
        Call<ApiModels.ProductScanResponse> getProductCall = (Call<ApiModels.ProductScanResponse>) mock(Call.class);
        @SuppressWarnings("unchecked")
        Call<Void> createSaleCall = (Call<Void>) mock(Call.class);

        ApiModels.ProductScanResponse product = new ApiModels.ProductScanResponse();
        product.productId = 13;
        product.productName = "Folder";
        product.price = 3.0;
        product.quantity = 15;

        when(api.getProductByQr(eq("Bearer token-123"), eq("QR-SUCCESS"))).thenReturn(getProductCall);
        doAnswer(invocation -> {
            Callback<ApiModels.ProductScanResponse> callback = invocation.getArgument(0);
            callback.onResponse(getProductCall, Response.success(product));
            return null;
        }).when(getProductCall).enqueue(any());

        when(api.createSale(eq("Bearer token-123"), any(ApiModels.SaleRequest.class))).thenReturn(createSaleCall);
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onResponse(createSaleCall, Response.success(null));
            return null;
        }).when(createSaleCall).enqueue(any());

        try (MockedStatic<ApiClient> mockedApiClient = mockStatic(ApiClient.class)) {
            mockedApiClient.when(ApiClient::getApi).thenReturn(api);

            SaleActivity activity = launchSaleActivityWithQr("QR-SUCCESS");

            EditText quantityInput = findViewByIdName(activity, "quantityInput");
            Button confirmButton = findViewByIdName(activity, "confirmSaleButton");
            quantityInput.setText("1");
            confirmButton.performClick();

            android.app.AlertDialog latestDialog = ShadowAlertDialog.getLatestAlertDialog();
            assertTrue(latestDialog != null && latestDialog.isShowing());
            assertEquals(
                    getStringByName(activity, "sale_success_message"),
                    latestDialog.findViewById(android.R.id.message) == null
                            ? ""
                            : ((TextView) latestDialog.findViewById(android.R.id.message)).getText().toString()
            );
        }
    }

    private SaleActivity launchSaleActivityWithQr(String qrCode) {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, SaleActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, qrCode);

        return launchActivity(SaleActivity.class, intent);
    }

    private <T extends Activity> T launchActivity(Class<T> activityClass, Intent intent) {
        return Robolectric.buildActivity(activityClass, intent)
                .create()
                .start()
                .resume()
                .visible()
                .get();
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewByIdName(Activity activity, String idName) {
        int id = activity.getResources().getIdentifier(idName, "id", activity.getPackageName());
        if (id == 0) {
            throw new IllegalStateException("View id not found: " + idName);
        }
        return (T) activity.findViewById(id);
    }

    private String getStringByName(Activity activity, String name) {
        int id = activity.getResources().getIdentifier(name, "string", activity.getPackageName());
        if (id == 0) {
            throw new IllegalStateException("String not found: " + name);
        }
        return activity.getString(id);
    }
}
