package com.example.qixiang;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.qixiang.gson.DS;
import com.example.qixiang.gson.QiXiang;
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

    private TextView titleCity,degreeText,sheshidu;

    private TextView fengText,shiText,yaText,zuims,hpa,hpc,feng,shi,ya;

    private TextView dsText,winds,zuidu;

    private TextView sInstText,sInst,jims;

    private TextView dInstText,dInst,jidu;

    private TextView sAvgText,sAvg,ms2min;

    private TextView dAvgText,dAvg,du2min;

    private TextView rhuMinText,rhuMin,hpcMin;

    private TextView pre1hText,pre1h,mm;

    private TextView prsMaxText,prsMax,hpaMax;

    private TextView prsMinText,prsMin,hpaMin;

    private TextView prsSeaText,prsSea,hpaSea;

    private TextView vapText,vap,hpaVap;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qixiang);
        bingPicImg = findViewById(R.id.bing_pic_img);
        qixiangLayout = findViewById(R.id.qixiang_layout);
        titleCity = findViewById(R.id.title_city);
        degreeText = findViewById(R.id.degree_text);
        sheshidu = findViewById(R.id.sheshidu);
        fengText = findViewById(R.id.feng_text);
        feng = findViewById(R.id.feng);
        zuims = findViewById(R.id.zui_ms);
        shiText = findViewById(R.id.shi_text);
        shi = findViewById(R.id.shi);
        hpc = findViewById(R.id.hpc);
        yaText = findViewById(R.id.ya_text);
        ya = findViewById(R.id.ya);
        hpa = findViewById(R.id.hpa);
        dsText = findViewById(R.id.d_s_text);
        winds = findViewById(R.id.d_s);
        zuidu = findViewById(R.id.zui_du);
        sInstText = findViewById(R.id.s_inst_text);
        sInst = findViewById(R.id.s_inst);
        jims = findViewById(R.id.ji_ms);
        dInstText = findViewById(R.id.d_inst_text);
        dInst = findViewById(R.id.d_inst);
        jidu = findViewById(R.id.ji_du);
        sAvgText = findViewById(R.id.s_avg_text);
        sAvg = findViewById(R.id.s_avg);
        du2min = findViewById(R.id.du_2min);
        dAvgText = findViewById(R.id.d_avg_text);
        dAvg = findViewById(R.id.d_avg);
        jidu = findViewById(R.id.ji_du);
        rhuMinText = findViewById(R.id.rhu_min_text);
        rhuMin = findViewById(R.id.rhu_min);
        hpcMin = findViewById(R.id.hpc_min);
        pre1hText = findViewById(R.id.pre_1h_text);
        pre1h = findViewById(R.id.pre_1h);
        mm = findViewById(R.id.mm);
        prsMaxText = findViewById(R.id.prs_max_text);
        prsMax = findViewById(R.id.prs_max);
        hpaMax = findViewById(R.id.hpa_max);
        prsMinText = findViewById(R.id.prs_min_text);
        prsMin = findViewById(R.id.prs_min);
        hpaMin = findViewById(R.id.hpa_min);
        prsSeaText = findViewById(R.id.prs_sea_text);
        prsSea = findViewById(R.id.prs_sea);
        hpaSea = findViewById(R.id.hpa_sea);
        vapText = findViewById(R.id.vap_text);
        vap = findViewById(R.id.vap);
        hpaVap = findViewById(R.id.hpa_vpa);
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
        String qixiangString = prefs.getString("ds",null);
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

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
    }

    public void requestQiXiang(final String stationId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        String time = formatter.format(date);
        String qixiangUrl = "http://api.data.cma.cn:8090/api?userId=523519206344vZKdh&pwd=o0pX1o1&dataFormat=json&interfaceId=getSurfEleByTimeRangeAndStaID&timeRange=[" + time + "0000," + time +"0000]&staIDs=" + stationId + "&elements=Station_Id_C,Year,Mon,Day,Hour,PRS,PRS_Sea,PRS_Max,PRS_Min,TEM,TEM_Max,TEM_Min,RHU,RHU_Min,VAP,PRE_1h,WIN_D_INST_Max,WIN_S_Max,WIN_D_S_Max,WIN_S_Avg_2mi,WIN_D_Avg_2mi,WEP_Now,WIN_S_INST_Max&dataCode=SURF_CHN_MUL_HOR";
        HttpUtil.sendOkHttpRequest(qixiangUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QixiangActivity.this,"获取气象信息失败",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(QixiangActivity.this,"获取气象信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    public void loadBingPic() {
        String requestBingPic = "http://10.12.51.15/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(QixiangActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(QixiangActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void showQiXiangInfo(DS ds){
        String stationName = ds.Station_Id_C;
        String degree = ds.TEM;
        String fengtext = ds.WIN_S_Max;
        String shitext = ds.RHU;
        String yatext = ds.PRS;
        String dsmax = ds.WIN_D_S_Max;
        String sinstmax = ds.WIN_S_INST_Max;
        String dinstmax = ds.WIN_D_INST_Max;
        String savg = ds.WIN_S_Avg_2mi;
        String davg = ds.WIN_D_Avg_2mi;
        String rhumin = ds.RHU_Min;
        String pre1hour = ds.PRE_1h;
        String prsmax = ds.PRS_Max;
        String prsmin = ds.PRS_Min;
        String prssea = ds.PRS_Sea;
        String vap1 = ds.VAP;
        titleCity.setText(stationName);
        degreeText.setText(degree);
        fengText.setText(fengtext);
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
    }
}
