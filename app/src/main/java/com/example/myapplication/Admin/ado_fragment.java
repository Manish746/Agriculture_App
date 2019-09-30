package com.example.myapplication.Admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
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

public class ado_fragment extends Fragment {
    private ArrayList<String> username;
    private ArrayList<String> userinfo;
    private ArrayList<String> mUserId;
    private ArrayList<String> mPkList;
    private ArrayList<String> mDdoNames;
    private String mUrl = "http://13.235.100.235/api/users-list/ado/";
    private RecyclerViewAdater recyclerViewAdater;
    private String token;
    private String nextUrl;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;
    private boolean isNextBusy = false;
    private View view;
    private boolean isRefresh;
    private final String TAG = "ado_fragment";
    private SwipeRefreshLayout swipeRefreshLayout;

    public ado_fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ado_fragment, container, false);
        Log.d(TAG, "onCreateView: ");
        isRefresh = false;
        username = new ArrayList<>();
        userinfo = new ArrayList<>();
        mUserId = new ArrayList<>();
        mPkList = new ArrayList<>();
        mDdoNames = new ArrayList<>();
        swipeRefreshLayout = view.findViewById(R.id.refreshpull);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(ado_fragment.this)
                        .attach(ado_fragment.this).commit();
            }
        });
        progressBar = view.findViewById(R.id.ado_list_progressbar);
        recyclerViewAdater = new RecyclerViewAdater(getActivity(), username, userinfo, mUserId, false,
                mPkList, mDdoNames);
        RecyclerView Rview = view.findViewById(R.id.recyclerViewado);
        Rview.setAdapter(recyclerViewAdater);
        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        layoutManager = new LinearLayoutManager(getActivity());
        Rview.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        Rview.addItemDecoration(divider);

        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    Log.d(TAG, "onResponse: " + nextUrl);
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    if(resultsArray.length()== 0){
                        recyclerViewAdater.mShowShimmer = false;
                        recyclerViewAdater.notifyDataSetChanged();

                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                        //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                    }
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        JSONObject authObject = singleObject.getJSONObject("auth_user");
                        String pk = authObject.getString("pk");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }
                    recyclerViewAdater.mShowShimmer = false;
                    recyclerViewAdater.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: JSON" + e);
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: " + error);
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
        Rview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + totalCount + " " + pastItemCount + " " + visibleItemCount);
                        if (!nextUrl.equals("null") && !isNextBusy)
                            getNextAdos();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void getNextAdos() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        Log.d(TAG, "getNextAdos: count ");
        progressBar.setVisibility(View.VISIBLE);
        final JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, nextUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        String pk = singleObject.getString("id");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }
                    Log.d(TAG, "onResponse: " + username);
                    recyclerViewAdater.notifyDataSetChanged();
                    isNextBusy = false;

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
                isNextBusy = false;
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonArrayRequest);
        requestFinished(requestQueue);

    }

    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        isRefresh = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (isRefresh) {
            getFragmentManager().beginTransaction().detach(ado_fragment.this)
                    .attach(ado_fragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isRefresh = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        isRefresh = true;
    }

}
