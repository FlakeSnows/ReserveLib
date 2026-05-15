package com.example.reservelib.catalog.dto;

public class LibraryResponse {

    private int id;
    private String name;

    public LibraryResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
