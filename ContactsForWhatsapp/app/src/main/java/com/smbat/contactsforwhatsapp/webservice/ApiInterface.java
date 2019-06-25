package com.smbat.contactsforwhatsapp.webservice;

import com.smbat.contactsforwhatsapp.webservice.models.MailingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface  {

    /**
     * Gets mailings array
     *
     * @return response body of downloaded audio file
     */
    @GET("mailings")
    Call<List<MailingResponse>> getMailings();
}