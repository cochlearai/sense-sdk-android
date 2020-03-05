# Sense SDK for Android

# Getting started

In order to run Sense SDK, you would require SDK key. Please contact us by e-mail (support@cochlear.ai) to get your key, which is mandatory to use Sense SDK. Sense SDK for Android supports Android API 26 (Version 8.0 “Oreo”), or later.

  * __Emergency__ Sound Detection

| | | | | |
|:---:|:---:|:---:|:---:|:---:|
| Fire_smoke_alarm | Glassbreak | Gunshot_explosion | Scream | Siren |

  * __Human Interaction__ Sound Detection

| | | | |
|:---:|:---:|:---:|:---:|
| Finger_snap | Knock | Whisper | Whistling |

## Android Studio Setup

To use Sense SDK in your project, add it as a build dependency and import the SDK as follows:

  1. Create `app/libs` library directory if `app/libs` directory doesn't exist in your android App project
  2. Copy `sense-sdk-release.aar` file to `app/libs` directory
  3. Edit `app/build.gradle` file
  4. Edit `app/src/main/AndroidManifest.xml` file
  5. Use Sense SDK in your App source code

In the `app/build.gradle` file, add the following:

```gradle
android {
    (...)
    aaptOptions {
        noCompress "model"
    }
}

repositories {
    flatDir {
        dirs 'libs'
    }
}

(...)

dependencies {
    (...)
    implementation (name:'sense-sdk-release', ext:'aar')
    (...)
}
```

In the `AndroidManifest.xml` file, add the following:

```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

# How to use Sense SDK Android


## Sense SDK Initialization

The Sense SDK can be initialized by calling `Cochl.init(context, sdkKey);` within the `ai.cochlear.sdk.core.Cochl` package. We recommend to start it when your app has finished launching like `onCreate()` method of Activity class. Furthermore, initialize
 work is not heavy, so there will be no overhead when you launch this app.

```java
import ai.cochlear.sdk.core.Cochl;

public class MainActivity extends AppCompatActivity {
  private final String sdkKey = "ENTER YOUR SDK KEY";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Cochl cochl = Cochl.getInstance();
    cochl.init(getApplicationContext(), sdkKey);
  }
}
```

## Sense Models

Sense SDK has its own pre-built deep learning models to predict various acoustic events. The below sample code shows how to load the `human-interaction` model and make a prediction.

```java
import ai.cochlear.sdk.models.Model;

(...)

// Get the human interaction model
Model model = Cochl.getInstance().getModel("human-interaction");
```

## Audio Input

Sense SDK was built with a simple idea: When you give an input data (audio recording & audio file), it detects the sound events in it and returns as a JSON result.

Two input types of `stream` and `file`, are supported in Sense SDK. For audio stream input, AudioRecord object is used as a parameter of addInput() method, while File object is used for audio file input.

* Stream

```java
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

(...)

// Set the parameters for the AudioRecord object
private static final int SAMPLE_RATE = 22050;
private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
// (Buffer size for one second) * (sizeof(float)) * 2
private static final int RECORD_BUF_SIZE = SAMPLE_RATE * 4 * 2;

// Create a AudioRecord object and added it
model.addInput(new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                               SAMPLE_RATE,
                               CHANNEL_CONFIG,
                               AUDIO_FORMAT,
                               RECORD_BUF_SIZE));

```

* File

```java
import java.io.File;

(...)

// Storage which is in the audio file
File sdcard = Environment.getExternalStorageDirectory();

// Create a File object and added it
model.addInput(new File(sdcard,"some_audio_file.wav"));
```

## Predict On Device

---
**NOTE**

Currently, Sense SDK supports audio files with **`22050 Hz sampling rate`** only. If your audio file has a different sampling rate, resample it by using FFmpeg or other tools before using.

* Resampling example on Linux
```
$ ffmpeg -i non_22050hz.wav -ar 22050 resampled_file.wav
```

For the best performance, it is highly recommended to use audio files which are recorded in 22050 Hz or higher (before resampling)

Do **NOT** change the `SAMPLE_RATE` parameter for the audio stream prediction.
```
private static final int SAMPLE_RATE = 22050;
```
---


* JSON result format

```
{
    "status"        : {
        "code"          : <Status code>,
        "description"   : "<Status code description>"
    },
    "result": {
        "task"      : "<TASK NAME>",
        "frames"    : [
            {
                "tag"           : "<CLASS NAME>",
                "probability"   : <Probability value (float) for 'CLASS NAME'>,
                "start_time"    : <Prediction start time in audio file>,
                "end_time"      : <Prediction end time in audio file>,
            },
            (...)
        ],
        "summary"   : [
            {
                "tag"           : "<CLASS NAME>",
                "probability"   : <Probability mean value (float) for continuous tags>,
                "start_time"    : <Prediction start time in first tag>,
                "end_time"      : <Prediction end time in last tag>,
            },
            (...)
        ]
    }
}
```

Note that the above JSON structure is the same as that of Sense API, while its analysis results may slightly differ. Specifically, there may be a loss of accuracy up to 2~3% in SDK due to the conversion of the deep-learning models that allow them to be compact enough for lightweight devices. This loss is expected within the current industry standards.

* Predict the audio stream


```java
import ai.cochlear.sdk.core.Cochl;
import ai.cochlear.sdk.models.Model;
import ai.cochlear.sdk.CochlException;

