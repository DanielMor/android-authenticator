package com.danielmor.authentication;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bixo on 17/03/2016.
 */
public class ServerAuthenticate {
    public static String userSignIn(String email, String password, String authTokenType) {
        String token = null;
        try {
            URL url = new URL("http://sioslife-demo.cloudapp.net/api/authenticate");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject cred = new JSONObject();
            cred.put("email",email);
            cred.put("password", password);

            OutputStream os = urlConnection.getOutputStream();
            os.write(cred.toString().getBytes());
            os.flush();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String output = br.readLine();
                JSONObject object = new JSONObject(output);
                token = object.getString("token");
                Log.e("token", token);
            }
            urlConnection.disconnect();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
}
