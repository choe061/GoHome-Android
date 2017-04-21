package com.example.choi.gohome.activity;

import android.app.Activity;
import android.os.Bundle;

import com.example.choi.gohome.R;

/**
 * Created by choi on 2016-09-20.
 */
public class SafetyScoutActivity extends Activity {

    /*private static final String CLIENT_ID = Authentication.CLIENT_ID; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private static final SpeechConfig SPEECH_CONFIG = SpeechConfig.OPENAPI_KR; // or SpeechConfig.OPENAPI_EN
    private RecognitionHandler recogHandler;
    private NaverRecognizer naverRecognizer;

    private List<String> arrayResult = new ArrayList<>();
    private TextView txtResult;
    private Button btnStop;
    private Button btnStart;
    private String mResult;
    private boolean serviceFlag = false;

    private AudioWriterPCM writer;

    private boolean isRunning;

    // Handle speech recognition Messages.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady:
                // Now an user can speak.
                txtResult.setText("Connected");
                writer = new AudioWriterPCM(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;

            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;

            case R.id.partialResult:
                // Extract obj property typed with String.
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                String[] results = (String[]) msg.obj;
                mResult = results[0];
                arrayResult.add(mResult);
                Log.d("arrayResult", String.valueOf(arrayResult));
                txtResult.setText(arrayResult.get(arrayResult.size()-1));
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }

                mResult = "Error code : " + msg.obj.toString();
                txtResult.setText(mResult);
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                isRunning = false;
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }

                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                isRunning = false;
                if(serviceFlag) {
                    onStart();
                    btnStart.performClick();
                }
                break;
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safety_scout_activity);

        /*txtResult = (TextView) findViewById(R.id.txt_result);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                serviceFlag = true;
                if (!isRunning) {
                    // Start button is pushed when SpeechRecognizer's state is inactive.
                    // Run SpeechRecongizer by calling recognize().
                    mResult = "";
                    txtResult.setText("Connecting...");
                    btnStart.setText(R.string.str_listening);
                    isRunning = true;

                    naverRecognizer.recognize();
                } else {
                    // This flow is occurred by pushing start button again
                    // when SpeechRecognizer is running.
                    // Because it means that a user wants to cancel speech
                    // recognition commonly, so call stop().
                    btnStart.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceFlag = false;
//                naverRecognizer.getSpeechRecognizer().stopImmediately();
//                naverRecognizer.getSpeechRecognizer().release();
            }
        });*/
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        recogHandler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, recogHandler, CLIENT_ID, SPEECH_CONFIG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initialize() must be called on resume time.
        naverRecognizer.getSpeechRecognizer().initialize();
        mResult = "";
        txtResult.setText("");
        btnStart.setText(R.string.str_start);
        btnStart.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // release() must be called on pause time.
//        naverRecognizer.getSpeechRecognizer().stopImmediately();
//        naverRecognizer.getSpeechRecognizer().release();
//        isRunning = false;
    }

    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference<SafetyScoutActivity> mActivity;

        RecognitionHandler(SafetyScoutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SafetyScoutActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }*/
    /*private Button voiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.safety_scout_activity);
        voiceBtn = (Button)findViewById(R.id.btn_voice);

        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SafetyScoutActivity.this, SpeechActivity.class);
                startActivity(intent);
            }

        });
    }*/
}