(...)

private final String sdkKey = "ENTER YOUR SDK KEY";

// Set the parameters for the AudioRecord object
private static final int SAMPLE_RATE = 22050;
private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
private static final int RECORD_BUF_SIZE = SAMPLE_RATE * 4 * 2;

(...)

AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                                     SAMPLE_RATE,
                                     CHANNEL_CONFIG,
                                     AUDIO_FORMAT,
                                     RECORD_BUF_SIZE));

// Get the Cochl singleton object
Cochl cochl = Cochl.getInstance();

// Set the SDK key and initialize SDK
cochl.init(getApplicationContext(), sdkKey);

// Use the model you want to predict on. The model in the sample code
// below is our human interaction model.
final Model model = cochl.getModel("human-interaction");
model.addInput(record);

// Set the callback function that is called after prediction is done
model.predict(new Model.OnPredictListener() {
    @Override
    public void onPredictDone(JSONObject result) {
        Log.d(LOG_TAG, result.toString());
    }

    @Override
    public void onError(CochlException error) {
        Log.d(LOG_TAG, error.toString());
    }
});
```

* Predict the audio file

>**LIMITATIONS**: Audio file length must be at least 1 second.

```java
import ai.cochlear.sdk.core.Cochl;
import ai.cochlear.sdk.models.Model;
import ai.cochlear.sdk.CochlException;

(...)

private final String sdkKey = "ENTER YOUR SDK KEY";

// Get the Cochl singleton object
Cochl cochl = Cochl.getInstance();

// Set the SDK key and initalize SDK
cochl.init(getApplicationContext(), sdkKey);

// Use the model you want to predict on. The model in the sample code
// below is our human interaction model.
final Model model = cochl.getModel("human-interaction");

// Storage which is in the audio file
File sdcard = Environment.getExternalStorageDirectory();

// Create a File object and added it
model.addInput(new File(sdcard,"some_audio_file.wav"));

// Set the callback function that is called after prediction is done
model.predict(new Model.OnPredictListener() {
    @Override
    public void onPredictDone(JSONObject result) {
        try {
            JSONObject results = result.getJSONObject("result");
            JSONArray frames = results.getJSONArray("frames");
            JSONArray summary = results.getJSONArray("summary");
            int len = summary.length();

            Log.d(LOG_TAG, "< Summary >");
            for (int i = 0; i < len; ++i) {
                Log.d(LOG_TAG, summary.get(i).toString());
            }

            int frame_len = frames.length();
            Log.d(LOG_TAG, "< Frames >");
            for (int i = 0; i < frame_len; ++i) {
                Log.d(LOG_TAG, frames.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(CochlException error) {
        Log.d(LOG_TAG, error.toString());
    }
});
```

## Pause & Resume (Stream only)

During the stream type prediction, you can pause and resume the inference as needed.

* Pause & Resume

```java
    pauseBtn = findViewById(R.id.pauseBtn);
    pauseBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            model.pause();
        }
    });

    resumeBtn = findViewById(R.id.resumeBtn);
    resumeBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            model.resume();
        }
    });
```


## Stop Prediction

While Sense SDK is running, several threads would be generated for stream recording, file decoding, or audio analysis. To avoid the risk that these threads will remain after the app has terminated, `stopPredict()` method have to be called before App termination.

```java
    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.stopPredict();
    }
```

## Reference
### Cochl

```
java.lang.Object
    ai.cochlear.sdk.core.Cochl
```

Cochl is a singleton instance and works with the SDK key and App context. After authenticating user Cochl instance can get the model by modelName parameter.

#### Public Methods

* *public static Cochl getInstance()*

Return the Cochl singleton object

* *public static String getSdkVersion()*

Return the Sense SDK version

* *public void init (Context context, String sdkKey)*

Autenticate the user with the SDK key

* *public Model getModel (String modelName)*

Return the Model object. The type of model is decided by `modelName` parameter.

* *public boolean isAuth()*

Return whether the user is authorized or not

### Model

```
java.lang.Object
    ai.cochlear.sdk.models.Model
```

This class explains the model for Sense SDK, which analyzes audio data. It would operate differently, depending on the input data type.

#### Public Constructors

* *public Model(String modelName, Context context, String modelKey)*

Initialize the internal variables by the parameters

#### Public Methods

* *public void addInput(AudioRecord record)*

Add the AudioRecord object to predict an audio stream

* *public void addInput(File file)*

Add the File object to predict an audio file

* *public void predict(OnPredictListener listener)*

Set the callback function that is called after prediction is done and start the prediction

* *public void stopPredict()*

Release resources including Sense SDK internal threads

* *public void pause()*

Pause the prediction for the audio stream

* *public void resume()*

Resume the prediction for the audio stream

### CochlException

```
java.lang.Object
    java.lang.Throwable
        java.lang.Exception
            java.lang.RuntimeException
                ai.cochlear.sdk.CochlException
```

This is custom exception for the exception handling of Sense SDK.

#### Public Constructors

* *public CochlException()*
* *public CochlException(String message)*
* *public CochlException(String format, Object... args)*
* *public CochlException(String message, Throwable throwable)*
* *public CochlException(Throwable throwable)*

The constructor would be generated when its respective exception case occurs.
