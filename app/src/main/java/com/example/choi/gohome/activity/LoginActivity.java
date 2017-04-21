package com.example.choi.gohome.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.choi.gohome.R;
import com.example.choi.gohome.utils.SettingPreference;
import com.example.choi.gohome.utils.gcm.RegistrationIntentService;
import com.example.choi.gohome.network.AppController;
import com.example.choi.gohome.network.domain.request.LoginRequest;
import com.example.choi.gohome.network.domain.response.LoginResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tsengvn.typekit.TypekitContextWrapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by choi on 2016-08-16.
 */
public class LoginActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private EditText loginPhone;
    private EditText loginPW;
    private CheckBox loginCookie;
    private Button loginBtn;
    private Button linkRegisterBtn;
    private String phone, pw;
    private LoginRequest loginRequest;
    private ProgressBar progressBar;
    private SettingPreference authData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPhone = (EditText)findViewById(R.id.login_phone_num);
        loginPW = (EditText)findViewById(R.id.login_pw);
        loginCookie = (CheckBox)findViewById(R.id.login_cookie);
        loginBtn = (Button)findViewById(R.id.login_btn);
        linkRegisterBtn = (Button)findViewById(R.id.link_register_btn);
        progressBar = (ProgressBar)findViewById(R.id.progress);

        authData = new SettingPreference("auth", LoginActivity.this);
        if(authData.getFlagPref("checked") && authData.getPref("myPhone") != null && authData.getPref("pw") != null) {
            loginPhone.setText(authData.getPref("myPhone"));
            loginPW.setText(authData.getPref("pw"));
            loginCookie.setChecked(true);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = loginPhone.getText().toString();
                pw = loginPW.getText().toString();

                if(phone.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if(pw.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    loginRequest = new LoginRequest(phone, pw);
                    if(loginCookie.isChecked()) {
                        authData.setFlagPref("checked", true);
                    } else if(!loginCookie.isChecked()){
                        authData.setFlagPref("checked", false);
                    }
                    in();
                }
            }
        });

        linkRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!loginPhone.getText().toString().isEmpty() && !loginPW.getText().toString().isEmpty()) {
            loginRequest = new LoginRequest(phone, pw);
//            loginBtn.performClick();
        }
    }

    private void in() {
        progressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> loginResponseCall = AppController.getHttpService().login(loginRequest);
        loginResponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if(loginResponse.isResult()) {
                        authData.setPref("myPhone", phone);
                        authData.setPref("pw", pw);
                        authData.setPref("token", loginResponse.getToken());

                        getInstanceIdToken();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        progressBar.setVisibility(View.INVISIBLE);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "phone또는 pw가 옳바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "연결 실패", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService 실행
     */
    public void getInstanceIdToken() {
        if(checkPlayServices()) {
            Log.e("MainActivity", "checkPlayServices()");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra("phone", phone);
            startService(intent);
        }
    }

    /**
     * Google Play Service를 사용할 수 있는 환경인지 체크
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e("MainActivity", "이 기기는 지원되지 않습니다.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
