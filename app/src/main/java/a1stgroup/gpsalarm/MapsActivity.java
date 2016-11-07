package a1stgroup.gpsalarm;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap myGoogleMap;
    GoogleApiClient myGoogleApiClient;
    Marker myMarker;    // Separate Marker object to allow operations with it.
    Circle myCircle;
    int alarmRadius;    // Used by markers. Can now be set through preferences.
    MediaPlayer mySound;
    static String ringtonePath;
    LocationRequest myLocationRequest;  // Global variable for requesting location
    public static final double earthRadius = 6372.8; // Radius of Earth, in kilometers
    private boolean stop = false;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
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

            // mySound = MediaPlayer.create(this, R.raw.annoying);      // Promoted to method initSound() to function with preferences.

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            setAlarmRadius(Integer.parseInt(prefs.getString("alarmRadius", "500")));
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
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(prefListener);

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
            myGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(MapsActivity.this);
                    LatLng coordinates = marker.getPosition();

                    List<Address> list = null;

                    try {
                        list = gc.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address addr = list.get(0);
                    marker.setTitle(addr.getLocality());
                    marker.showInfoWindow();                    // This is needed in case InfoWindow is already open before moving the marker.
                }
            });

            myGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng point) {
                    if (myMarker != null) {
                        myGoogleMap.clear();
                    }

                    setMarker("Location", point.latitude, point.longitude);
                    /*myMarker = myGoogleMap.addMarker(new MarkerOptions()
                            .position(point)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    myCircle = drawCircle(point);*/
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

    private void zoom(float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(zoom);
        myGoogleMap.moveCamera(cameraUpdate);
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
        String locality = address.getLocality();                        // Locality here just for demonstartion purposes.

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocationZoom(lat, lng, 15);

        setMarker(locality, lat, lng);
    }

    private void setMarker(String locality, double lat, double lng) {
        if (myMarker != null) {                                      // If marker marker has a reference, remove it.
            removeEverything();
        }

        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(true)
                .title(locality)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))      // Here it is possible to specify custom icon design.
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
            case R.id.menuItemSettings:
                // Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

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
        myLocationRequest.setInterval(4000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // /to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
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

    public void detectRadius() {
        double lat = myGoogleMap.getMyLocation().getLatitude();             // Deprecated. Possible solution: http://stackoverflow.com/questions/23104089/googlemap-getmylocation-cannot-get-current-location
        double lon = myGoogleMap.getMyLocation().getLongitude();
        if (myMarker != null && !stop) {
            if (haversine(lat, lon, myMarker.getPosition().latitude, myMarker.getPosition().longitude) <= myCircle.getRadius() / 1000) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(750);
                mySound.start();
                /*AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MapsActivity.this);
                alertBuilder.setMessage("You have arrived!");
                alertBuilder.show();
                alertBuilder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mySound.release();
                        stop = true;
                        dialog.cancel();
                        removeEverything();
                    }
                });
                alertBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mySound.release();
                        stop = true;
                        dialog.cancel();
                        removeEverything();
                    }
                });*/
            }

        }

    }

    @Override
    public void onLocationChanged(Location location) {          // Called every time user changes location

        if (location == null) {
            Toast.makeText(this, "Can't get current location", Toast.LENGTH_LONG).show();
        } else {
            detectRadius();
        }
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

    public void initSound() {
        mySound = MediaPlayer.create(this, Uri.parse(ringtonePath));
    }

}

