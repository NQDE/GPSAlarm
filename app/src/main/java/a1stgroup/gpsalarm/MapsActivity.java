package a1stgroup.gpsalarm;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnInfoWindowLongClickListener {

    GoogleMap myGoogleMap;
    GoogleApiClient myGoogleApiClient;
    Marker myMarker;    // Separate Marker object to allow operations with it.
    Circle myCircle;
    int alarmRadius;    // Used by markers. Can now be set through preferences.
    MediaPlayer mySound;
    static String ringtonePath;
    LocationRequest myLocationRequest;  // Global variable for requesting location
    static long locationUpdateFrequency;
    public static final double earthRadius = 6372.8; // Radius of Earth, in kilometers
    private boolean stop = false;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    static ArrayList<MarkerData> markerDataList = new ArrayList<>();
    TrackerAlarmReceiver alarm = new TrackerAlarmReceiver();
    private boolean destinationReached  = false;
    private PopupWindow pw;
    Button closePopUp;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //protected void Pause(View view) {
    //  mySound.stop();
    //   mySound.release();
    ///}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_maps);
            initMap();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            setAlarmRadius(Integer.parseInt(prefs.getString("alarmRadius", "500")));
            setLocationUpdateFrequency(Long.parseLong(prefs.getString("locationUpdateFrequency", "10000")));
            ringtonePath = prefs.getString("alarmRingtone", DEFAULT_ALARM_ALERT_URI.toString());
            initSound();


            prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                    if (key.equals("mapType")) {
                        changeMapType(prefs.getString(key, "2"));
                    }
                    if (key.equals("alarmRadius")) {
                        setAlarmRadius(Integer.parseInt(prefs.getString(key, "500")));
                        removeEverything();
                    }
                    if (key.equals("alarmRingtone")) {
                        ringtonePath = prefs.getString("alarmRingtone", DEFAULT_ALARM_ALERT_URI.toString());
                        initSound();
                    }
                    if (key.equals("locationUpdateFrequency")) {
                        setLocationUpdateFrequency(Long.parseLong(prefs.getString("locationUpdateFrequency", "10000")));
                    }
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(prefListener);
            Intent mapIntent = new Intent(this, TrackerAlarmReceiver.class);

            alarm.setAlarm(MapsActivity.this);

            try {
                markerDataList = (ArrayList<MarkerData>) InternalStorage.readObject(this, "myFile"); // Retrieve the list from internal storage
            } catch (IOException e) {
                Log.e("File Read error: ", e.getMessage());
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Failed to retrieve list from file", Toast.LENGTH_SHORT).show();
                Log.e("File Read error: ", e.getMessage());
            }

        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }


    private void initMap() {
        MapFragment myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        myMapFragment.getMapAsync(this);            // Previously getMap

    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);      // Can return 3 different values

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play services!", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;

        if (myGoogleMap != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            changeMapType(prefs.getString("mapType", "2"));

            myGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng point) {
                    if (myMarker != null) {
                        myGoogleMap.clear();
                    }

                    double roundedLatitude = Math.round(point.latitude * 100000.0) / 100000.0;
                    double roundedLongitude = Math.round(point.longitude * 100000.0) / 100000.0;

                    setMarker("Location", roundedLatitude, roundedLongitude);

//                     TODO
//                    A robust implementation of getting location information and passing it to a user-added marker.
//                    Our implementation (below) works most of the time, but makes adding markers dependent on web-connectivity
//                    and slow on some devices.
//
//                    Geocoder gc = new Geocoder(MapsActivity.this);
//                    List<Address> list = null;
//                    try {
//                        list = gc.getFromLocation(point.latitude, point.longitude, 1);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (list != null && list.size() > 0) {                           // Prevents from crashing on non-existent results.
//                        myMarker.setTitle(list.get(0).getLocality());
//                    }

                }
            });

            myGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tv_snippet);



                    LatLng coordinates = marker.getPosition();

                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + coordinates.latitude);
                    tvLng.setText("Longitude: " + coordinates.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

            myGoogleMap.setOnInfoWindowLongClickListener(this);

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        myGoogleMap.setMyLocationEnabled(true);

        myGoogleApiClient = new GoogleApiClient.Builder(this)       // This code is for updating current location
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        myGoogleApiClient.connect();

        if (ListActivity.selectedMarkerData != null) {
            setMarker(ListActivity.selectedMarkerData.getName(), ListActivity.selectedMarkerData.getLatitude(), ListActivity.selectedMarkerData.getLongitude());
        }

    }



    private void goToLocation(double lat, double lng) {
        LatLng coordinates = new LatLng(lat, lng);
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLng(coordinates);
        myGoogleMap.moveCamera(camUpdate);
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng coordinates = new LatLng(lat, lng);
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, zoom);
        myGoogleMap.moveCamera(camUpdate);
    }

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);                               // Takes any string and converts to latitude / longitude.

        List<Address> list = gc.getFromLocationName(location, 1);       // We choose just 1 result

        if (list.size() < 1) {                                          // Prevents search from crashing on non-existent results.
            Toast.makeText(this, "No such location found. \nTry a different keyword.", Toast.LENGTH_LONG).show();
            et.getText().clear();
            return;
        }

        Address address = list.get(0);                                  // This object is filled with lots of information.
        String locality = address.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        double roundedLat = Math.round(lat * 100000.0) / 100000.0;
        double roundedLng = Math.round(lng * 100000.0) / 100000.0;
        goToLocationZoom(lat, lng, 15);

        setMarker(locality, roundedLat, roundedLng);
    }

    void setMarker(String locality, double lat, double lng) {
        if (myMarker != null) {                                      // If marker has a reference, remove it.
            removeEverything();
        }

        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(true)
                .title(locality)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_marker_40))      // Here it is possible to specify custom icon design.
                .position(new LatLng(lat, lng));

        myMarker = myGoogleMap.addMarker(options);

        myCircle = drawCircle(new LatLng(lat, lng));

        stop = false;
    }

    private Circle drawCircle(LatLng latLng) {

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(alarmRadius)
                .fillColor(0x33FF0000)              // 33 for alpha (transparency)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        return myGoogleMap.addCircle(circleOptions);
    }

    private void removeEverything() {
        if (myMarker != null) {
            myMarker.remove();
            myMarker = null;        // To save some space
            if (myCircle != null) {
                myCircle.remove();
                myCircle = null;        // memory saving
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);         // More on this line: http://stackoverflow.com/questions/10303898/oncreateoptionsmenu-calling-super
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuItemMyAlarms:
                Intent i = new Intent(this, ListActivity.class);
                startActivity(i);
                return true;
            case R.id.menuItemSettings:
                Intent j = new Intent(this, MyPreferencesActivity.class);
                startActivity(j);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onInfoWindowLongClick(Marker marker) {
        // Toast.makeText(this, "Info Window long click", Toast.LENGTH_SHORT).show();

        View myView = (LayoutInflater.from(this)).inflate(R.layout.dialog_inputname, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(myView);
        final EditText userInput = (EditText) myView.findViewById(R.id.etxtInputName);

        alertBuilder.setCancelable(true)
                .setTitle("Save Alarm")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = userInput.getText().toString();

                        if (TextUtils.isEmpty(name) || TextUtils.getTrimmedLength(name) < 1) {
                            Toast.makeText(MapsActivity.this, "Empty name not allowed. \nPlease try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (MarkerData markerData : markerDataList) {
                            if (markerData.getName().equals(name)) {
                                Toast.makeText(MapsActivity.this, "Duplicate name not allowed. \nPlease try again with a unique name.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        addMarkerDataToList(name);
                        myMarker.hideInfoWindow();
                    }
                });
        Dialog myDialog = alertBuilder.create();
        myDialog.show();
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = LocationRequest.create();
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* TODO
        Change priority to balanced
         */
        myLocationRequest.setInterval(locationUpdateFrequency);
        myLocationRequest.setFastestInterval(locationUpdateFrequency / 4);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
    }

    protected void trackLocation() {
        myLocationRequest = LocationRequest.create();
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocationRequest.setInterval(100);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }

    public void detectRadius(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        if (myMarker != null && !stop) {
            if (haversine(lat, lon, myMarker.getPosition().latitude, myMarker.getPosition().longitude) <= myCircle.getRadius() / 1000) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
                mySound.seekTo(0);
                mySound.start();
                if (!destinationReached) {
                    showPopup();
                }
                destinationReached = true;
            }
        }
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) MapsActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.screen_popup,
                (ViewGroup) findViewById(R.id.popup_element));
        pw = new PopupWindow(layout, 300, 370, true);
        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

        closePopUp = (Button) layout.findViewById(R.id.btn_close_popup);
        closePopUp.setOnClickListener(cancel_button_click_listener);
    }

    private OnClickListener cancel_button_click_listener = new OnClickListener() {
        public void onClick(View v) {
            mySound.pause();
            removeEverything();
            destinationReached = false;
            pw.dismiss();
        }
    };

    @Override
    public void onLocationChanged(Location location) {          // Called every time user changes location

        if (location == null) {
            Toast.makeText(this, "Can't get current location", Toast.LENGTH_LONG).show();
        }
        else
            detectRadius(location);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    public Action getIndexApiAction0() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public void changeMapType(String type) {
        switch (type) {
            case "1":
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case "2":
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "3":
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "4":
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "5":
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    public void setAlarmRadius(int newRadius) {
        alarmRadius = newRadius;
    }

    public void setLocationUpdateFrequency(long newFrequency) {
        locationUpdateFrequency = newFrequency;
    }

    public void initSound() {
        mySound = MediaPlayer.create(this, Uri.parse(ringtonePath));
    }

    public void addMarkerDataToList (String name) {
        MarkerData toBeAdded = new MarkerData();
        toBeAdded.setName(name);
        toBeAdded.setLatitude(myMarker.getPosition().latitude);
        toBeAdded.setLongitude(myMarker.getPosition().longitude);
        if (markerDataList.add(toBeAdded))
            try {
                InternalStorage.writeObject(this, "myFile", markerDataList);
                Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
                Log.e("IOException", e.getMessage());
            }
        else
            Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
    }

}

