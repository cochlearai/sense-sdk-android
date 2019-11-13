package ai.cochlear.sdk.examples.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FILENAME = "new_labels2.txt";
    private List<String> labels = new ArrayList<String>();
    private ArrayList<String> displayedLabels = new ArrayList<>(40);
    final static String displayed = "displayed_labels";
    private boolean[] checked_list;

    private final String sdkKey = "ENTER YOUR SDK KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LinearLayout scrollView = (LinearLayout) findViewById(R.id.scrollView);
        Log.i(LOG_TAG, "Reading labels from: " + FILENAME);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(FILENAME)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        // Build a list view based on these labels.

        checked_list = new boolean[displayedLabels.size()];

        for(int i = 0; i<displayedLabels.size(); i++){

            String label = displayedLabels.get(i);


            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.custom_textview, null);

            tv.setText(label);


            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = scrollView.indexOfChild(view);

                    checked_list[index] = !checked_list[index];
                    if(true==checked_list[index])
                        view.setBackgroundColor(Color.CYAN);
                    else
                        view.setBackgroundColor(Color.WHITE);

                }
            });

            scrollView.addView(tv, i);
            requestPermission();
        }

        Button btDone = (Button)findViewById(R.id.done);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EventActivity.class);
                intent.putExtra("checked_list", checked_list);
                intent.putStringArrayListExtra(displayed, displayedLabels);
                startActivity(intent);
            }
        });

        new SenseSDKTask(getApplicationContext()).execute(sdkKey);
    }

    private boolean requestPermission() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            return false;
        }

        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        SenseSDKWrapper.getInstance().stop();
    }

    private class SenseSDKTask extends AsyncTask<String, Void, Void> {
        private Context context = null;
        SenseSDKTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            SenseSDKWrapper sense = SenseSDKWrapper.getInstance();
            sense.init(this.context, sdkKey);
            return null;
        }
    }
}
