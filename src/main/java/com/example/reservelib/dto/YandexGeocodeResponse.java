package com.example.reservelib.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexGeocodeResponse {

    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        private GeoObjectCollection GeoObjectCollection;

        public GeoObjectCollection getGeoObjectCollection() {
            return GeoObjectCollection;
        }

        public void setGeoObjectCollection(GeoObjectCollection geoObjectCollection) {
            GeoObjectCollection = geoObjectCollection;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {

        private List<FeatureMember> featureMember;

        public List<FeatureMember> getFeatureMember() {
            return featureMember;
        }

        public void setFeatureMember(List<FeatureMember> featureMember) {
            this.featureMember = featureMember;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {

        private GeoObject GeoObject;

        public GeoObject getGeoObject() {
            return GeoObject;
        }

        public void setGeoObject(GeoObject geoObject) {
            GeoObject = geoObject;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObject {

        private Point Point;

        public Point getPoint() {
            return Point;
        }

        public void setPoint(Point point) {
            Point = point;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {

        private String pos;

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }
    }
}