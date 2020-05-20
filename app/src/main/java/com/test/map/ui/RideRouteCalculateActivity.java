package com.test.map.ui;

import android.content.Intent;
import android.os.Bundle;

import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;
import com.test.map.R;
import com.test.map.base.BaseActivity;
import com.test.map.manager.TrackManager;


/**
 * @author tu
 */
public class RideRouteCalculateActivity extends BaseActivity {

    private NaviLatLng mStart;
    private NaviLatLng mEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_navi);
        mAMapNaviView =  findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        Intent intent = getIntent();
        mStart = intent.getParcelableExtra("start");
        mEnd = intent.getParcelableExtra("end");
    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        mAMapNavi.calculateRideRoute(mStart, mEnd);

    }

    @Override
    public void onCalculateRouteSuccess(int[] ids) {
        super.onCalculateRouteSuccess(ids);
        mAMapNavi.startNavi(NaviType.GPS);
    }

    @Override
    public void onStartNavi(int type) {
        super.onStartNavi(type);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TrackManager.getInstance().stopTrack(this);
        setResult(RESULT_OK);
    }
}
