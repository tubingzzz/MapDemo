package com.test.map;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;

import com.test.map.base.MainContract;
import com.test.map.callback.AnimatorIml;
import com.test.map.manager.TrackManager;
import com.test.map.presenter.MainPresenter;
import com.test.map.ui.CheckPermissionsActivity;

import androidx.annotation.Nullable;

import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author tu
 */
public class MainActivity  extends CheckPermissionsActivity implements View.OnClickListener  , MainContract.View{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 100;
    private MapView mMapView;
    private RelativeLayout mBottomLayout;
    private AMap aMap;
    private ProgressDialog progDialog;
    private TextView mRouteTimeDes;
    private TextView mRouteDetailDes;
    private View mInforLayout;
    private TextView mDesName;
    private TextView mNaviResult;
    private MainPresenter mMainPresenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
        mMainPresenter = new MainPresenter(this);
        mMainPresenter.init();
    }

    private void initView(Bundle savedInstanceState) {
        mBottomLayout = findViewById(R.id.bottom_layout);
        mRouteTimeDes = findViewById(R.id.firstline);
        mRouteDetailDes = findViewById(R.id.secondline);
        mInforLayout = findViewById(R.id.info_layout);
        mNaviResult = findViewById(R.id.result);
        mDesName = findViewById(R.id.des_name);
        //获取地图控件引用
        mMapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        findViewById(R.id.route_bt).setOnClickListener(this);
        findViewById(R.id.navi_bt).setOnClickListener(this);
        findViewById(R.id.navi_bt1).setOnClickListener(this);
        mBottomLayout.setOnClickListener(this);
    }






    private void showAnimation(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0);
            translationY.setDuration(300);
            translationY.start();
        }
    }

    private void hideAnimation(final View view) {
        if (view.getVisibility() == View.VISIBLE) {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY",  0,view.getHeight());
            translationY.setDuration(300);
            translationY.addListener(new AnimatorIml() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            translationY.start();
        }
    }


    @Override
    public void onBackPressed() {
        if (mBottomLayout.getVisibility() == View.VISIBLE||mInforLayout.getVisibility() ==View.VISIBLE) {
            hideAnimation(mBottomLayout);
            hideAnimation(mInforLayout);
            if (aMap != null) {
                aMap.clear();
            }
        } else {
            super.onBackPressed();
        }
    }

    /*  ****************************** bind activity lifecycle  *******************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        TrackManager.getInstance().removeTraceListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navi_bt:
            case R.id.navi_bt1:
                mMainPresenter.goToNavi();
                break;
            case R.id.route_bt:
                mMainPresenter.getRoutPlan();
                break;
            case R.id.bottom_layout:
                mMainPresenter.goToRouteDetail();
                break;
            default:
                break;
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMainPresenter.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void showLoading() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    @Override
    public void hideLoading() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public AMap getAMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        return aMap;
    }

    @Override
    public void showPoiInfoView(String name) {
        showAnimation(mInforLayout);
        hideAnimation(mBottomLayout);
        mDesName.setText(name);
    }


    @Override
    public void showRouteView(String des) {
        showAnimation(mBottomLayout);
        hideAnimation(mInforLayout);
        mRouteTimeDes.setText(des);
        mRouteDetailDes.setVisibility(View.GONE);
    }

    @Override
    public void showNaviResult(int total, long time) {
        mNaviResult.setText("里程： " +total/1000 +"m ,用时： "+ time/60 + "分鐘");
    }


}
