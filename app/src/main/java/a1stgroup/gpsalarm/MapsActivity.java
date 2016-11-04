package a1stgroup.gpsalarm;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap myGoogleMap;

    GoogleApiClient myGoogleApiClient;
   // private android.location.LocationListener locationListener;
    //LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final LocationManager manager = (LocationManager) getSystemService( LOCATION_SERVICE );


        super.onCreate(savedInstanceState);
        Toast.makeText(this, "ONCREATEE", Toast.LENGTH_LONG).show();
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(this, "so fucking disabled", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            // Call your Alert message
        }





        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_maps);
            initMap();
        } else {
            // No Google Maps Supported layout.
        }

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
        Toast.makeText(this, "ready", Toast.LENGTH_LONG).show();
        myGoogleMap = googleMap;

        if (myGoogleMap != null) {
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
            myGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener( ){
                @Override
                public void onMapLongClick(LatLng point) {
                    myGoogleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);

            }
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

    Marker myMarker;                                                      // Separate Marker object to allow operations with it.

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);                               // Takes any string and converts to latitude / longitude.

        List<Address> list = gc.getFromLocationName(location, 1);       // 1 result
        Address address = list.get(0);                                  // This object is filled with lots of information.
        String locality = address.getLocality();                        // Locality here just for demonstartion purposes.

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocationZoom(lat, lng, 15);

        setMarker(locality, lat, lng);

    }

    Circle myCircle;


    private void setMarker(String locality, double lat, double lng) {
        if(myMarker != null) {                                      // If marker marker has a reference, remove it.
            removeEverything();
        }

        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))      // Here it is possible to specify custom icon design.
                .position(new LatLng(lat, lng));

        myMarker = myGoogleMap.addMarker(options);

        myCircle = drawCircle(new LatLng(lat, lng));
    }

    private Circle drawCircle(LatLng latLng) {

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(1000)
                .fillColor(0x33FF0000)              // 33 for alpha (transparency)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        return myGoogleMap.addCircle(circleOptions);
    }

    private void removeEverything() {
        myMarker.remove();
        myMarker = null;        // To save some space
        myCircle.remove();
        myCircle = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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


    LocationRequest myLocationRequest;                          // Global variable for requesting location


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Connect", Toast.LENGTH_LONG).show();
        myLocationRequest = LocationRequest.create();
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
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

    @Override
    public void onLocationChanged(Location location) {          // Called every time user changes location

        if(location == null) {
            Toast.makeText(this, "Can't get current location", Toast.LENGTH_LONG).show();
        } else {
           // Toast.makeText(this, "Fu location", Toast.LENGTH_LONG).show();


            //CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 15);
            //myGoogleMap.animateCamera(camUpdate);
            //Toast.makeText(this, "Location Updated", Toast.LENGTH_LONG).show();

        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
        case 10:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
            }
            return;
        }
    }


}
