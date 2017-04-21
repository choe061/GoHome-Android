package com.example.choi.gohome.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

/**
 * Created by choi on 2016-09-20.
 */
public class SendQuestionMail extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private String phoneNum, device, swVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        phoneNum = telephonyManager.getLine1Number();
        device = Build.BRAND + " " + Build.MODEL;
        swVersion = Build.VERSION.RELEASE;
        sendMail();
        finish();
    }

    public void sendMail() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("plain/text");
        sendIntent.setData(Uri.parse("qjarbs111@gmail.com"));
        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
//            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "test@gmail.com" });
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "<집으로> 문의메일");
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "문의 종류(에러/건의) : " +
                "\n핸드폰 번호 : " + phoneNum +
                "\n기기 정보 : " + device +
                "\nSW 버전 : " + swVersion +
                "\n답변 받을 메일 : " +
                "\n문의 내용 : "
        );
        startActivity(sendIntent);
    }
}
