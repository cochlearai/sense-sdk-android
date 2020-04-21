package ai.cochlear.examples.simpleevent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import ai.cochlear.sdk.core.Cochl;
import ai.cochlear.sdk.models.Model;
import ai.cochlear.sdk.CochlException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String sdkKey = "<ENTER YOUR SDK KEY HERE>";

    /** For realtime audio stream recording*/
    AudioRecord record = null;

    /** AudioRecord parameters */
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int SAMPLE_RATE = 22050;
    private static final int RECORD_BUF_SIZE = SAMPLE_RATE  * 4 * 2;

    /** Sound event textView */
    TextView soundEventView = null;

    /** Handler for textView update */
    private final static int UPDATE_MESSAGE = 0x1000;
    private final Handler textHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final int what = msg.what;

            if(what == UPDATE_MESSAGE) {
                soundEventView.setText((String)msg.obj);
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request App Permissions
        requestPermission();

        soundEventView = findViewById(R.id.soundEventTextView);

        String serviceName = getString(R.string.service_name);
        TextView serviceView = findViewById(R.id.serviceTextView);
        serviceView.append(serviceName);


        // Start the sound prediction
        Cochl cochl = Cochl.getInstance();
        cochl.init(getApplicationContext(), sdkKey);

        record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE);

        final Model model = cochl.getModel(serviceName);
        model.addInput(record);

        // Set the callback function that is called after prediction is done
        model.predict(new Model.OnPredictListener() {
            @Override
            public void onPredictDone(JSONObject result) {
                try {
                    JSONObject results = result.getJSONObject("result");
                    JSONArray frames = results.getJSONArray("frames");
                    JSONObject frame = frames.getJSONObject(0);

                    String tag = frame.getString("tag");;

                    Message message = Message.obtain();
                    message.obj = tag;
                    message.what = UPDATE_MESSAGE;
                    textHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(CochlException error) {
                Log.d(LOG_TAG, error.toString());
            }
        });

    }

    private boolean requestPermission() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.RECORD_AUDIO,
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
}