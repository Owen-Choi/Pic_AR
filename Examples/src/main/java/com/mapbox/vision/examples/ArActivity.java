package com.mapbox.vision.examples;

import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.route.RouteFetcher;
import com.mapbox.services.android.navigation.v5.route.RouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.vision.VisionManager;
import com.mapbox.vision.ar.VisionArManager;
import com.mapbox.vision.ar.core.models.ManeuverType;
import com.mapbox.vision.ar.core.models.Route;
import com.mapbox.vision.ar.core.models.RoutePoint;
import com.mapbox.vision.ar.view.gl.VisionArView;
import com.mapbox.vision.mobile.core.interfaces.VisionEventsListener;
import com.mapbox.vision.mobile.core.models.AuthorizationStatus;
import com.mapbox.vision.mobile.core.models.Camera;
import com.mapbox.vision.mobile.core.models.Country;
import com.mapbox.vision.mobile.core.models.FrameSegmentation;
import com.mapbox.vision.mobile.core.models.classification.FrameSignClassifications;
import com.mapbox.vision.mobile.core.models.detection.FrameDetections;
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate;
import com.mapbox.vision.mobile.core.models.position.VehicleState;
import com.mapbox.vision.mobile.core.models.road.RoadDescription;
import com.mapbox.vision.mobile.core.models.world.WorldDescription;
import com.mapbox.vision.performance.ModelPerformance.On;
import com.mapbox.vision.performance.ModelPerformanceMode;
import com.mapbox.vision.performance.ModelPerformanceRate;
import com.mapbox.vision.utils.VisionLogger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Example shows how Vision and VisionAR SDKs are used to draw AR lane over the video stream from camera.
 * Also, Mapbox navigation services are used to build route and  navigation session.
 */
public class ArActivity extends BaseActivity implements RouteListener, ProgressChangeListener, OffRouteListener {

    private static final String TAG = ArActivity.class.getSimpleName();

    // Handles navigation.
    private MapboxNavigation mapboxNavigation;
    // Fetches route from points.
    private RouteFetcher routeFetcher;
    private RouteProgress lastRouteProgress;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> locationCallback;

    private boolean visionManagerWasInit = false;
    private boolean navigationWasStarted = false;

