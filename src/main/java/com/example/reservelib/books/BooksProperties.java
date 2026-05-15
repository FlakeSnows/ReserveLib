package com.example.reservelib.books;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "books")
public class BooksProperties {

    private Provider provider;
    private Search search;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public static class Provider {
        private String baseUrl;
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Search {
        private List<String> publisherKeywords;

        public List<String> getPublisherKeywords() {
            return publisherKeywords;
        }

        public void setPublisherKeywords(List<String> publisherKeywords) {
            this.publisherKeywords = publisherKeywords;
        }
    }
}
