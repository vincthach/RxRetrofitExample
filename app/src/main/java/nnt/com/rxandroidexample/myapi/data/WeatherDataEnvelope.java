package nnt.com.rxandroidexample.myapi.data;

import com.google.gson.annotations.SerializedName;

import rx.Observable;

/**
 * Created by vinc.nguyen on 12/19/16.
 */

public class WeatherDataEnvelope {
    @SerializedName("cod")
    private int httpCode;

    public static class Weather {
        public String description;
    }

    /**
     * The web service always returns a HTTP header code of 200 and communicates errors
     * through a 'cod' field in the JSON payload of the response body.
     */
    public Observable filterWebServiceErrors() {
        if (httpCode == 200) {
            return Observable.just(this);
        } else {
            return Observable.error(
                    new Exception("There was a problem fetching the weather data."));
        }
    }
}
