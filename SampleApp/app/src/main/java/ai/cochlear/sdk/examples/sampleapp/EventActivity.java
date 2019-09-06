package ai.cochlear.sdk.examples.sampleapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ai.cochlear.sdk.CochlException;
import ai.cochlear.sdk.core.Cochl;
import ai.cochlear.sdk.models.Model;

public class EventActivity extends AppCompatActivity {
    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int RECORD_BUF_SIZE = SAMPLE_RATE * 4 * 2;
    boolean isRecording = false;
    AudioRecord record;
    Model model;


    // UI elements.
    private Button backButton;
    private ListView labelsListView;
    private static final String LOG_TAG = EventActivity.class.getSimpleName();

    private ArrayList<String> displayedLabels = new ArrayList<>();
    private List<String> chosen = new ArrayList<>();
    final static String displayed = "displayed_labels";
    private boolean[] checked_list;
    private int[] labelIndexMapper;
    String tag = null;

    private final String sdkKey = "ENTER YOUR SDK KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Cochl cochl = Cochl.getInstance();
        cochl.init(getApplicationContext(), sdkKey);

        labelsListView = (ListView) findViewById(R.id.list_view2);

        Intent intent=new Intent(this.getIntent());
        checked_list = intent.getBooleanArrayExtra("checked_list");
        displayedLabels = intent.getStringArrayListExtra(displayed);
        System.out.println(displayedLabels.get(0) + " "+ displayedLabels.get(1));

        backButton = (Button)findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                EventActivity.this.finish();
            }
        });

        labelIndexMapper = new int[checked_list.length];
        for(int i=0; i<checked_list.length; i++){
            if(true==checked_list[i]){
                labelIndexMapper[i] = chosen.size();
                chosen.add(displayedLabels.get(i));
            }else{
                labelIndexMapper[i] = -1;
            }
        }


        // Build a list view based on these labels.
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.list_text_item, chosen);
        labelsListView.setAdapter(arrayAdapter);


        // Load the TensorFlow model.
        model = cochl.getInstance().getModel("event");

        // Start the recording and recognition threads.
        startRecording();
    }

    public void startRecording() {
        isRecording = true;
        record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE);
        model.addInput(record);

        // Predict audio stream
        model.predict(new Model.OnPredictListener() {
            @Override
            public void onPredictDone(JSONObject result) {
                // Parsing the result
                JSONObject result1 = null;
                try {
                    result1 = result.getJSONObject("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray frames = null;
                try {
                    frames = result1.getJSONArray("frames");
                    //Log.d(LOG_TAG, frames.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject arr = null;
                try {
                    arr = frames.getJSONObject(0);
                    //Log.d(LOG_TAG, frames.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    tag = arr.getString("tag");
                    Log.d(LOG_TAG, tag);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                for (int i = 0; i < chosen.size(); i++) {
                                    if(chosen.get(i).equals(tag)){
                                        int idx = labelIndexMapper[i];
                                        View labelView = labelsListView.getChildAt(i);

                                        AnimatorSet colorAnimation =
                                                (AnimatorSet)
                                                        AnimatorInflater.loadAnimator(
                                                                EventActivity.this, R.animator.color_animation);
                                        colorAnimation.setTarget(labelView);
                                        colorAnimation.start();

                                    }

                                }
                            }
                        });


                Log.d(LOG_TAG, result.toString());
            }

            @Override
            public void onError(CochlException error) {
                Log.d(LOG_TAG, error.toString());
            }
        });
        for(int i= 0; i<chosen.size(); i++ ){
            System.out.println(chosen.get(i));
        }




    }
    public void stopRecording() {
        isRecording = false;
        model.stopPredict();

    }
}
