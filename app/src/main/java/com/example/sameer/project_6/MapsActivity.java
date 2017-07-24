package com.example.sameer.project_6;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.sameer.project_6.DirectionsJSONParser.m;

public class  MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private BroadcastReceiver broadcastReceiver;

    public int travel_mode=0;
    public static GoogleMap map;
    private boolean mapsSupported = true;
    ArrayList<LatLng> markerPoints;
    int mMode = 0;
    final int MODE_DRIVING = 0;
    final int MODE_BICYCLING = 1;
    final int MODE_WALKING = 2;
    public Button direct;
    ImageView dir,nav,walk,car,bus;
    //public int[] images = {R.drawable.turn_left, R.drawable.turn_right, R.drawable.turn_sharp_right, R.drawable.turn_slight_right, R.drawable.turn_sharp_left, R.drawable.turn_slight_left, R.drawable.striaght, R.drawable.u_turn};

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        TextView start=(TextView)findViewById(R.id.start);
        TextView end=(TextView)findViewById(R.id.end);
        start.setText(mainScreen.start);
        end.setText(mainScreen.end);
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);
        }
        context = this;
        dir=(ImageView)findViewById(R.id.dir);
        nav=(ImageView)findViewById(R.id.nav);
        walk=(ImageView)findViewById(R.id.walk);
        car=(ImageView)findViewById(R.id.car);
        bus=(ImageView)findViewById(R.id.bus);
        dir.setOnClickListener(this);
        nav.setOnClickListener(this);
        walk.setOnClickListener(this);
        car.setOnClickListener(this);
        bus.setOnClickListener(this);



//        map.setMyLocationEnabled(true);
        // Setting onclick event listener for the m
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode="";
        if(travel_mode==1) {
            mode = "mode=walking";
            mMode = 2;
        }
        else if(travel_mode==2)
        {
            mMode=0;
            mode="mode=driving";
        }
        else if(travel_mode==3){
            mode="mode=driving";
            mMode=0;
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.e("Tag", "" + data + "\n");
            br.close();

        } catch (Exception e) {
            //Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Manipulates the map once available.
     */


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nav) {

            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(mainScreen.origin.latitude, mainScreen.origin.longitude))
                    .bearing(20).tilt(65.5f).zoom(18f).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            Intent i = new Intent(getApplicationContext(), GPS_Service.class);
            startService(i);
        }
        else if (v.getId() == R.id.dir)
        {
            called(MapsActivity.this);
        }
        else if (v.getId() == R.id.walk)
        {
            LatLng origin = mainScreen.origin;
            LatLng dest = mainScreen.dest;
            String url = getDirectionsUrl(origin, dest);
            travel_mode=1;
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        } else if (v.getId() == R.id.car) {
            LatLng origin = mainScreen.origin;
            LatLng dest = mainScreen.dest;
            String url = getDirectionsUrl(origin, dest);
                travel_mode=2;
                DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        } else if (v.getId() == R.id.bus) {
            LatLng origin = mainScreen.origin;
            LatLng dest = mainScreen.dest;
            String url = getDirectionsUrl(origin, dest);
            travel_mode=2;
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        }
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                //Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            LatLng origin = mainScreen.origin;
            LatLng dest = mainScreen.dest;
            map.clear();
            map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(mainScreen.origin).title("origin"));
            //map.addMarker(new MarkerOptions().position(origin).title("origin"));
            map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(mainScreen.dest).title("destination"));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(dest);
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);


            //map.moveCamera(CameraUpdateFactory.newLatLng(mainScreen.dest));
            map.moveCamera(cu);
            map.animateCamera(cu);





            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);

                // Changing the color polyline according to the mode
                if (mMode == MODE_DRIVING)
                    lineOptions.color(Color.RED);
                else if (mMode == MODE_BICYCLING)
                    lineOptions.color(Color.GREEN);
                else if (mMode == MODE_WALKING)
                    lineOptions.color(Color.BLUE);
            }

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }

    public void onBackPressed() {
        for (int i = 0; i <= m; i++) {
            DirectionsJSONParser.offsets[i] = "";
        }
        DirectionsJSONParser.a = 0;
        m = 0;

            Intent i1 = new Intent(getApplicationContext(),GPS_Service.class);
            stopService(i1);

        Intent i = new Intent(this, mainScreen.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void called(Context context) {
        final Dialog dialog = new Dialog(MapsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        Button close = (Button) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ListView listView = (ListView) dialog.findViewById(R.id.listView);
        ArrayList<String> aa = new ArrayList<String>();
        for (int i = 0; i < m; i++) {
            aa.add(DirectionsJSONParser.jHtml[i]);
        }
        ArrayAdapter<String> adpat = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_list_item_1, aa);

        listView.setAdapter(adpat);
        DisplayMetrics displayMetrics = MapsActivity.this.getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        dialog.show();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) throws IllegalArgumentException {
        map = googleMap;

        LatLng origin = mainScreen.origin;
        LatLng dest = mainScreen.dest;/*
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);*/

        //MapController mapControll;
        if(map!=null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                        1);
            }

            map.setMyLocationEnabled(true);
        }
        if(origin!=null&&dest!=null) {
            map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(origin).title("origin"));
            //map.addMarker(new MarkerOptions().position(origin).title("origin"));
            map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(dest).title("destination"));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(dest);
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);


            //map.moveCamera(CameraUpdateFactory.newLatLng(mainScreen.dest));
            map.moveCamera(cu);
            map.animateCamera(cu);
            // Getting URL to the Google Directions API
        }
   }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

}
