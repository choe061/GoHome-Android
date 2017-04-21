package com.example.choi.gohome.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choi.gohome.utils.Authentication;
import com.example.choi.gohome.utils.GpsInfo;
import com.example.choi.gohome.network.domain.LatLngSize;
import com.example.choi.gohome.network.domain.MyLatLng;
import com.example.choi.gohome.utils.NaverRecognizer;
import com.example.choi.gohome.R;
import com.example.choi.gohome.utils.SMSService;
import com.example.choi.gohome.utils.SQLiteHandler;
import com.example.choi.gohome.utils.SettingPreference;
import com.example.choi.gohome.utils.gcm.GcmClass;
import com.example.choi.gohome.network.AppController;
import com.example.choi.gohome.network.domain.request.ProfileRequest;
import com.example.choi.gohome.network.domain.response.ProfileResponse;
import com.example.choi.gohome.utils.AudioWriterPCM;
import com.naver.speech.clientapi.SpeechConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private GpsInfo gpsInfo;
    private GcmClass gcmClass;
    private SMSService smsService;
    private SettingPreference authData, pref;
    private SQLiteHandler sqLiteHandler;
    private Handler handler;
    private TimerTask timerTask;
    private Timer timer;

    private Button serviceStart, serviceStop, emergencyCall, mapSearch, routeSetting, etcSetting;
    private TextView myName, myPhoneNum;
    private ProgressBar progressBar;

    private String name, phone;
    private String myPhone, token;
    private String nowAddress;
    private String guardian_phone1, guardian_phone2, guardian_phone3;
    private double lat, lng;
    private int addressListSize, errCnt = 0, errCnt2 = 0;
    private int timePref;
    private boolean smsPref, sms112Pref, voicePref, uploadPref;
    private boolean routerFlag = false, emergency = false, isConnected = false, guardiansFlag = false;
    private List<String> addressList = new ArrayList<>();
    private List<String> guardian_phone = new ArrayList<>();
    private List<LatLngSize> latLngSize;

    private static final String CLIENT_ID = Authentication.CLIENT_ID; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private static final SpeechConfig SPEECH_CONFIG = SpeechConfig.OPENAPI_KR; // or SpeechConfig.OPENAPI_EN
    private RecognitionHandler recogHandler;
    private NaverRecognizer naverRecognizer;
    private AudioWriterPCM writer;

    private String mResult;
    private boolean serviceFlag = false, voiceFlag = false, isRunning;
    private List<String> arrayResult = new ArrayList<>();
    private List<String> helpVoiceList;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            // Handle speech recognition Messages.
            case R.id.clientReady:
                // Now an user can speak.
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
                Log.e("partialResult", mResult);
                if(helpVoiceList.contains(mResult) && mResult != null) {
                    emergencyCall.performClick();
                }
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                String[] results = (String[]) msg.obj;
                mResult = results[0];
                arrayResult.add(mResult);
                Log.e("arrayResult", String.valueOf(arrayResult));
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }

                mResult = "Error code : " + msg.obj.toString();
                Log.e("recognitionError", mResult);
                isRunning = false;
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                Log.e("clientInactive", String.valueOf(serviceFlag));
                isRunning = false;
                if (serviceFlag) {
                    onStart();
                    speechServiceStart();
                }
                break;
        }
    }
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqLiteHandler = new SQLiteHandler(MainActivity.this);

        serviceStart = (Button) findViewById(R.id.service_start);
        serviceStop = (Button) findViewById(R.id.service_stop);
        emergencyCall = (Button) findViewById(R.id.emergency_call);
        mapSearch = (Button) findViewById(R.id.map_search);
        routeSetting = (Button) findViewById(R.id.route_setting);
        etcSetting = (Button) findViewById(R.id.etc_setting);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.bringToFront();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        View header = navigationView.getHeaderView(0);
        myName = (TextView) header.findViewById(R.id.myName);
        myPhoneNum = (TextView) header.findViewById(R.id.myPhoneNum);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        MyLatLng myLatLng = (MyLatLng)msg.obj;
                        lat = myLatLng.getLat();
                        lng = myLatLng.getLng();
                        isConnected = myLatLng.getConnected();
                        break;
                }
            }
        };

        serviceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!guardiansFlag) {
                    Toast.makeText(MainActivity.this, "보호자를 등록하세요.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, GuardiansActivity.class));
                }
                if(voicePref) {
                    String[] helpVoice = getResources().getStringArray(R.array.help_voice_arrays);
                    helpVoiceList = new ArrayList<>(Arrays.asList(helpVoice));
                    try {
                        speechServiceStart();
                    } catch (IllegalMonitorStateException e) {
                        speechServiceStart();
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progressBar.setVisibility(View.VISIBLE);
                            if (errCnt < 5) {
                                gpsSetting();
                                if ((gpsInfo == null) || (nowAddress.isEmpty()) || (!isConnected) || (lat == 0) || (lng == 0)) {
                                    errCnt++;
                                    throw new NullPointerException();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                                start();
                            } else {
                                stop();
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(MainActivity.this, "GPS상태가 좋지않습니다.\n서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                                errCnt = 0;
                            }
                        } catch (NullPointerException e) {
                            serviceStart.performClick();
                        }
                    }
                }, 1000);
            }
        });
        serviceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "안심귀가 서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                stop();
                if(voicePref) {
//                    naverRecognizer.getSpeechRecognizer().stopImmediately();
//                    naverRecognizer.getSpeechRecognizer().release();
                    naverRecognizer = null;
                    serviceFlag = false;
                    voiceFlag = false;
                    isRunning = false;
                }
            }
        });
        emergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "긴급 호출 클릭", Toast.LENGTH_SHORT).show();
                if(nowAddress != null) {
                    String msg = name + "님의 긴급호출! 위치:"+nowAddress;
                    sendGcm(msg);
                    if(smsPref) {
                        for(int i=0; i<guardian_phone.size(); i++) {
                            smsService.sendSMS(guardian_phone.get(i), name+"님이 긴급호출을 하였습니다. 위치: "+nowAddress);
                        }
                    }

                } else {
                    emergency = true;
                    Toast.makeText(MainActivity.this, "서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
                    serviceStart.performClick();
                }
            }
        });
        mapSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapActivity.class));
            }
        });
        routeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RouteSettingActivity.class));
            }
        });
        etcSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smsService.sendSMS("01098031992", "test");
                //startActivity(new Intent(MainActivity.this, SafetyScoutActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recogHandler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, recogHandler, CLIENT_ID, SPEECH_CONFIG);
        authData = new SettingPreference("auth", MainActivity.this);
        smsService = new SMSService(MainActivity.this);
        addressList.clear();
        addressListSize = authData.getListPref("addressList");
        for(int i=0; i<addressListSize; i++) {
            addressList.add(authData.getPref("addressList_"+i));
        }
        gcmClass = new GcmClass();
        myPhone = authData.getPref("myPhone");
        token = authData.getPref("token");

        pref = new SettingPreference("setting", MainActivity.this);
        if(pref.getPref("timePref") == null) {
            pref.setPref("timePref", "1");
        }
        timePref = Integer.parseInt(pref.getPref("timePref"));
        smsPref = pref.getFlagPref("smsPref");
        sms112Pref = pref.getFlagPref("sms112Pref");
        voicePref = pref.getFlagPref("voicePref");
        getProfile(myPhone, token);
        gcmClass.getGcmTokenList(myPhone, MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!voiceFlag) {
            naverRecognizer.getSpeechRecognizer().initialize();
            voiceFlag = !voiceFlag;
        }
        latLngSize = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getRouteDB();
            }
        }, 500);
        errCnt2 = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void gpsSetting() {
        if (gpsInfo == null) {
            gpsInfo = new GpsInfo(MainActivity.this, handler);
        }
        if (gpsInfo != null) {
            gpsInfo.connectStart();
            nowAddress = gpsInfo.getAddress(MainActivity.this, lat, lng);
        }
    }

    public void start() {
        Toast.makeText(MainActivity.this, "안심귀가 서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
        if(addressListSize > 0 || (!latLngSize.isEmpty())) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if((!isConnected) || (lat == 0) || (lng == 0)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "GPS상태가 좋지않습니다.", Toast.LENGTH_SHORT).show();
                                String msg = name+"님의 GPS상태가 좋지않습니다.";
                                sendGcm(msg);

                                if(smsPref) {
                                    for(int i=0; i<guardian_phone.size(); i++) {
                                        if(nowAddress != null) {
                                            smsService.sendSMS(guardian_phone.get(i), name+"님의 GPS상태가 좋지않습니다. 마지막 위치: "+nowAddress);
                                        } else if(nowAddress == null) {
                                            smsService.sendSMS(guardian_phone.get(i), msg);
                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        if(lat != 0 && lng != 0) {
                            nowAddress = gpsInfo.getAddress(MainActivity.this, lat, lng);
                        }
                        for (int i = 0; i < addressListSize; i++) {
//                                    String summaryAddress = addressList.get(i).substring(0, addressList.get(i).lastIndexOf("동"));
//                                    nowAddress.contains(summaryAddress)
                            if (nowAddress.contains(addressList.get(i))) {
                                routerFlag = true;
                            }
                        }
                        for(int i=0; i<latLngSize.size(); i++) {
                            float[] results = new float[3];
                            Location.distanceBetween(lat, lng, latLngSize.get(i).getLat(), latLngSize.get(i).getLng(), results);
                            if (results[0] <= latLngSize.get(i).getSize()) {
                                routerFlag = true;
                            }
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (routerFlag) {
                                    Toast.makeText(MainActivity.this, "경로 유지 "+nowAddress, Toast.LENGTH_SHORT).show();
                                    routerFlag = false;
                                } else {
                                    Toast.makeText(MainActivity.this, "경로 이탈 "+nowAddress, Toast.LENGTH_SHORT).show();
                                    String msg;
                                    if (emergency) {
                                        msg = name + "님의 긴급호출! 위치:" + nowAddress;
                                    } else {
                                        msg = name + "님 경로 이탈! 위치:" + nowAddress;
                                    }
                                    sendGcm(msg);
                                    if(smsPref) {
                                        for(int i=0; i<guardian_phone.size(); i++) {
                                            String g_phone = guardian_phone.get(i);
                                            smsService.sendSMS(g_phone, msg);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000 * 10);  //*timePref 1분 마다 반복
        } else if (addressListSize == 0 && latLngSize.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "먼저 경로를 설정하세요.\n안심귀가 서비스를 종료합니다.", Toast.LENGTH_SHORT).show();
                    routeSetting.performClick();
                }
            });
            stop();
        }
    }

    public void stop() {
        Log.e("순서", "서비스 정지");
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        if(gpsInfo != null) {
            gpsInfo.onDestroy();
            gpsInfo = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.etc_btn:

        }
        return super.onOptionsItemSelected(item);
    }

    public void getProfile(String myPhone, String token) {
        ProfileRequest profileRequest = new ProfileRequest(myPhone, token);
        Call<ProfileResponse> profileResponseCall = AppController.getHttpService().getProfile(profileRequest);
        profileResponseCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if(response.isSuccessful()) {
                    ProfileResponse profile = response.body();

                    myName.setText(profile.getMyProfile().getName());
                    myPhoneNum.setText(profile.getMyProfile().getMyPhone());

                    guardian_phone1 = profile.getMyProfile().getGuardian_phone1();
                    guardian_phone2 = profile.getMyProfile().getGuardian_phone2();
                    guardian_phone3 = profile.getMyProfile().getGuardian_phone3();
                    if(guardian_phone1 != null || guardian_phone2 != null || guardian_phone3 != null){
                        guardiansFlag = true;
                    }
                    guardian_phone.clear();

                    if(guardian_phone1 != null) {
                        guardian_phone.add(guardian_phone1);
                    }
                    if(guardian_phone2 != null) {
                        guardian_phone.add(guardian_phone2);
                    }
                    if(guardian_phone3 != null) {
                        guardian_phone.add(guardian_phone3);
                    }

                } else {
                    name = "프로필 정보를 받지 못했습니다.";
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                name = "프로필 정보를 받지 못했습니다.";
            }
        });
    }

    public void sendGcm(String msg) {
        String to;
        for (int i = 0; i < 3; i++) {
            to = authData.getPref("gcm_token"+i);
            if (to != null) {
                gcmClass.sendGcm(to, msg);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if(id == R.id.nav_app_setting) {
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
        } else if(id == R.id.nav_guide) {
            startActivity(new Intent(MainActivity.this, GuideActivity.class));
        } else if(id == R.id.nav_question) {
            startActivity(new Intent(MainActivity.this, SendQuestionMail.class));
        } else if(id == R.id.nav_app_info) {
            startActivity(new Intent(MainActivity.this, AppInfoActivity.class));
        } else if(id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void getRouteDB() {
        try {
            if(errCnt2 <= 5) {
                latLngSize.addAll(sqLiteHandler.selectAll());
                Log.e("latLngSize", String.valueOf(latLngSize));
            }
        } catch (Exception e) {
            errCnt2++;
            onResume();
        }
    }

    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            } else if(activity == null) {
                //아무 반응 없음
            }
        }
    }

    public void speechServiceStart() {
        serviceFlag = true;
        if (!isRunning) {
            // Start button is pushed when SpeechRecognizer's state is inactive.
            // Run SpeechRecongizer by calling recognize().
            mResult = "";
            isRunning = true;
            naverRecognizer.recognize();
        } else {
            // This flow is occurred by pushing start button again
            // when SpeechRecognizer is running.
            // Because it means that a user wants to cancel speech
            // recognition commonly, so call stop().
            naverRecognizer.getSpeechRecognizer().stop();
        }
    }

    public void logout() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }
}
