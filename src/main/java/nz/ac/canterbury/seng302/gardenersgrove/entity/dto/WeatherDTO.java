package nz.ac.canterbury.seng302.gardenersgrove.entity.dto;

public class WeatherDTO {
    private String day;
    private String date;
    private String description;
    private String image;
    private int[] temperatures;
    private int humidity;

    public WeatherDTO(String day, String date, String description, String image, int[] temperatures, int humidity) {
        this.day = day;
        this.date = date;
        this.description = description;
        this.image = image;
        this.temperatures = temperatures;
        this.humidity = humidity;
    }
    public String getDay() {
        return day;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public int[] getTemperatures() {
        return temperatures;
    }

    public int getHumidity() {
        return humidity;
    }
}
