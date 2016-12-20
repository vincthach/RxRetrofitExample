package nnt.com.rxandroidexample.myapi.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by vinc.nguyen on 12/19/16.
 */

public class WeatherForecastListDataEnvelope extends WeatherDataEnvelope {
    public Location city;
    public ArrayList<ForecastDataEnvelope> list;

    public class Location {
        public String name;
    }

    public class ForecastDataEnvelope {
        @SerializedName("dt")
        public long timestamp;
        public Temperature temp;
        public ArrayList<WeatherDataEnvelope.Weather> weather;
    }

    public class Temperature {
        public float min;
        public float max;
    }
}
