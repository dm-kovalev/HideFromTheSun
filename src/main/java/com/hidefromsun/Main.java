package com.hidefromsun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;

/**
 * @author abulgako
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, ApiException, IOException {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("")
                .build();
        DirectionsResult results = DirectionsApi.getDirections(context, "Peterhof", "Saint-Petersburg")
                .alternatives(true)
                .await();

        //results.routes[0].legs[0].steps[0].polyline.decodePath()
        List<List<LatLng>> routes = new ArrayList<>();

        for (DirectionsRoute route : results.routes) {

            List<LatLng> lngs = Stream.of(route.legs).sequential()
                .flatMap(directionsLeg -> Stream.of(directionsLeg.steps))
                .map(directionsStep -> directionsStep.polyline)
                .flatMap(encodedPolyline -> encodedPolyline.decodePath().stream())
                .collect(Collectors.toList());

            routes.add(lngs);
        }

        System.out.println();
    }

}
