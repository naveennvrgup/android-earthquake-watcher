package com.example.earthquakewatcher.Activities;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakewatcher.Model.EarthQuake;
import com.example.earthquakewatcher.R;
import com.example.earthquakewatcher.UI.CustomInfoWindow;
import com.example.earthquakewatcher.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue requestQueue;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private BitmapDescriptor[] iconColors;
    private Button showListButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListButton = findViewById(R.id.showListBtn);

        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this,ShowListActivity.class));
            }
        });

        requestQueue = Volley.newRequestQueue(this);
        iconColors = new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
//                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
        };

        getEarthQuakes();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    public void getEarthQuakes() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constants.URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        EarthQuake earthQuake = new EarthQuake();

                        try {
                            JSONArray features = response.getJSONArray("features");

                            for (int i = 0; i < Constants.LIMIT; i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lat = coordinates.getDouble(1), lon = coordinates.getDouble(0);

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setLat(lat);
                                earthQuake.setLon(lon);
                                earthQuake.setmagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                DateFormat dateFormat = DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());

                                MarkerOptions markerOptions = new MarkerOptions();

                                markerOptions.icon(iconColors[Constants.randomInt(iconColors.length - 1, 0)]);
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
                                markerOptions.snippet("Magnitude: " + earthQuake.getmagnitude() + "\n" + "Date: " + formattedDate);

                                if(earthQuake.getmagnitude()>=2.0){
                                    CircleOptions circleOptions=new CircleOptions();
                                    circleOptions.center(new LatLng(earthQuake.getLat(),earthQuake.getLon()));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(5f);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                    mMap.addCircle(circleOptions);
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("naveen: ", "info window");
        getQuakeDetails(marker.getTag().toString());
    }

    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String detailsUrl = "";

                        try {
                            JSONObject properties = response.getJSONObject("properties");
                            JSONObject products = properties.getJSONObject("products");
                            JSONArray geoserve = products.getJSONArray("geoserve");

                            for (int i = 0; i < geoserve.length(); i++) {
                                JSONObject geoserveObj = geoserve.getJSONObject(i);
                                JSONObject contentObj = geoserveObj.getJSONObject("contents");
                                JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                                detailsUrl = geoJsonObj.getString("url");
                                getMoreDetails(detailsUrl);
                            }
                            Log.d("naveen:", detailsUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void getMoreDetails(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.popup, null);

                        Button dismissButton = view.findViewById(R.id.dismissPop);
                        Button dismissButtonTop = view.findViewById(R.id.desmissPopTop);
                        TextView popList = view.findViewById(R.id.popList);
                        WebView htmlPop = view.findViewById(R.id.htmlWebview);

                        StringBuilder stringBuilder = new StringBuilder();

                        try {
                            if (response.has("tectonicSummary") && response.getString("tectonicSummary") != null) {
                                JSONObject tectonic = response.getJSONObject("tectonicSummary");

                                if (tectonic.has("text") && tectonic.getString("text") != null) {
                                    String text = tectonic.getString("text");
                                    htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
                                }
                            } else {
                                String text = "<h4>404 no summary found!</h4>";
                                htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
                            }

                            JSONArray cities = response.getJSONArray("cities");

                            for (int i = 0; i < cities.length(); i++) {
                                JSONObject citiesObj = cities.getJSONObject(i);

                                stringBuilder.append("City: " + citiesObj.getString("name")
                                        + "\n" + "Distance: " + citiesObj.getString("distance")
                                        + "\n" + "Population: " + citiesObj.getString("population"));

                                stringBuilder.append("\n\n");
                            }
                            popList.setText(stringBuilder);


                            // dismiss btns
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });


                            alertDialogBuilder.setView(view);
                            alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("naveen: ", "marker");
        return false;
    }
}
