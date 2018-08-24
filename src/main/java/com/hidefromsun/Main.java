package com.hidefromsun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
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
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;

/**
 * @author abulgako
 */
public class Main {

    // https://www.movable-type.co.uk/scripts/latlong.html
    private static double computeBearing(LatLng src, LatLng dst) {
        double lt1 = Math.toRadians(src.lat);
        double lt2 = Math.toRadians(dst.lat);

        double dLng = Math.toRadians(dst.lng - src.lng);

        double y = Math.sin(dLng) * Math.cos(lt2);
        double x = Math.cos(lt1) * Math.sin(lt2) - Math.sin(lt1)
                * Math.cos(lt2) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    public static void main(String[] args) throws InterruptedException, ApiException, IOException {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("")
                .build();
        DirectionsResult results = DirectionsApi.getDirections(context, "Peterhof", "Saint-Petersburg")
                .alternatives(true)
                .await();
        context.shutdown();

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

        int sunOnTheRight = 0;
        final GregorianCalendar calendar = new GregorianCalendar();

        // just for example
        List<LatLng> route = routes.get(0);
        for (int i = 0; i < route.size() - 2; i++) {
            LatLng currentPosition = route.get(i);

            double bearing = computeBearing(currentPosition, route.get(i + 1));
            AzimuthZenithAngle sunPosition = Grena3.calculateSolarPosition(
                    calendar,
                    currentPosition.lat,
                    currentPosition.lng,
                    DeltaT.estimate(calendar));
            double sunAzimuth = sunPosition.getAzimuth();

            // if they are in the same half-plane
            if (bearing <= 180 && sunAzimuth <= 180 || bearing > 180 && sunAzimuth > 180) {
                if (bearing < sunAzimuth)
                    sunOnTheRight++;
            }
            else {
                if (bearing > (sunAzimuth + 180) % 360)
                    sunOnTheRight++;
            }
        }

        String result = sunOnTheRight > route.size() / 2 ? "left" : "right";
        System.out.println(String.format("Sit on the %s side", result));
    }
}
