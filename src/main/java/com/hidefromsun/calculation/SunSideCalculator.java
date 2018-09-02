package com.hidefromsun.calculation;

import com.google.maps.model.LatLng;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;
import org.apache.commons.lang3.Range;

import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class SunSideCalculator {

    private enum Side {
        FRONT, RIGHT, BACK, LEFT
    }

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

    private static Side computeSide(double bearing, double sunAzimuth) {
        double sunPosition = bearing <= sunAzimuth
                ? sunAzimuth - bearing
                : 360 - bearing + sunAzimuth;

        if (Range.between(355.0, 360.0).contains(sunPosition)
            || Range.between(0.0, 5.0).contains(sunPosition)) {
            return Side.FRONT;
        }
        if (Range.between(5.0, 175.0).contains(sunPosition)) {
            return Side.RIGHT;
        }
        if (Range.between(175.0, 185.0).contains(sunPosition)) {
            return Side.BACK;
        }
        if (Range.between(185.0, 355.0).contains(sunPosition)) {
            return Side.LEFT;
        }
        throw new RuntimeException("your universe has broken");
    }

    private static double percent(int part, int total) {
        return (double) part / total * 100;
    }

    public static SunSideStatistics getSideStatistics(List<LatLng> route) {
        final GregorianCalendar calendar = new GregorianCalendar();
        Map<Side, Integer> sunPosCount = new EnumMap<>(Side.class);

        for (int i = 0; i < route.size() - 2; i++) {
            LatLng currentPosition = route.get(i);
            double bearing = computeBearing(currentPosition, route.get(i + 1));
            AzimuthZenithAngle sunPosition = Grena3.calculateSolarPosition(
                    calendar,
                    currentPosition.lat,
                    currentPosition.lng,
                    DeltaT.estimate(calendar));
            double sunAzimuth = sunPosition.getAzimuth();

            sunPosCount.merge(computeSide(bearing, sunAzimuth), 1, Integer::sum);
        }

        int routeSize = route.size();
        return new SunSideStatistics(
                percent(sunPosCount.get(Side.FRONT), routeSize),
                percent(sunPosCount.get(Side.RIGHT), routeSize),
                percent(sunPosCount.get(Side.BACK), routeSize),
                percent(sunPosCount.get(Side.LEFT), routeSize));
    }
}
