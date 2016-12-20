package nnt.com.rxandroidexample.myapi.repositories;

import android.location.Location;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vinc.nguyen on 12/19/16.
 */

public class LocationServiceApi {

    // Return fake data for test rx android
    public static Observable<Location> getCurrentLocation() {
        Location location = new Location("");
        location.setLatitude(112);
        location.setLongitude(37);
        return Observable.just(location);
    }
}
