package com.test.map.manager;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceStatusListener;

import java.util.List;

/**
 * @author tu
 * @date 2020/5/20 18
 */
public class TrackManager implements TraceStatusListener {

    private TraceStatusListener mTraceLitener;
    private long mStartTime;
    private long mEndTime;
    private boolean isStop=true;
    private long mSumTime;

    public static TrackManager getInstance() {
        return Builder.trackManager;
    }

    public void startTrack(Context context) {
        LBSTraceClient.getInstance(context).startTrace(this);
        mStartTime = System.currentTimeMillis();
        mSumTime=0;
        isStop=false;
    }

    public void stopTrack(Context context) {
        mEndTime = System.currentTimeMillis();
        if (!isStop) {
            mSumTime = mEndTime - mStartTime;
        }
        isStop=true;
        LBSTraceClient.getInstance(context).stopTrace();
    }

    public void setTraceListener(TraceStatusListener traceLitener) {
        mTraceLitener=traceLitener;
    }

    public void removeTraceListener() {
        mTraceLitener=null;
    }

    public long getSumTime() {
        return mSumTime;
    }

    @Override
    public void onTraceStatus(List<TraceLocation> list, List<LatLng> list1, String s) {
        if (mTraceLitener != null) {
            mTraceLitener.onTraceStatus(list,  list1,  s);
        }
    }


    private static class Builder{
        private static TrackManager trackManager = new TrackManager();
    }
}
