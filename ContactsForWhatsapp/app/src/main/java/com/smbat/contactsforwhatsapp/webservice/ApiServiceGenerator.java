package com.smbat.contactsforwhatsapp.webservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiServiceGenerator {

    private static final String TAG = ApiServiceGenerator.class.getSimpleName();
    private static final String BASE_URL = "http://botqa.live:8400/";
    private static final long CONNECT_TIME_OUT = 2;

    private static Retrofit retrofit;

    private ApiServiceGenerator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates API service for passed client interface by base url
     */
    public static <S> S createService(final Class<S> serviceClass) {
        Gson x = new GsonBuilder().setLenient().create();
        final Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getUnsafeOkHttpClient("token"))
                        .addConverterFactory(GsonConverterFactory.create(x));
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

    /**
     * Gets built retrofit object
     *
     * @return the retrofit object
     */
    public static Retrofit getRetrofitObj() {
        return retrofit;
    }

    /* Helper Methods */

    /**
     * Gets unsafe okHttp client by given access token
     *
     * @param accessTokenToPass the access token for Bearer authorization
     * @return the unsafe okHttp client
     */
    private static OkHttpClient getUnsafeOkHttpClient(final String accessTokenToPass) {
        try {
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManagers(), new SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) getTrustManagers()[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            builder.addInterceptor(getAuthorization(accessTokenToPass));
            builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.MINUTES);
            builder.retryOnConnectionFailure(true);
            return builder.build();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            throw new RuntimeException();
        }
    }

    /**
     * Gets trust managers array that does not validate certificate chains
     *
     * @return trust managers array
     */
    @NonNull
    private static TrustManager[] getTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * Gets authorization interceptor by given access token
     *
     * @param accessTokenToPass the access token to pass with Bearer
     * @return the authorization interceptor
     */
    @NonNull
    private static Interceptor getAuthorization(final String accessTokenToPass) {
        return new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                final Request authorisedRequest = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Authorization", "Bearer " + accessTokenToPass)
                        .build();
                return chain.proceed(authorisedRequest);
            }
        };
    }

}
