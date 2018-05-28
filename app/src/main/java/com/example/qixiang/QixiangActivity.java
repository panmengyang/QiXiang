package com.example.qixiang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qixiang.gson.DS;
import com.example.qixiang.service.AutoUpdateService;
import com.example.qixiang.util.HttpUtil;
import com.example.qixiang.util.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class QixiangActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefreshLayout;

    private ScrollView qixiangLayout;

    private TextView titleCity, degreeText, Time, temMax, temMin, tiganText;

    private TextView fengText, shiText, yaText;

    private TextView winds;

    private TextView sInst;

    private TextView dInst;

    private TextView sAvg;

    private TextView dAvg;

    private TextView rhuMin;

    private TextView pre1h;

    private TextView prsMax;

    private TextView prsMin;

    private TextView prsSea;

    private TextView vap;

    private Button popButton;

    private PopupWindow myPop;

    private TextView pageNow;

    private TextView s;

    private TextView visText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qixiang);
        popButton = findViewById(R.id.menu_button);
        popButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.layout_pop, null);
                myPop = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                final TextView h1 = view.findViewById(R.id.tv_1h);
                final String stationId = (String) titleCity.getText();
                pageNow = findViewById(R.id.page_now);
                h1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPop.dismiss();
                        pageNow.setText(h1.getText());
                        requestQiXiang(stationId);
                    }
                });
                final TextView h3 = view.findViewById(R.id.tv_3h);
                h3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPop.dismiss();
                        pageNow.setText(h3.getText());
                        requestQixiang1(stationId);
                    }
                });
                final TextView h6 = view.findViewById(R.id.tv_6h);
                h6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPop.dismiss();
                        pageNow.setText(h6.getText());
                        requestQiXiang2(stationId);
                    }
                });
                myPop.setOutsideTouchable(true);
                myPop.setFocusable(true);
                myPop.showAsDropDown(popButton);
            }
        });
        qixiangLayout = findViewById(R.id.qixiang_layout);
        titleCity = findViewById(R.id.title_city);
        Time = findViewById(R.id.time);
        temMax = findViewById(R.id.tem_max);
        temMin = findViewById(R.id.tem_min);
        degreeText = findViewById(R.id.degree_text);
        fengText = findViewById(R.id.feng_text);
        shiText = findViewById(R.id.shi_text);
        yaText = findViewById(R.id.ya_text);
        winds = findViewById(R.id.d_s);
        sInst = findViewById(R.id.s_inst);
        dInst = findViewById(R.id.d_inst);
        sAvg = findViewById(R.id.s_avg);
        dAvg = findViewById(R.id.d_avg);
        rhuMin = findViewById(R.id.rhu_min);
        pre1h = findViewById(R.id.pre_1h);
        prsMax = findViewById(R.id.prs_max);
        prsMin = findViewById(R.id.prs_min);
        prsSea = findViewById(R.id.prs_sea);
        vap = findViewById(R.id.vap);
        s = findViewById(R.id.zuifeng);
        tiganText = findViewById(R.id.tigan_text);
        visText = findViewById(R.id.vis);
        swipeRefreshLayout = findViewById(R.id.swipe_fresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String qixiangString = prefs.getString("ds", null);
        final String stationId;
        if (qixiangString != null) {
            DS ds = Utility.handleQiXiangResponse(qixiangString);
            stationId = ds.Station_Id_C;
            showQiXiangInfo(ds);
        } else {
            stationId = getIntent().getStringExtra("station_id");
            qixiangLayout.setVisibility(View.INVISIBLE);
            requestQiXiang(stationId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestQiXiang(stationId);
            }
        });
    }

    public void requestQiXiang(final String stationId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        Date date1 = new Date(date.getTime() - (long) 9 * 60 * 60 * 1000);
        String time = formatter.format(date1);
        String qixiangUrl = "http://api.data.cma.cn:8090/api?userId=527413713427Xmdhn&pwd=TfXLIIx&dataFormat=json&interfaceId=getSurfEleByTimeRangeAndStaID&timeRange=[" + time + "0000," + time + "0000]&staIDs=" + stationId + "&elements=TEM,TEM_Max,TEM_Min,tigan,PRS,PRS_Sea,PRS_Max,PRS_Min,VAP,RHU,RHU_Min,windpower,WIN_D_Avg_2mi,WIN_D_S_Max,WIN_S_Max,WIN_D_INST_Max,WIN_S_Inst_Max,WIN_S_Avg_2mi,PRE_1h,VIS,CLO_Cov,CLO_Cov_Low,CLO_COV_LM,WEP_Now,Station_Id_C,Year,Mon,Day,Hour&dataCode=SURF_CHN_MUL_HOR";
        HttpUtil.sendOkHttpRequest(qixiangUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final DS ds = Utility.handleQiXiangResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ds != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(QixiangActivity.this).edit();
                            editor.putString("ds", responseText);
                            editor.apply();
                            showQiXiangInfo(ds);
                        } else {
                            Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestQixiang1(final String stationId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        Date date1 = new Date(date.getTime() - (long) 10 * 60 * 60 * 1000);
        String time = formatter.format(date1);
        String qixiangUrl = "http://api.data.cma.cn:8090/api?userId=527413713427Xmdhn&pwd=TfXLIIx&dataFormat=json&interfaceId=getSurfEleByTimeRangeAndStaID&timeRange=[" + time + "0000," + time + "0000]&staIDs=" + stationId + "&elements=TEM,TEM_Max,TEM_Min,tigan,PRS,PRS_Sea,PRS_Max,PRS_Min,VAP,RHU,RHU_Min,windpower,WIN_D_Avg_2mi,WIN_D_S_Max,WIN_S_Max,WIN_D_INST_Max,WIN_S_Inst_Max,WIN_S_Avg_2mi,PRE_1h,VIS,CLO_Cov,CLO_Cov_Low,CLO_COV_LM,WEP_Now,Station_Id_C,Year,Mon,Day,Hour&dataCode=SURF_CHN_MUL_HOR";
        HttpUtil.sendOkHttpRequest(qixiangUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final DS ds = Utility.handleQiXiangResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ds != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(QixiangActivity.this).edit();
                            editor.putString("ds", responseText);
                            editor.apply();
                            showQiXiangInfo(ds);
                        } else {
                            Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void requestQiXiang2(final String stationId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        Date date1 = new Date(date.getTime() - (long) 11 * 60 * 60 * 1000);
        String time2 = formatter.format(date1);
        String qixiangUrl = "http://api.data.cma.cn:8090/api?userId=527413713427Xmdhn&pwd=TfXLIIx&dataFormat=json&interfaceId=getSurfEleByTimeRangeAndStaID&timeRange=[" + time2 + "0000," + time2 + "0000]&staIDs=" + stationId + "&elements=TEM,TEM_Max,TEM_Min,tigan,PRS,PRS_Sea,PRS_Max,PRS_Min,VAP,RHU,RHU_Min,windpower,WIN_D_Avg_2mi,WIN_D_S_Max,WIN_S_Max,WIN_D_INST_Max,WIN_S_Inst_Max,WIN_S_Avg_2mi,PRE_1h,VIS,CLO_Cov,CLO_Cov_Low,CLO_COV_LM,WEP_Now,Station_Id_C,Year,Mon,Day,Hour&dataCode=SURF_CHN_MUL_HOR";
        HttpUtil.sendOkHttpRequest(qixiangUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final DS ds = Utility.handleQiXiangResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ds != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(QixiangActivity.this).edit();
                            editor.putString("ds", responseText);
                            editor.apply();
                            showQiXiangInfo(ds);
                        } else {
                            Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showQiXiangInfo(DS ds) {
        if (ds != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日HH时");
            Date date = new Date();
            Date date2 = new Date(date.getTime() - (long) 60 * 60 * 1000);
            String time = formatter.format(date2);
            String temmax = ds.TEM_Max + "°/";
            String temmin = ds.TEM_Min + "°";
            String stationName = ds.Station_Id_C;
            String degree = ds.TEM + "°";
            String fengtext = ds.WIN_S_Max;
            String shitext = ds.RHU;
            String yatext = ds.PRS;
            String dsmax = ds.WIN_D_S_Max;
            String sinstmax = ds.WIN_S_Inst_Max;
            String dinstmax = ds.WIN_D_INST_Max;
            String savg = ds.WIN_S_Avg_2mi;
            String davg = ds.WIN_D_Avg_2mi;
            String rhumin = ds.RHU_Min;
            String pre1hour = ds.PRE_1h;
            String prsmax = ds.PRS_Max;
            String prsmin = ds.PRS_Min;
            String prssea = ds.PRS_Sea;
            String vap1 = ds.VAP;
            String fengli = ds.windpower;
            String tigan = "体感温度：" + ds.tigan + "°";
            String vis = ds.VIS;
            visText.setText(vis);
            tiganText.setText(tigan);
            s.setText(fengtext);
            Time.setText(time);
            temMax.setText(temmax);
            temMin.setText(temmin);
            titleCity.setText(stationName);
            degreeText.setText(degree);
            fengText.setText(fengli);
            shiText.setText(shitext);
            yaText.setText(yatext);
            winds.setText(dsmax);
            sInst.setText(sinstmax);
            dInst.setText(dinstmax);
            sAvg.setText(savg);
            dAvg.setText(davg);
            rhuMin.setText(rhumin);
            pre1h.setText(pre1hour);
            prsMax.setText(prsmax);
            prsMin.setText(prsmin);
            prsSea.setText(prssea);
            vap.setText(vap1);
            qixiangLayout.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(QixiangActivity.this, "获取气象信息失败", Toast.LENGTH_SHORT).show();
        }
    }
}
