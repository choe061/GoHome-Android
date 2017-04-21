package com.example.choi.gohome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.choi.gohome.R;
import com.example.choi.gohome.network.AppController;
import com.example.choi.gohome.network.domain.request.User;
import com.example.choi.gohome.network.domain.response.ResultResponse;
import com.example.choi.gohome.network.domain.response.RegisterResponse;
import com.tsengvn.typekit.TypekitContextWrapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by choi on 2016-08-16.
 */
public class RegisterActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private User user;
    private EditText inputPhone, inputPW, inputPW2, inputName;
    private Spinner inputAge, inputGender;
    private Button duplication, registerBtn;
    private String phone, pw, pw2, name;
    private int age, gender;
    private Handler handler;
    private boolean dupFlag = false, pwFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputPhone = (EditText) findViewById(R.id.phone_num);
        inputPW = (EditText) findViewById(R.id.pw);
        inputPW2 = (EditText) findViewById(R.id.pw2);
        inputName = (EditText) findViewById(R.id.name);
        inputAge = (Spinner) findViewById(R.id.age);
        inputGender = (Spinner) findViewById(R.id.gender);
        duplication = (Button) findViewById(R.id.duplication_btn);
        registerBtn = (Button) findViewById(R.id.register_btn);

        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        phone = telephonyManager.getLine1Number();
        inputPhone.setText(phone);
        inputPhone.setEnabled(false);
        inputPhone.setClickable(false);
        inputPhone.setFocusable(false);
        inputPhone.setFocusableInTouchMode(false);

        inputPW2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pw = inputPW.getText().toString();
                pw2 = inputPW2.getText().toString();
                if (!s.toString().equals(pw)) {
                    if(pw2 != null) {
                        inputPW.setTextColor(0xFFFF0000);
                        inputPW2.setTextColor(0xFFFF0000);
                        pwFlag = false;
                    }
                } else if (s.toString().equals(pw)) {
                    inputPW.setTextColor(0xFF66CC99);
                    inputPW2.setTextColor(0xFF66CC99);
                    pwFlag = true;
                }
                inputPW.invalidate();
                inputPW2.invalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        duplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = inputPhone.getText().toString().trim();
                if(!phone.isEmpty()) {
                    duplocationPhone(phone);
                }
            }
        });

        String[] ageArr = getResources().getStringArray(R.array.age_arrays);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, ageArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputAge.setAdapter(adapter);
        inputAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age = position;
                Log.e("age", String.valueOf(age));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] genderArr = getResources().getStringArray(R.array.gender_arrays);
        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, genderArr);
        gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(gender_adapter);
        inputGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = position;
                Log.e("gender", String.valueOf(gender));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw = inputPW.getText().toString();
                name = "미등록";
                if(!inputName.getText().toString().isEmpty()) {
                    name = inputName.getText().toString().trim();
                }

                if(dupFlag) {
                    if (!phone.isEmpty() && pw.equals(pw2)) {
                        user = new User(phone, pw, name, age, gender);
                        register();
                    } else {
                        Toast.makeText(RegisterActivity.this, "비밀번호를 다시 확인하세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "핸드폰 번호 중복확인을 하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void duplocationPhone(String phone) {
        Call<ResultResponse> duplicationCall = AppController.getHttpService().duplication(phone);
        duplicationCall.enqueue(new Callback<ResultResponse>() {
            @Override
            public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                if(response.isSuccessful()) {
                    ResultResponse resultResponse = response.body();
                    if(resultResponse.isResult()) {
                        dupFlag = true;
                        Toast.makeText(RegisterActivity.this, "사용 가능한 번호입니다.", Toast.LENGTH_SHORT).show();
                    } else if(!resultResponse.isResult()) {
                        dupFlag = false;
                        Toast.makeText(RegisterActivity.this, "이미 등록된 번호입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultResponse> call, Throwable t) {

            }
        });
    }

    public void register() {
        Call<RegisterResponse> registerResponseCall = AppController.getHttpService().register(user);
        registerResponseCall.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if(response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if(registerResponse.isResult()) {
                        Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.\n[서버 에러]", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다.\n[서버 에러]", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
