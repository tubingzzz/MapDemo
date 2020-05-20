package com.test.map.presenter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.amap.api.trace.TraceStatusListener;
import com.test.map.MainActivity;
import com.test.map.R;
import com.test.map.base.MainContract;
import com.test.map.manager.TrackManager;
import com.test.map.overlay.RideRouteOverlay;
import com.test.map.ui.RideRouteCalculateActivity;
import com.test.map.ui.RideRouteDetailActivity;
import com.test.map.utills.AMapUtil;
import com.test.map.utills.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter implements MainContract.Presenter, AMap.OnMapClickListener, AMap.OnPOIClickListener, AMap.OnMyLocationChangeListener, TraceStatusListener, GeocodeSearch.OnGeocodeSearchListener, RouteSearch.OnRouteSearchListener {

    private static final String TAG = "MainPresenter";
    private static final int REQUEST_CODE = 1000;
    private final MainContract.View mView;

    private final MainActivity mContext;
    private GeocodeSearch mGeocodeSearch;
    private RouteSearch mRouteSearch;
    private Marker marker;
    private Poi mCurrent;
    private AMap mAMap;
    private RideRouteOverlay mRideRouteOverlay;
    private TraceOverlay mTraceOverlay;
    private Location mLocation;
    private RideRouteResult mRideRouteResult;
    private RidePath mRidePath;

    public MainPresenter(MainContract.View view) {
        mView=view;
        mContext = (MainActivity) view;
        initAmap();
    }
    private void initAmap() {
        mAMap = mView.getAMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。

        UiSettings mUiSettings = mAMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setScaleControlsEnabled(true); //比例尺
        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
        mAMap.setMyLocationEnabled(true);
        mAMap.setOnMapClickListener(this);
        mAMap.setOnPOIClickListener(this);
        mAMap.setOnMyLocationChangeListener(this);
        TrackManager.getInstance().setTraceLitener(this);

        //经纬度转换为地址
        mGeocodeSearch = new GeocodeSearch((Context) mView);
        mGeocodeSearch.setOnGeocodeSearchListener(this);
        //路径搜索器
        mRouteSearch = new RouteSearch((Context) mView);
        mRouteSearch.setRouteSearchListener(this);

    }


    @Override
    public void goToNavi() {
        Intent intent = new Intent(mContext, RideRouteCalculateActivity.class);
        intent.putExtra("start", new NaviLatLng(mLocation.getLatitude(),mLocation.getLongitude()));
        intent.putExtra("end", new NaviLatLng(mCurrent.getCoordinate().latitude,mCurrent.getCoordinate().longitude));
        mContext.startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    public void goToRouteDetail() {
        Intent routeDetailIntent = new Intent(mContext, RideRouteDetailActivity.class);
        routeDetailIntent.putExtra("ride_path", mRidePath);
        routeDetailIntent.putExtra("ride_result", mRideRouteResult);
        mContext.startActivity(routeDetailIntent);
    }

    @Override
    public void getRoutPlan() {
        if (mCurrent==null) return;
        LatLonPoint endPoint = new LatLonPoint(mCurrent.getCoordinate().latitude, mCurrent.getCoordinate().longitude);
        LatLonPoint startPoint = new LatLonPoint(mLocation.getLatitude(), mLocation.getLongitude());
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo);
        mRouteSearch.calculateRideRouteAsyn(query);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE ) {
            if (mTraceOverlay != null) {
                int distance = mTraceOverlay.getDistance();
                long sumTime = TrackManager.getInstance().getSumTime()/1000;
                mView.showNaviResult(distance,sumTime);

            }
        }
    }

    /** ************************  点击地图  *******************************/
    @Override
    public void onMapClick(LatLng latLng) {
        Log.e(TAG, "onMapClick: " + latLng);
        if (marker != null) {
            marker.remove();
        }
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
    }

    /** ************************  点击POI  *******************************/
    @Override
    public void onPOIClick(Poi poi) {
        Log.e("test", "onPOIClick: poi==" + poi);
        if (marker != null) {
            marker.remove();
        }
        mCurrent = poi;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(poi.getCoordinate());
        marker = mAMap.addMarker(markerOptions);
        mView.showPoiInfoView(poi.getName());

    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.d(TAG, "onMyLocationChange: location == "+location.toString());
        mLocation = location;
    }

    @Override
    public void onTraceStatus(List<TraceLocation> list, List<LatLng> list1, String s) {
        if (marker != null) {
            marker.remove();
        }
        if (mRideRouteOverlay != null) {
            mRideRouteOverlay.removeFromMap();
        }
        ToastUtil.show(mContext,"list.size()=="+list.size());
        if (list.size() > 0) {
            TraceLocation location = list.get(list.size()-1);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("{");
            stringBuffer.append("\"lon\":").append(location.getLongitude()).append(",");
            stringBuffer.append("\"lat\":").append(location.getLatitude()).append(",");
            stringBuffer.append("\"loctime\":").append(location.getTime()).append(",");
            stringBuffer.append("\"speed\":").append(location.getSpeed()).append(",");
            stringBuffer.append("\"bearing\":").append(location.getBearing());
            stringBuffer.append("}");
            stringBuffer.append("\n");
            ToastUtil.show(mContext,"stringBuffer=="+stringBuffer.toString());
        }
        mTraceOverlay = new TraceOverlay(mAMap);
        List<LatLng> latLngs = traceLocationToMap(list);
        mTraceOverlay.setProperCamera(latLngs);
    }

    /**
     * 轨迹纠偏点转换为地图LatLng
     *
     * @param traceLocationList
     * @return
     */
    public List<LatLng> traceLocationToMap(List<TraceLocation> traceLocationList) {
        List<LatLng> mapList = new ArrayList<LatLng>();
        for (TraceLocation location : traceLocationList) {
            LatLng latlng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mapList.add(latlng);
        }
        return mapList;
    }


    /** ************************  编码地址  *******************************/
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (mContext==null) return;
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            Toast.makeText(mContext, "获取当前位置信息" + regeocodeResult.getRegeocodeAddress().getFormatAddress(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "获取当前位置信息失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }


    /** ************************ 规划路径  *******************************/

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        if (mView == null) {
            return;
        }
        // 清理地图上的所有覆盖物
        if (marker != null) {
            marker.remove();
        }
        if (mRideRouteOverlay != null) {
            mRideRouteOverlay.removeFromMap();
        }
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mRideRouteResult = result;
                    final RidePath ridePath = mRideRouteResult.getPaths().get(0);
                    if (ridePath == null) {
                        return;
                    }
                    mRidePath =ridePath;
                    mRideRouteOverlay = new RideRouteOverlay(
                            mContext, mAMap, ridePath,
                            mRideRouteResult.getStartPos(),
                            mRideRouteResult.getTargetPos());
                    mRideRouteOverlay.removeFromMap();
                    mRideRouteOverlay.addToMap();
                    mRideRouteOverlay.zoomToSpan();

                    int dis = (int) ridePath.getDistance();
                    int dur = (int) ridePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    mView.showRouteView(des);

                } else if (result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(mContext, errorCode);
        }
    }
}
