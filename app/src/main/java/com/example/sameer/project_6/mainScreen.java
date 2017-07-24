package com.example.sameer.project_6;

/**
 * Created by sameer on 5/28/2017.
 */
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

public class mainScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,View.OnClickListener{
    public static GoogleApiClient client;
    public static LatLng origin,dest;
    public static String address,start,end;
    public Button bu;
    public TextView source,destination;
    public  static int pressed;
    double s1,d1,s2,d2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);

      //  FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        bu=(Button)findViewById(R.id.navigation);
        try {
            bu.setOnClickListener(this);
        }catch (Exception e){}
            client= new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0,  this)
                .addApi(Places.GEO_DATA_API)
                .build();
        source=(TextView)findViewById(R.id.source);
        destination=(TextView)findViewById(R.id.destination);
        source.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pressed=1;

                try {
                    Intent intent =
                            new PlaceAutocomplete
                                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(mainScreen.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {

                }

                return false;
            }

        });
        destination.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    pressed=2;
                    Intent intent =
                            new PlaceAutocomplete
                                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(mainScreen.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {

                }
                return false;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.navigation)
        {
            Intent i=new Intent(mainScreen.this,MapsActivity.class);
            startActivity(i);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.

                Place place = PlaceAutocomplete.getPlace(this, data);

                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());
                if(pressed==1) {
                    start=place.getName()+"";
                    String address1=place.getName() + ",\n" +
                            place.getAddress() + "\n" + place.getPhoneNumber();

                    ((TextView) findViewById(R.id.source))
                            .setText(address1);
                    GeocodingLocation locationAddress = new GeocodingLocation();
                    locationAddress.getAddressFromLocation(start,
                            getApplicationContext(), new GeocoderHandler());
                }
                else
                {

                    address=  place.getAddress()+"";
                    end=address;
                    ((TextView) findViewById(R.id.destination))
                            .setText(place.getName() + ",\n" +
                                    place.getAddress() + "\n" + place.getPhoneNumber());
                    GeocodingLocation locationAddress = new GeocodingLocation();
                    locationAddress.getAddressFromLocation(address,
                            getApplicationContext(), new GeocoderHandler());
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }

        }
    }
}
