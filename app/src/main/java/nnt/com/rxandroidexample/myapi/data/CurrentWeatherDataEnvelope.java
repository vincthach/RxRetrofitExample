package nnt.com.rxandroidexample.myapi.data;

/**
 * Created by vinc.nguyen on 12/19/16.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Data structure for current weather results returned by the web service.
 */
public class CurrentWeatherDataEnvelope extends WeatherDataEnvelope {
    @SerializedName("name")
    public String locationName;

    @SerializedName("dt")
    public long timestamp;

    public ArrayList<Weather> weather;

    public Main main;

    public class Main {
        public float temp;
        public float temp_min;
        public float temp_max;
    }
}