package a1stgroup.gpsalarm;

        import android.app.Dialog;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
        import android.view.Menu;
        import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap myGoogleMap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (googleServicesAvailable()) {
            Toast.makeText(this, "Good: Google Services Available!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_maps);
            initMap();
        } else {
            // No Google Maps Supported layout.
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
        goToLocation(57.062133, 24.025366);
        zoom(15);

        LatLng sydney = new LatLng(-34, 151);
        myGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void goToLocation(double lat, double lng) {
        LatLng coordinates = new LatLng(lat, lng);
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLng(coordinates);
        myGoogleMap.moveCamera(camUpdate);
    }


    private void zoom(float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(zoom);
        myGoogleMap.moveCamera(cameraUpdate);
    }

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<android.location.Address> list = gc.getFromLocationName(location, 1);
        android.location.Address address = list.get(0);
        String locality = address.getLocality();
        Toast.makeText(this,locality,Toast.LENGTH_LONG).show();
        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocation(lat, lng);
        zoom(15);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
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
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }



}
