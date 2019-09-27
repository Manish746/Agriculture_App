package com.example.myapplication.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DdaOngoingFragment extends Fragment {
    // TODO: Rename and change types and number of parameters
    private static final String TAG = "DdaOngoingFragment";
    private ArrayList<String> Id;
    private ArrayList<String> Name;
    private ArrayList<String> Address;
    private DdaongoingAdapter ddaongoingAdapter;
    private String url_get_ongoing = "http://13.235.100.235:8000/api/locations/dda/ongoing";
    private String next_url_get_ongoing;
    private String token;
    private String villagename;
    private String blockname;
    private String district;
    private String state;
    private LinearLayoutManager layoutManager;
    private boolean isNextBusy = false;
    private int length_of_results_array;
    private RecyclerView review;
    private View view;
    private boolean isRefresh;
//    private Toolbar toolbar;

    public DdaOngoingFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Id = new ArrayList<String>();
        Name = new ArrayList<String>();
        Address = new ArrayList<String>();
        isRefresh = false;

        view = inflater.inflate(R.layout.fragment_ongoing,container,false);
        review = view.findViewById(R.id.recyclerViewongoing);

        ddaongoingAdapter = new DdaongoingAdapter(getActivity(),Id,Name,Address);
        review.setAdapter(ddaongoingAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        review.setLayoutManager(layoutManager);


        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token","");
        Log.d(TAG, "onCreateView: "+token);
        getRequestData();

        review.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalCount, pastCount, visibleCount;
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastCount = layoutManager.findFirstVisibleItemPosition();
                    visibleCount = layoutManager.getChildCount();
                    if ((pastCount + visibleCount) >= totalCount) {
                        if (!next_url_get_ongoing.equals("null") && !isNextBusy)
                            get_ddaongoing();
                        Log.d(TAG, "onScrolled:");
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void getRequestData(){
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url_get_ongoing, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    next_url_get_ongoing = jsonObject.getString("next");
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    length_of_results_array = jsonArray.length();
                    if(length_of_results_array==0){
                        ddaongoingAdapter.showongoingshimmer = false;
                        ddaongoingAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        JSONObject a = c.getJSONObject("ado");
                        Name.add(a.getString("name"));
                        Id.add(c.getString("id"));
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        state = c.getString("state");
                        Address.add(villagename+", "+blockname+", "+district+", "+state);
                    }
                    Log.d(TAG, "onResponse:ongoing ");
                    ddaongoingAdapter.showongoingshimmer = false;
                    ddaongoingAdapter.notifyDataSetChanged();

                }catch (JSONException e){
                    Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error );
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void get_ddaongoing(){
        RequestQueue requestQueue1 = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
            JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(next_url_get_ongoing, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject rootObject = new JSONObject(String.valueOf(response));
                                next_url_get_ongoing = rootObject.getString("next");
                                JSONArray resultsArray = rootObject.getJSONArray("results");
                                length_of_results_array = resultsArray.length();
                                if(length_of_results_array==0){
                                    ddaongoingAdapter.showongoingshimmer = false;
                                    ddaongoingAdapter.notifyDataSetChanged();
                                    view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                                }
                                for (int i = 0; i < resultsArray.length(); i++) {
                                    JSONObject singleObject = resultsArray.getJSONObject(i);
                                    JSONObject a = singleObject.getJSONObject("ado");
                                    Name.add(a.getString("name"));
                                    Id.add(singleObject.getString("id"));
                                    String location = singleObject.getString("village_name") + ", " + singleObject.getString("block_name") + ", "
                                            + singleObject.getString("district") + ", " + singleObject.getString("state");
                                    Address.add(location);
                                    isNextBusy = false;
                                    Log.d(TAG, "onResponse: in next url");
                                }
                                ddaongoingAdapter.showongoingshimmer = false;
                                ddaongoingAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Token " + token);
                    return map;
                }
            };
            requestQueue1.add(jsonObjectRequest1);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        isRefresh = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        isRefresh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (isRefresh) {
            getFragmentManager().beginTransaction().detach(DdaOngoingFragment.this)
                    .attach(DdaOngoingFragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isRefresh = false;
        }
    }

}
