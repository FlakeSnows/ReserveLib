package com.example.reservelib.irbis;

import com.example.reservelib.irbis.dto.IrbisLibrariesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/irbis")
public class IrbisController {

    private final IrbisService irbisService;

    public IrbisController(IrbisService irbisService) {
        this.irbisService = irbisService;
    }

    @GetMapping("/libraries")
    public IrbisLibrariesResponse getLibrariesByBookTitle(
            @RequestParam String title,
            @RequestParam(required = false) Boolean deepScan
    ) {
        return irbisService.findLibrariesByTitle(title, deepScan);
    }
}
