package com.example.reservelib.irbis.dto;

import java.util.List;

public record IrbisLibrariesResponse(String title, int count, List<String> libraries) {
}
