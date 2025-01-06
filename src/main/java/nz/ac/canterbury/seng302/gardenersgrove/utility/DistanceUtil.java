package nz.ac.canterbury.seng302.gardenersgrove.utility;

public class DistanceUtil {

    /**
     * Private constructor to prevent initialisation of this class, as it is static
     */
    private DistanceUtil(){}

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     * This formula is equivalent to the one from the link on the Geographical Distance Calculation spike
     * @param userLat the latitude of the user's location
     * @param userLon the longitude of the user's location
     * @param jobLat the latitude of the job's location
     * @param jobLon the longitude of the job's location
     * @return the distance between the user's location and the job's location in kilometers
     */
    public static double calculateDistance(double userLat, double userLon, double jobLat, double jobLon) {
        final int R = 6371; // Radius of the earth in kilometers
        double latDistance = Math.toRadians(jobLat - userLat);
        double lonDistance = Math.toRadians(jobLon - userLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(jobLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return Math.round(distance * 10.0) / 10.0; // 1dp - nearest 100m
    }

}
