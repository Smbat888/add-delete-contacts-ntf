package com.smbat.contactsforwhatsapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smbat.contactsforwhatsapp.webservice.ApiHelper;
import com.smbat.contactsforwhatsapp.webservice.ApiInterface;
import com.smbat.contactsforwhatsapp.webservice.ApiServiceGenerator;
import com.smbat.contactsforwhatsapp.webservice.models.MailingResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE_READ_CONTACTS = 1;

    private static final int PERMISSION_REQUEST_CODE_WRITE_CONTACTS = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<MailingResponse> mailings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPhoneContactsPermission(Manifest.permission.WRITE_CONTACTS)) {
            requestPermission(Manifest.permission.WRITE_CONTACTS,
                    PERMISSION_REQUEST_CODE_WRITE_CONTACTS);
        }
        if (!hasPhoneContactsPermission(Manifest.permission.READ_CONTACTS)) {
            requestPermission(Manifest.permission.READ_CONTACTS,
                    PERMISSION_REQUEST_CODE_READ_CONTACTS);
        }

        // Fetching mailing contacts data
        getMailings();

        // Click this button to save user input phone contact info.
        Button savePhoneContactButton = findViewById(R.id.add_phone_contact_save_button);
        savePhoneContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get android phone contact content provider uri.
                //Uri addContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                // Below uri can avoid java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones error.
                Toast.makeText(getApplicationContext(), "s---->>>>>>", Toast.LENGTH_LONG).show();

                List<MailingResponse> tmpList = mailings.subList(255, 510); // use this substring if required!!

                for (final MailingResponse mailing : tmpList) {
                    Uri addContactsUri = ContactsContract.Data.CONTENT_URI;

                    // Add an empty contact and get the generated id.
                    long rowContactId = getRawContactId();

                    // Add contact name data.
                    String displayName = mailing.getName();
                    insertContactDisplayName(addContactsUri, rowContactId, displayName);

                    // Add contact phone data.
                    String phoneNumber = mailing.getPhone();
                    insertContactPhoneNumber(addContactsUri, rowContactId, phoneNumber, "mobile");
                }

                Toast.makeText(getApplicationContext(), "New contact has been added, go back to previous page to see it in contacts list.", Toast.LENGTH_LONG).show();
            }
        });

        Button deleteContact = findViewById(R.id.delete);
        deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get android phone contact content provider uri.
                List<MailingResponse> tmpList = mailings.subList(0, 255);

                for (final MailingResponse mailing : tmpList) {
                    deleteContactsLike(mailing.getName());
                }
                Toast.makeText(getApplicationContext(), "The contact has been deleted", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean hasPhoneContactsPermission(String permission) {
        boolean ret = false;

        // If android sdk version is bigger than 23 the need to check run time permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // return phone read contacts permission grant status.
            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                ret = true;
            }
        } else {
            ret = true;
        }
        return ret;
    }

    // Request a runtime permission to app user.
    private void requestPermission(String permission, int requestCode) {
        String requestPermissionArray[] = {permission};
        ActivityCompat.requestPermissions(this, requestPermissionArray, requestCode);
    }

    // After user select Allow or Deny button in request runtime permission dialog
    // , this method will be invoked.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int length = grantResults.length;
        if (length > 0) {
            int grantResult = grantResults[0];

            if (grantResult == PackageManager.PERMISSION_GRANTED) {

                if (requestCode == PERMISSION_REQUEST_CODE_READ_CONTACTS) {
                    // If user grant read contacts permission.

                } else if (requestCode == PERMISSION_REQUEST_CODE_WRITE_CONTACTS) {
                    // If user grant write contacts permission then start add phone contact activity.

                }
            } else {
                Toast.makeText(getApplicationContext(), "You denied permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // This method will only insert an empty data to RawContacts.CONTENT_URI
    // The purpose is to get a system generated raw contact id.
    private long getRawContactId() {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }


    // Insert newly created contact display name.
    private void insertContactDisplayName(Uri addContactsUri, long rawContactId, String displayName) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put contact display name value.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);

        getContentResolver().insert(addContactsUri, contentValues);

    }

    private void insertContactPhoneNumber(Uri addContactsUri, long rawContactId, String phoneNumber, String phoneTypeStr) {
        // Create a ContentValues object.
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put phone number value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        // Calculate phone type by user selection.
        int phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;

        if ("home".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        } else if ("mobile".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        } else if ("work".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
        }
        // Put phone type value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneContactType);

        // Insert new contact data into phone contact list.
        getContentResolver().insert(addContactsUri, contentValues);

    }

    private int deleteContactsLike(String name) {
        return getContentResolver().delete(
                ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.Contacts.DISPLAY_NAME
                        + " like ?",
                new String[]{name + '%'});
    }

    private void getMailings() {
        final ApiInterface apiServiceGenerator =
                ApiServiceGenerator.createService(ApiInterface.class);
        if (null == apiServiceGenerator) {
            Log.d(TAG, "Failed to get API service");
            return;
        }

        Log.d(TAG, "Getting mailing contacts data...");
        final retrofit2.Call<List<MailingResponse>> data = apiServiceGenerator
                .getMailings();
        ApiHelper.enqueueWithRetry(getApplicationContext(), data, new Callback<List<MailingResponse>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<MailingResponse>> call,
                                   @NonNull Response<List<MailingResponse>> response) {
                if (null != response.body()) {
                    mailings = response.body();
                    Log.d(TAG, "Contacts data retreived: " + response.body());

                    return;
                }
                Toast.makeText(MainActivity.this,
                        "Error retrieving mailings. Unable to make calls",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<MailingResponse>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Error retrieving mailings. Unable to make calls",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
