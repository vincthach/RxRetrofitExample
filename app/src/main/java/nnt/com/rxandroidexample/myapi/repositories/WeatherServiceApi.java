package nnt.com.rxandroidexample.myapi.repositories;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import nnt.com.rxandroidexample.models.CurrentWeather;
import nnt.com.rxandroidexample.models.WeatherForecast;
import nnt.com.rxandroidexample.myapi.data.CurrentWeatherDataEnvelope;
import nnt.com.rxandroidexample.myapi.data.WeatherForecastListDataEnvelope;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Func1;

import static nnt.com.rxandroidexample.myapi.config.WeatherConfig.API_KEY;
import static nnt.com.rxandroidexample.myapi.config.WeatherConfig.WEB_SERVICE_BASE_URL;

/**
 * Created by vinc.nguyen on 12/19/16.
 */

public class WeatherServiceApi {

    private final WeatherRepositoryInterface mWebservice;

    public WeatherServiceApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WEB_SERVICE_BASE_URL)
                .build();
        mWebservice = retrofit.create(WeatherRepositoryInterface.class);
    }

    interface WeatherRepositoryInterface {
        @GET("/weather?units=metric")
        Observable<CurrentWeatherDataEnvelope> fetchCurrentWeather(@Query("lon") String apiKey,
                                                                   @Query("lon") double longitude,
                                                                   @Query("lat") double latitude);
        @GET("/forecast/daily?units=metric&cnt=7")
        Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(@Query("lon") String apiKey,
                                                                          @Query("lon") double longitude,
                                                                          @Query("lat") double latitude);
    }


    public Observable<CurrentWeather> fetchCurrentWeather(Location location) {
        return mWebservice
                .fetchCurrentWeather(API_KEY, location.getLongitude(), location.getLatitude())
                .flatMap(new Func1<CurrentWeatherDataEnvelope, Observable<? extends CurrentWeatherDataEnvelope>>() {
                    @Override
                    public Observable<? extends CurrentWeatherDataEnvelope> call(CurrentWeatherDataEnvelope currentWeatherDataEnvelope) {
                        return currentWeatherDataEnvelope.filterWebServiceErrors();
                    }
                })
                .map(new Func1<CurrentWeatherDataEnvelope, CurrentWeather>() {
                    @Override
                    public CurrentWeather call(CurrentWeatherDataEnvelope data) {
                        return new CurrentWeather(data.locationName, data.timestamp,
                                data.weather.get(0).description, data.main.temp,
                                data.main.temp_min, data.main.temp_max);
                    }
                });
    }

    public Observable<List<WeatherForecast>> fetchWeatherForecasts(Location location) {
        return mWebservice
                .fetchWeatherForecasts(API_KEY, location.getLongitude(), location.getLatitude())
                .flatMap(new Func1<WeatherForecastListDataEnvelope, Observable<? extends WeatherForecastListDataEnvelope>>() {
                    @Override
                    public Observable<? extends WeatherForecastListDataEnvelope> call(WeatherForecastListDataEnvelope data) {
                        return data.filterWebServiceErrors();
                    }
                })
                .map(new Func1<WeatherForecastListDataEnvelope, List<WeatherForecast>>() {

                    @Override
                    public List<WeatherForecast> call(WeatherForecastListDataEnvelope listData) {
                        final ArrayList<WeatherForecast> weatherForecasts =
                                new ArrayList<>();

                        for (WeatherForecastListDataEnvelope.ForecastDataEnvelope data : listData.list) {
                            final WeatherForecast weatherForecast = new WeatherForecast(
                                    listData.city.name, data.timestamp, data.weather.get(0).description,
                                    data.temp.min, data.temp.max);
                            weatherForecasts.add(weatherForecast);
                        }

                        return weatherForecasts;
                    }
                });

    }

}
