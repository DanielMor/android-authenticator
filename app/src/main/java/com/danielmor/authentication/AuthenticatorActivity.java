package com.danielmor.authentication;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bixo on 17/03/2016.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private final String TAG = this.getClass().getSimpleName();

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        mAccountManager = AccountManager.get(getBaseContext());

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

        if (accountName != null) {
            ((TextView)findViewById(R.id.account_name)).setText(accountName);
        }
        /*
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });/*
        findViewById(R.id.signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
                // and return them in setAccountAuthenticatorResult(). See finishLogin().
                Intent signup = new Intent(getBaseContext(), SignUpActivity.class);
                signup.putExtras(getIntent().getExtras());
                startActivityForResult(signup, REQ_SIGNUP);
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void submit(View view) {

        final String userName = ((TextView) findViewById(R.id.username)).getText().toString();
        final String userPass = ((TextView) findViewById(R.id.password)).getText().toString();
        String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
        if(accountType == null) {
            accountType = AccountGeneral.ACCOUNT_TYPE;
        }

        LoginTask task = new LoginTask(userName, userPass, accountType);
        task.execute();
    }

    private void finishLogin(Intent intent) {
        Log.d("udinic", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String type = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        if(type == null) {
            type = AccountGeneral.ACCOUNT_TYPE;
        }
        final Account account = new Account(accountName, type);

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("udinic", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d("udinic", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private class LoginTask extends AsyncTask<String, Void, Intent> {

        private String username;
        private String password;
        private String accountType;

        public LoginTask(String username, String password, String accountType){
            this.username = username;
            this.password = password;
            this.accountType = accountType;
        }

        @Override
        protected Intent doInBackground(String... params) {

            Log.d("udinic", TAG + "> Started authenticating");

            String authtoken = null;
            Bundle data = new Bundle();
            try {
                authtoken = ServerAuthenticate.userSignIn(username, password, accountType);

                data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                data.putString(PARAM_USER_PASS, password);

            } catch (Exception e) {
                data.putString(KEY_ERROR_MESSAGE, e.getMessage());
            }

            final Intent res = new Intent();
            res.putExtras(data);
            return res;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
            } else {
                finishLogin(intent);
            }
        }
    }
}
