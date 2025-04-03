package ru.solonchev.blogback.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.solonchev.blogback.web.dto.CreatePostRequestDto;
import ru.solonchev.blogback.web.dto.PostDto;
import ru.solonchev.blogback.web.dto.UpdatePostRequestDto;
import ru.solonchev.blogback.web.service.PostService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDto>> findAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId) {
        return ResponseEntity.ok(postService.findAllPosts(categoryId, tagId));
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<PostDto>> findAllDrafts(@RequestAttribute UUID userId) {
        return ResponseEntity.ok(postService.findAllUserDrafts(userId));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequestDto request,
            @RequestAttribute UUID userId) {
        return new ResponseEntity<>(postService.createPost(request, userId), HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequestDto requestDto) {
        return ResponseEntity.ok(postService.updatePost(postId, requestDto));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
