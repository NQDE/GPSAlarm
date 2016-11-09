package a1stgroup.gpsalarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListAdapter myAdapter = new MyCustomizedAdapter(this, MapsActivity.markerDataList);

        ListView myListView = (ListView) findViewById(R.id.idOfListView);

        myListView.setAdapter(myAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String pickedWord = "You touched " + String.valueOf(adapterView.getItemAtPosition(i));
                Toast.makeText(ListActivity.this, pickedWord, Toast.LENGTH_LONG).show();
            }
        });
    }
}

