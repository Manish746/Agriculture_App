package com.example.myapplication.Dda;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.layout.simple_spinner_item;

public class DdaselectAdo extends AppCompatActivity {
    private static final String TAG = "DdaselectAdo";
    private ArrayList<String> nameofado;

    private String urlget = "http://13.235.100.235:8000/api/ado/";
    private String token;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddaselect_ado);
        Toast.makeText(this, "List of Ado's", Toast.LENGTH_LONG).show();
        loadSpinnerData(urlget);


        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        Log.d(TAG, "onCreateView: " + token);

    }

    private void loadSpinnerData(String url){

        //make volley request
        nameofado = new ArrayList<String>();
        spinner = (Spinner) findViewById(R.id.listofado);

        RequestQueue requestQueue = Volley.newRequestQueue(this);


        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>  () {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        nameofado.add(c.getString("name"));
                        Log.d(TAG, "onResponse: "+c.getString("name"));
                    }
                    Log.d(TAG, "onResponse: name of ado is fetched");
                    
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            Log.d(TAG, "onItemSelected: item is selected");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(DdaselectAdo.this, simple_spinner_item, nameofado);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    spinner.setAdapter(spinnerArrayAdapter);
                    
                }catch (JSONException e){ e.printStackTrace();}

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonObjectRequest);

    }
}
