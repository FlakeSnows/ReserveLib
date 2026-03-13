package com.example.reservelib.dto;

public class CoordinatesDto {

    private final String address;
    private final String latitude;
    private final String longitude;

    public CoordinatesDto(String address, String latitude, String longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