    // This dummy points will be used to build route. For real world test this needs to be changed to real values for
    // source and target locations.
//    private final Point ROUTE_ORIGIN = Point.fromLngLat(139.72622, 35.68446);
//    private final Point ROUTE_DESTINATION = Point.fromLngLat(139.764332, 35.669547);

//    private final Point ROUTE_ORIGIN = Point.fromLngLat(127.1274756, 37.4109056);
    private Point ROUTE_ORIGIN;

//    private final Point ROUTE_DESTINATION = Point.fromLngLat(80.210064, 13.031197);
    private Point ROUTE_DESTINATION;

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_ar_navigation);
    }

    protected void setArRenderOptions(@NotNull final VisionArView visionArView) {
        visionArView.setFenceVisible(true);
    }

    @Override
    protected void onPermissionsGranted() {
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopVisionManager();
        stopNavigation();
        Toast myToast = Toast.makeText(getApplicationContext(),"네비게이션 스탑 ",Toast.LENGTH_SHORT);
        myToast.show();
    }

    private void startVisionManager() {
        Timber.e("비전매니저 실행됨");
        if (allPermissionsGranted() && !visionManagerWasInit) {
            // Create and start VisionManager.
            VisionManager.create();
            VisionManager.setModelPerformance(new On(ModelPerformanceMode.DYNAMIC, ModelPerformanceRate.LOW.INSTANCE));
            VisionManager.start();
            VisionManager.setVisionEventsListener(new VisionEventsListener() {
                @Override
                public void onAuthorizationStatusUpdated(@NotNull AuthorizationStatus authorizationStatus) {
                }

                @Override
                public void onFrameSegmentationUpdated(@NotNull FrameSegmentation frameSegmentation) {
                }

                @Override
                public void onFrameDetectionsUpdated(@NotNull FrameDetections frameDetections) {
                }

                @Override
                public void onFrameSignClassificationsUpdated(@NotNull FrameSignClassifications frameSignClassifications) {
                }

                @Override
                public void onRoadDescriptionUpdated(@NotNull RoadDescription roadDescription) {
                }

                @Override
                public void onWorldDescriptionUpdated(@NotNull WorldDescription worldDescription) {
                }

                @Override
                public void onVehicleStateUpdated(@NotNull VehicleState vehicleState) {
                }

                @Override
                public void onCameraUpdated(@NotNull Camera camera) {
                    Timber.e("카메라 업데이트");
                }

                @Override
                public void onCountryUpdated(@NotNull Country country) {
                    Timber.e("국가 : %s", country);
                }

                @Override
                public void onUpdateCompleted() {
                }
            });

            VisionArView visionArView = findViewById(R.id.mapbox_ar_view);

            // Create VisionArManager.
            VisionArManager.create(VisionManager.INSTANCE);
            visionArView.setArManager(VisionArManager.INSTANCE);
//            setArRenderOptions(visionArView);
            Timber.e("비전 매니저 초기화.");
            visionManagerWasInit = true;
        }
    }

    private void stopVisionManager() {
        if (visionManagerWasInit) {
            VisionArManager.destroy();
            VisionManager.stop();
            VisionManager.destroy();

            visionManagerWasInit = false;
        }
    }

    private void startNavigation() {
        if (allPermissionsGranted() && !navigationWasStarted) {
            // Initialize navigation with your Mapbox access token.
            mapboxNavigation = new MapboxNavigation(
                    this,
                    getString(R.string.mapbox_access_token),
                    MapboxNavigationOptions.builder().build()
            );

            // Initialize route fetcher with your Mapbox access token.
            routeFetcher = new RouteFetcher(this, getString(R.string.mapbox_access_token));
            routeFetcher.addRouteListener(this);

            locationEngine = LocationEngineProvider.getBestLocationEngine(this);

            LocationEngineRequest arLocationEngineRequest = new LocationEngineRequest.Builder(0)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(1000)
                    .build();

            locationCallback = new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
//                    Toast myToast = Toast.makeText(getApplicationContext(),"콜백 성공 " + result, Toast.LENGTH_SHORT);
//                    myToast.show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    // 객체가 잠깐 떴다가 사라질때마다 이 함수가 실행된다.
                    Log.e(TAG, "실패콜백");
                }
            };

            try {
                locationEngine.requestLocationUpdates(arLocationEngineRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException se) {
                Toast myToast = Toast.makeText(getApplicationContext(),"catch 문", Toast.LENGTH_SHORT);
                myToast.show();
                VisionLogger.Companion.e(TAG, se.toString());
            }

            initDirectionsRoute();

            // Route need to be reestablished if off route happens.
            mapboxNavigation.addOffRouteListener(this);
            mapboxNavigation.addProgressChangeListener(this);

            navigationWasStarted = true;
        }
    }

    private void stopNavigation() {
        if (navigationWasStarted) {
            locationEngine.removeLocationUpdates(locationCallback);

            mapboxNavigation.removeProgressChangeListener(this);
            mapboxNavigation.removeOffRouteListener(this);
            mapboxNavigation.stopNavigation();

            navigationWasStarted = false;
        }
    }

    private void initDirectionsRoute() {

        double dest_lat = getIntent().getDoubleExtra("Dest_LAT", 0);
        double dest_lng = getIntent().getDoubleExtra("Dest_LNG", 0);
        double origin_lat = getIntent().getDoubleExtra("Origin_LAT", 0);
        double origin_lng = getIntent().getDoubleExtra("Origin_LNG", 0);

        ROUTE_ORIGIN = Point.fromLngLat(origin_lng, origin_lat);
        ROUTE_DESTINATION = Point.fromLngLat(dest_lng, dest_lat);

        Log.e(TAG, "initDirectionsRoute: " + dest_lat + " " + dest_lng);

        // Get route from predefined points.
        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .origin(ROUTE_ORIGIN)
                .destination(ROUTE_DESTINATION)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                        if (response.body() == null || response.body().routes().isEmpty()) {
                            Toast myToast = Toast.makeText(getApplicationContext(),"경로값 없음", Toast.LENGTH_SHORT);
                            myToast.show();
                            return;
                        }

                        // Start navigation session with retrieved route.
                        DirectionsRoute route = response.body().routes().get(0);
                        mapboxNavigation.startNavigation(route);
                        // Set route progress.
                        VisionArManager.setRoute(new Route(
                                getRoutePoints(route),
                                route.duration().floatValue(),
                                "",
                                ""
                        ));
                        Toast myToast = Toast.makeText(getApplicationContext(),"direction 설정 성공", Toast.LENGTH_SHORT);
                        myToast.show();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast myToast = Toast.makeText(getApplicationContext(),"direction 설정 실패", Toast.LENGTH_SHORT);
                        myToast.show();
                    }
                });
    }

    @Override
    public void onErrorReceived(Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }

        mapboxNavigation.stopNavigation();
        Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseReceived(@NotNull DirectionsResponse response, RouteProgress routeProgress) {
        mapboxNavigation.stopNavigation();
        if (response.routes().isEmpty()) {
            Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
        } else {
            DirectionsRoute route = response.routes().get(0);

            mapboxNavigation.startNavigation(route);

            // Set route progress.
            VisionArManager.setRoute(new Route(
                    getRoutePoints(route),
                    (float) routeProgress.durationRemaining(),
                    "",
                    ""
            ));
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastRouteProgress = routeProgress;
    }

    @Override
    public void userOffRoute(Location location) {
        routeFetcher.findRouteFromRouteProgress(location, lastRouteProgress);
    }

    private RoutePoint[] getRoutePoints(@NotNull DirectionsRoute route) {
        ArrayList<RoutePoint> routePoints = new ArrayList<>();

        List<RouteLeg> legs = route.legs();
        if (legs != null) {
            for (RouteLeg leg : legs) {

                List<LegStep> steps = leg.steps();
                if (steps != null) {
                    for (LegStep step : steps) {
                        RoutePoint point = new RoutePoint((new GeoCoordinate(
                                step.maneuver().location().latitude(),
                                step.maneuver().location().longitude()
                        )), mapToManeuverType(step.maneuver().type()));

                        routePoints.add(point);

                        List<Point> geometryPoints = buildStepPointsFromGeometry(step.geometry());
                        for (Point geometryPoint : geometryPoints) {
                            point = new RoutePoint((new GeoCoordinate(
                                    geometryPoint.latitude(),
                                    geometryPoint.longitude()
                            )), ManeuverType.None);

                            routePoints.add(point);
                        }
                    }
                }
            }
        }

        return routePoints.toArray(new RoutePoint[0]);
    }

    private List<Point> buildStepPointsFromGeometry(String geometry) {
        return PolylineUtils.decode(geometry, Constants.PRECISION_6);
    }

    private ManeuverType mapToManeuverType(@Nullable String maneuver) {
        if (maneuver == null) {
            return ManeuverType.None;
        }
        switch (maneuver) {
            case "turn":
                return ManeuverType.Turn;
            case "depart":
                return ManeuverType.Depart;
            case "arrive":
                return ManeuverType.Arrive;
            case "merge":
                return ManeuverType.Merge;
            case "on ramp":
                return ManeuverType.OnRamp;
            case "off ramp":
                return ManeuverType.OffRamp;
            case "fork":
                return ManeuverType.Fork;
            case "roundabout":
                return ManeuverType.Roundabout;
            case "exit roundabout":
                return ManeuverType.RoundaboutExit;
            case "end of road":
                return ManeuverType.EndOfRoad;
            case "new name":
                return ManeuverType.NewName;
            case "continue":
                return ManeuverType.Continue;
            case "rotary":
                return ManeuverType.Rotary;
            case "roundabout turn":
                return ManeuverType.RoundaboutTurn;
            case "notification":
                return ManeuverType.Notification;
            case "exit rotary":
                return ManeuverType.RotaryExit;
            default:
                return ManeuverType.None;
        }
    }
}
