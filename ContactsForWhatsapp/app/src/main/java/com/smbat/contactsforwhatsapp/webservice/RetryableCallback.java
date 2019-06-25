package com.smbat.contactsforwhatsapp.webservice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetryableCallback<T> implements Callback<T> {

    private static final String TAG = RetryableCallback.class.getSimpleName();

    private final int totalRetries;
    private final Context context;
    private final Call<T> call;

    private int retryCount = 0;
    private int retryRefreshCount = 0;

    RetryableCallback(final Context context, final Call<T> call, final int totalRetries) {
        this.call = call;
        this.totalRetries = totalRetries;
        this.context = context;
    }

    @Override
    public void onResponse(@NonNull final Call<T> call, @NonNull final Response<T> response) {
        if (ApiHelper.isCallSuccess(response)) {
            onFinalResponse(call, response);
        } else {
            if (response.code() == 401) {
                // empty
            } else {
                if (retryCount++ < totalRetries) {
                    Log.v(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
                    retry();
                } else {
                    onFinalResponse(call, response);
                }
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (retryCount++ < totalRetries) {
            Log.v(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
            retry();
        } else
            onFinalFailure(call, t);
    }

    public void onFinalResponse(final Call<T> call, final Response<T> response) {
        // empty implementation
    }

    public void onFinalFailure(final Call<T> call, final Throwable t) {
        // empty implementation
    }

    private void retry() {
        call.clone().enqueue(this);
    }
}