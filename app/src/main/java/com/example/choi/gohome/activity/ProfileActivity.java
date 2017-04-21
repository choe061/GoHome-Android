package com.example.choi.gohome.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choi.gohome.R;
import com.example.choi.gohome.utils.SettingPreference;
import com.example.choi.gohome.network.AppController;
import com.example.choi.gohome.network.domain.request.ProfileRequest;
import com.example.choi.gohome.network.domain.request.UpdateProfile;
import com.example.choi.gohome.network.domain.response.ProfileResponse;
import com.example.choi.gohome.network.domain.response.ResultResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by choi on 2016-10-10.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView profilePhone, userCode;
    private EditText profileName;
    private Spinner profileAge, profileGender;
    private SettingPreference authData;
    private Button profileSaveBtn, guardianLinkBtn, pwChangeLinkBtn;
    private String myPhone, token, name, user_code;
    private int age, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userCode = (TextView) findViewById(R.id.user_code);
        profilePhone = (TextView) findViewById(R.id.phone);
        profileName = (EditText) findViewById(R.id.name);
        profileAge = (Spinner) findViewById(R.id.age);
        profileGender = (Spinner) findViewById(R.id.gender);
        profileSaveBtn = (Button) findViewById(R.id.profile_save_btn);
        guardianLinkBtn = (Button) findViewById(R.id.guardian_link_btn);
        pwChangeLinkBtn = (Button) findViewById(R.id.pw_change_link_btn);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String[] ageArr = getResources().getStringArray(R.array.age_arrays);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, ageArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileAge.setAdapter(adapter);
        profileAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] genderArr = getResources().getStringArray(R.array.gender_arrays);
        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, genderArr);
        gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileGender.setAdapter(gender_adapter);
        profileGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        profileSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = profileName.getText().toString();
                setProfile();
            }
        });

        guardianLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, GuardiansActivity.class));
            }
        });

        pwChangeLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, PwChangeActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        authData = new SettingPreference("auth", ProfileActivity.this);
        myPhone = authData.getPref("myPhone");
        token = authData.getPref("token");
        getProfile();
    }

    public void getProfile() {
        ProfileRequest profileRequest = new ProfileRequest(myPhone, token);
        Call<ProfileResponse> profileResponseCall = AppController.getHttpService().getProfile(profileRequest);
        profileResponseCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if(response.isSuccessful()) {
                    ProfileResponse profile = response.body();
                    if(profile.isResult()) {
                        name = profile.getMyProfile().getName();
                        age = profile.getMyProfile().getAge();
                        gender = profile.getMyProfile().getGender();
                        user_code = profile.getMyProfile().getUser_code();
                        userCode.setText(user_code);
                        profilePhone.setText(myPhone);
                        profileName.setText(name);
                        profileAge.setSelection(age);
                        profileGender.setSelection(gender);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {

            }
        });
    }

    public void setProfile() {
        UpdateProfile updateProfile = new UpdateProfile(name, age, gender, myPhone, token);
        Call<ResultResponse> resultResponseCall = AppController.getHttpService().updateMyProfile(updateProfile);
        resultResponseCall.enqueue(new Callback<ResultResponse>() {
            @Override
            public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                if(response.isSuccessful()) {
                    ResultResponse resultResponse = response.body();
                    if(resultResponse.isResult()) {
                        Toast.makeText(ProfileActivity.this, "프로필 설정 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "네트워크 에러, 다시 시도하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}