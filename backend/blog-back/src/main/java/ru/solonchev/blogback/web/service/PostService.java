package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.solonchev.blogback.persistence.model.*;
import ru.solonchev.blogback.persistence.repository.PostRepository;
import ru.solonchev.blogback.web.dto.CreatePostRequestDto;
import ru.solonchev.blogback.web.dto.PostDto;
import ru.solonchev.blogback.web.dto.UpdatePostRequestDto;
import ru.solonchev.blogback.web.mapper.PostMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;
    private final PostMapper postMapper;

    private static final int WORDS_PER_MINUTE = 200;

    @Transactional(readOnly = true)
    public List<PostDto> findAllPosts(UUID categoryId, UUID tagId) {
        if (categoryId != null && tagId != null) {
            Category category = categoryService.findCategoryById(categoryId);
            Tag tag = tagService.findTagById(tagId);
            return postMapper.mapToListDto(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag));
        }
        if (categoryId != null) {
            Category category = categoryService.findCategoryById(categoryId);
            return postMapper.mapToListDto(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category));
        }
        if (tagId != null) {
            Tag tag = tagService.findTagById(tagId);
            return postMapper.mapToListDto(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag));
        }
        return postMapper.mapToListDto(postRepository.findAllByStatus(PostStatus.PUBLISHED));
    }

    @Transactional(readOnly = true)
    public List<PostDto> findAllUserDrafts(UUID userId) {
        User user = userService.findUserById(userId);
        return postMapper.mapToListDto(postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT));
    }

    @Transactional
    public PostDto createPost(CreatePostRequestDto request, UUID userId) {
        User author = userService.findUserById(userId);
        Post post = new Post()
                .setTitle(request.getTitle())
                .setContent(request.getContent())
                .setStatus(request.getStatus())
                .setAuthor(author)
                .setReadingTime(calculateReadingTime(request.getContent()));
        Category category = categoryService.findCategoryById(request.getCategoryId());
        post.setCategory(category);

        Set<UUID> tagIds = request.getTagIds();
        List<Tag> tags = tagService.findTagsByIds(tagIds);
        post.setTags(new HashSet<>(tags));

        return postMapper.mapToDto(postRepository.save(post));
    }

    @Transactional
    public PostDto updatePost(UUID postId, UpdatePostRequestDto updatePostRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        post.setTitle(updatePostRequestDto.getTitle());
        post.setContent(updatePostRequestDto.getContent());
        post.setStatus(updatePostRequestDto.getStatus());
        post.setReadingTime(calculateReadingTime(updatePostRequestDto.getContent()));

        UUID updateRequestCategoryId = updatePostRequestDto.getCategoryId();
        if (!post.getCategory().getId().equals(updateRequestCategoryId)) {
            Category category = categoryService.findCategoryById(updateRequestCategoryId);
            post.setCategory(category);
        }

        Set<UUID> existingTagIds = post.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        Set<UUID> updateRequestTagIds = updatePostRequestDto.getTagIds();
        if (existingTagIds != updateRequestTagIds) {
            List<Tag> newTags = tagService.findTagsByIds(updateRequestTagIds);
            post.setTags(new HashSet<>(newTags));
        }
        return postMapper.mapToDto(postRepository.save(post));
    }

    public PostDto getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        return postMapper.mapToDto(post);
    }

    public void deletePost(UUID postId) {
        postRepository.deleteById(postId);
    }

    private Integer calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int wordCount = content.trim().split("\\s+").length;
        return Math.ceilDiv(wordCount, WORDS_PER_MINUTE);
    }
}
