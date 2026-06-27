package com.secondbrain.backend.controller;

import com.secondbrain.backend.dto.SearchRequest;
import com.secondbrain.backend.dto.SearchResult;
import com.secondbrain.backend.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<List<SearchResult>> search(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request.getQuery()));
    }
}
