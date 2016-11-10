package a1stgroup.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class ListActivity extends AppCompatActivity {

    static MarkerData selectedMarkerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final ListAdapter myAdapter = new MyCustomizedAdapter(this, MapsActivity.markerDataList);

        ListView myListView = (ListView) findViewById(R.id.idOfListView);

        myListView.setAdapter(myAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

              //  String pickedWord = "You touched " + String.valueOf(adapterView.getItemAtPosition(i));
              //  Toast.makeText(ListActivity.this, pickedWord, Toast.LENGTH_LONG).show();

                selectedMarkerData = (MarkerData) myAdapter.getItem(i);

                Toast.makeText(ListActivity.this, "Alarm Set: " + selectedMarkerData.getName(), Toast.LENGTH_SHORT).show();
              //  Toast.makeText(ListActivity.this, "Latitude: " + selectedMarkerData.getLatitude(), Toast.LENGTH_SHORT).show();
              //  Toast.makeText(ListActivity.this, "Longitude: " + selectedMarkerData.getLongitude(), Toast.LENGTH_SHORT).show();

                Intent myIntent = new Intent(ListActivity.this, MapsActivity.class);
                startActivity(myIntent);
            }
        });

        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
    }
}