package ru.solonchev.blogback.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.solonchev.blogback.web.dto.CreateTagsRequest;
import ru.solonchev.blogback.web.dto.TagResponse;
import ru.solonchev.blogback.web.dto.TagResponseWithPagination;
import ru.solonchev.blogback.web.service.TagService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<TagResponseWithPagination> findAllTags(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(tagService.findAllTags(page, size));
    }

    @PostMapping
    public ResponseEntity<List<TagResponse>> createTags(@RequestBody CreateTagsRequest request) {
        return new ResponseEntity<>(tagService.createTags(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}