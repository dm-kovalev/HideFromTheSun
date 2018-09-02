package com.hidefromsun.controller;

import com.hidefromsun.calculation.SunSideCalculator;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class SideController {

    @Autowired
    private GeoApiContext context;

    // TODO all these actions must be transferred to the frontend
    // used only for debugging in the current version
    private List<LatLng> getExampleRoute(String origin, String dest) throws InterruptedException, ApiException, IOException {
        DirectionsResult results = DirectionsApi.getDirections(context, origin, dest)
                .alternatives(true)
                .await();
        return Stream.of(results.routes[0].legs).sequential()
                .flatMap(directionsLeg -> Stream.of(directionsLeg.steps))
                .map(directionsStep -> directionsStep.polyline)
                .flatMap(encodedPolyline -> encodedPolyline.decodePath().stream())
                .collect(Collectors.toList());
    }

    // should take a selected route (?) from frontend instead of origin/dest
    @RequestMapping(value = "side", method = RequestMethod.GET)
    public String calculateSideStatistics(@RequestParam("origin") String origin, @RequestParam("dest") String dest) {
        String stat = null;
        try {
            stat = SunSideCalculator.getSideStatistics(getExampleRoute(origin, dest)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stat == null ? "empty" : stat;
    }
}
