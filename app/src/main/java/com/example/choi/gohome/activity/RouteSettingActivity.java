package com.example.choi.gohome.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choi.gohome.utils.SettingPreference;
import com.example.choi.gohome.R;

import java.util.ArrayList;

/**
 * Created by choi on 2016-08-02.
 */
public class RouteSettingActivity extends AppCompatActivity {

    private FloatingActionButton routeAddMain, routeAddAddress, routeAddMap;
    private boolean isFabOpen = false;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private ListView routeListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> routeArrayList = new ArrayList<>();
    private SettingPreference authData;
    private AlertDialog.Builder alerBuilder, alerBuilder_dong;
    private CharSequence[] goo;
    private CharSequence[] dong_kangnam, dong_kangdong, dong_kangbuk, dong_giheung;
    private String address;
    private RelativeLayout container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        routeListView = (ListView) findViewById(R.id.route_list);
        routeAddMain = (FloatingActionButton) findViewById(R.id.route_add_main);
        routeAddAddress = (FloatingActionButton) findViewById(R.id.route_add_address);
        routeAddMap = (FloatingActionButton) findViewById(R.id.route_add_map);
        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

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

        authData = new SettingPreference("auth", RouteSettingActivity.this);

        CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.route_layout);
        if (layout != null) {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFAB();
                    Log.e("배경", "터치");
                }
            });
        }

        for(int i=0; i<authData.getListPref("addressList"); i++) {
            routeArrayList.add(i, authData.getPref("addressList_"+i));
        }
        mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item, R.id.list_text, routeArrayList);
        routeListView.setAdapter(mAdapter);

        goo = new CharSequence[]{"강남구", "강동구", "강북구", "강서구", "기흥구"};
        dong_kangnam = new CharSequence[]{"개포동", "논현동", "대치동", "도곡동", "삼성동", "세곡동", "수서동", "신사동", "압구정동",
                "역삼동", "율현동", "일원동", "자곡동", "청담동"};
        dong_kangdong = new CharSequence[]{"강일동", "고덕동", "길동", "둔촌동", "명일동", "상일동", "성내동", "암사동"};
        dong_kangbuk = new CharSequence[]{"천호동", "미아동", "번동", "수유동", "유이동"};
        dong_giheung = new CharSequence[]{"상하동", "구갈동", "신갈동", "중동"};
        alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setTitle("구를 선택하세요.")
                .setItems(goo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        address = (String) goo[which];
                        switch (which) {
                            case 0:
                                alerBuilder_dong = new AlertDialog.Builder(RouteSettingActivity.this);
                                alerBuilder_dong.setTitle("동을 선택하세요.")
                                        .setItems(dong_kangnam, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                address += " "+dong_kangnam[which];
                                                setAddress();
                                            }
                                        }).create().show();
                                break;
                            case 4:
                                alerBuilder_dong = new AlertDialog.Builder(RouteSettingActivity.this);
                                alerBuilder_dong.setTitle("동을 선택하세요.")
                                        .setItems(dong_giheung, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                address += " "+dong_giheung[which];
                                                setAddress();
                                            }
                                        }).create().show();
                                break;
                        }
                    }
                });
        final AlertDialog alertDialog = alerBuilder.create();

        routeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder confirm = new AlertDialog.Builder(RouteSettingActivity.this);
                confirm.setMessage(routeArrayList.get(position)+"을(를) 삭제 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String deleteRegion = routeArrayList.get(position);
                                ArrayList<String> addressList = new ArrayList<>();
                                if(deleteRegion.equals(authData.getPref("addressList_"+position))) {
                                    authData.deletePref("addressList_"+position);
                                }
                                for(int i=0; i<authData.getListPref("addressList"); i++) {
                                    if(authData.getPref("addressList_"+i) != null) {
                                        addressList.add(authData.getPref("addressList_" + i));
                                    }
                                }
                                routeArrayList.clear();
                                mAdapter.clear();

                                listShow(addressList);
                                Toast.makeText(RouteSettingActivity.this, deleteRegion+"을(를) 삭제했습니다.", Toast.LENGTH_SHORT).show();
                                onStart();
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog confirmDialog = confirm.create();
                confirmDialog.show();
            }
        });

        routeAddMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                startActivity(new Intent(RouteSettingActivity.this, RouteSettingMapActivity.class));
            }
        });

        routeAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                alertDialog.show();
            }
        });
        
        routeAddMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                address = "서울특별시 ";
                animateFAB();
            }
        });
    }

    private void animateFAB() {
        if(isFabOpen) {
            routeAddMain.startAnimation(rotateBackward);
            routeAddAddress.startAnimation(fabClose);
            routeAddMap.startAnimation(fabClose);
            routeAddAddress.setClickable(false);
            routeAddMap.setClickable(false);
            isFabOpen = false;
        } else {
            routeAddMain.startAnimation(rotateForward);
            routeAddAddress.startAnimation(fabOpen);
            routeAddMap.startAnimation(fabOpen);
            routeAddAddress.setClickable(true);
            routeAddMap.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView alert = new TextView(this);
        container = (RelativeLayout) findViewById(R.id.list_main);
        if(authData.getListPref("addressList") == 0) {
            alert.setText("등록된 경로가 없습니다.");
            alert.setTextSize(20);
            RelativeLayout.LayoutParams alertLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
            alert.setLayoutParams(alertLayout);
            container.addView(alert);
        } else if(authData.getListPref("addressList") != 0) {
            container.removeView(alert);
        }
    }

    public void setAddress() {
        ArrayList<String> addressList = new ArrayList<>();
        for(int i=0; i<authData.getListPref("addressList"); i++) {
            addressList.add(authData.getPref("addressList_"+i));
        }
        if(addressList.contains(address)) {
            Toast.makeText(RouteSettingActivity.this, "이미 등록된 경로입니다.", Toast.LENGTH_SHORT).show();
        } else if(!addressList.contains(address)) {
            routeArrayList.clear();
            mAdapter.clear();
            container.removeAllViews();

            addressList.add(address);
            listShow(addressList);
            Toast.makeText(RouteSettingActivity.this, address+"을(를) 추가했습니다.", Toast.LENGTH_SHORT).show();
            onStart();
        }
    }

    public void listShow(ArrayList<String> addressList) {
        authData.setListPref("addressList", addressList);
        for(int i=0; i<addressList.size(); i++) {
            routeArrayList.add(authData.getPref("addressList_"+i));
        }
        mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_list_item, R.id.list_text, routeArrayList);
        routeListView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if(isFabOpen) {
            animateFAB();
        } else {
            super.onBackPressed();
        }
    }
}
