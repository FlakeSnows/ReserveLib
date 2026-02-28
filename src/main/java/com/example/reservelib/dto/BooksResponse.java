package com.example.reservelib.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BooksResponse {

    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        private VolumeInfo volumeInfo;

        public VolumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(VolumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {

        private String publisher;
        private String description;
        private List<String> authors;
        private String title;

        public String getTitle(){
            return title;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}