package com.smbat.contactsforwhatsapp.webservice;

import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHelper {

    private ApiHelper() {
        throw new UnsupportedOperationException();
    }

    private static final int DEFAULT_RETRIES = 3;

    /**
     * Requests with given retry
     *
     * @param context    the context to access retryable callback
     * @param call       the call to execute
     * @param retryCount the count of request retries
     * @param callback   the given callback
     * @param <T>        generic call data
     */
    static <T> void enqueueWithRetry(final Context context, final Call<T> call,
                                     final int retryCount, final Callback<T> callback) {
        call.enqueue(new RetryableCallback<T>(context, call, retryCount) {

            @Override
            public void onFinalResponse(Call<T> call, Response<T> response) {
                callback.onResponse(call, response);
            }

            @Override
            public void onFinalFailure(Call<T> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    /**
     * Requests with retry (3 times - by default)
     *
     * @param context  the context to access retryable callback
     * @param call     the call to execute
     * @param callback the given callback
     * @param <T>      generic call data
     */
    public static <T> void enqueueWithRetry(final Context context,
                                            final Call<T> call,
                                            final Callback<T> callback) {
        enqueueWithRetry(context, call, DEFAULT_RETRIES, callback);
    }

    /**
     * Checks is call success
     *
     * @param response the response to check
     * @return tru - if success, false - otherwise
     */
    public static boolean isCallSuccess(final Response response) {
        final int code = response.code();
        return (code == 200 || code == 201 || code == 204);
    }
}
