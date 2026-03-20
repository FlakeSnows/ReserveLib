package com.example.reservelib.catalog.dto;

public class LibraryResponse {

    private int id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public LibraryResponse(int id, String name, String address, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
