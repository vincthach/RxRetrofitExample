package nnt.com.rxandroidexample;

import android.location.Location;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import nnt.com.rxandroidexample.adapter.WeatherForecastListAdapter;
import nnt.com.rxandroidexample.helpers.TemperatureFormatter;
import nnt.com.rxandroidexample.models.CurrentWeather;
import nnt.com.rxandroidexample.models.WeatherForecast;
import nnt.com.rxandroidexample.myapi.config.WeatherConfig;
import nnt.com.rxandroidexample.myapi.data.WeatherForecastListDataEnvelope;
import nnt.com.rxandroidexample.myapi.repositories.LocationServiceApi;
import nnt.com.rxandroidexample.myapi.repositories.WeatherServiceApi;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CompositeSubscription mCompositeSubscription;
    private TextView mLocationNameTextView;
    private TextView mCurrentTemperatureTextView;
    private ListView mForecastListView;
    private TextView mAttributionTextView;

    private static final String KEY_CURRENT_WEATHER = "key_current_weather";
    private static final String KEY_WEATHER_FORECASTS = "key_weather_forecasts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mCompositeSubscription = new CompositeSubscription();
        initViews();
        attachListeners();
    }

    private void attachListeners (){
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeather();
            }
        });
    }

    private void initViews() {
        mLocationNameTextView = (TextView) findViewById(R.id.location_name);
        mAttributionTextView = (TextView) findViewById(R.id.attribution);
        mForecastListView = (ListView) findViewById(R.id.weather_forecast_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_container);
        mCurrentTemperatureTextView = (TextView) findViewById(R.id.current_temperature);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                android.R.color.black,
                R.color.colorAccent,
                android.R.color.black);
        mAttributionTextView.setVisibility(View.INVISIBLE);
    }

    private void updateWeather() {
        mSwipeRefreshLayout.setRefreshing(true);

        Observable getLocationObservable = LocationServiceApi.getCurrentLocation();
        getLocationObservable
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<Location, Observable<HashMap<String, WeatherForecast>>>() {
                    @Override
                    public Observable<HashMap<String, WeatherForecast>> call(Location location) {
                        WeatherServiceApi weatherServiceApi = new WeatherServiceApi();
                        return Observable.zip(weatherServiceApi.fetchCurrentWeather(location),
                                weatherServiceApi.fetchWeatherForecasts(location),
                                new Func2<CurrentWeather, List<WeatherForecast>, HashMap<String, WeatherForecast>>() {
                                    @Override
                                    public HashMap<String, WeatherForecast> call(CurrentWeather currentWeather, List<WeatherForecast> weatherForecasts) {
                                        HashMap weatherData = new HashMap();
                                        weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                                        weatherData.put(KEY_WEATHER_FORECASTS, weatherForecasts);
                                        return weatherData;
                                    }
                                }
                        );
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<HashMap<String, WeatherForecast>>() {
                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAttributionTextView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Log.e("Error", error.getMessage());
                    }

                    @Override
                    public void onNext(HashMap<String, WeatherForecast> weatherData) {
                        final CurrentWeather currentWeather = (CurrentWeather) weatherData
                                .get(KEY_CURRENT_WEATHER);
                        mLocationNameTextView.setText(currentWeather.getLocationName());
                        mCurrentTemperatureTextView.setText(
                                TemperatureFormatter.format(currentWeather.getTemperature()));

                        // Update weather forecast list.
                        final List<WeatherForecast> weatherForecasts = (List<WeatherForecast>)
                                weatherData.get(KEY_WEATHER_FORECASTS);
                        final WeatherForecastListAdapter adapter = (WeatherForecastListAdapter)
                                mForecastListView.getAdapter();
                        adapter.clear();
                        adapter.addAll(weatherForecasts);
                    }
                });
    }
}
