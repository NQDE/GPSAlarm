package a1stgroup.gpsalarm;

        import android.app.Dialog;
        import android.location.Address;
        import android.location.Geocoder;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GoogleApiAvailability;
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

    /*public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);   // Takes any string and converts to latitude / longitude.

        List<Address> list = gc.getFromLocationName(location, 1);
        Address address = list.get(0);      // This object is filled with lots of information.
        String locality = address.getLocality();                  // Locality here just for demonstartion purposes.

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = address.getLatitude();
        double lng = address.getLongitude();
        goToLocation(lat, lng);
        zoom(15);

    }*/

}
