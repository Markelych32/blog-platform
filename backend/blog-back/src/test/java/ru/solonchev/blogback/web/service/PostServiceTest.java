package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.solonchev.blogback.persistence.model.*;
import ru.solonchev.blogback.persistence.repository.PostRepository;
import ru.solonchev.blogback.web.dto.CreatePostRequestDto;
import ru.solonchev.blogback.web.dto.PostDto;
import ru.solonchev.blogback.web.dto.UpdatePostRequestDto;
import ru.solonchev.blogback.web.mapper.PostMapper;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private UUID postId;
    private UUID userId;
    private UUID categoryId;
    private UUID tagId;
    private User user;
    private Category category;
    private Tag tag;
    private Post post;
    private PostDto postDto;
    private CreatePostRequestDto createPostRequestDto;
    private UpdatePostRequestDto updatePostRequestDto;
    private List<Post> posts;
    private List<PostDto> postDtos;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        tagId = UUID.randomUUID();

        user = new User()
                .setId(userId)
                .setName("Test User")
                .setEmail("test@example.com")
                .setPassword("password")
                .setCreatedAt(LocalDateTime.now());

        category = new Category()
                .setId(categoryId)
                .setName("Test Category");

        tag = new Tag()
                .setId(tagId)
                .setName("Test Tag");

        post = new Post()
                .setId(postId)
                .setTitle("Test Post")
                .setContent("Test Content")
                .setAuthor(user)
                .setCategory(category)
                .setTags(Set.of(tag))
                .setStatus(PostStatus.PUBLISHED)
                .setReadingTime(1)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        postDto = new PostDto()
                .setId(postId)
                .setTitle("Test Post")
                .setContent("Test Content")
                .setStatus(PostStatus.PUBLISHED)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        createPostRequestDto = CreatePostRequestDto.builder()
                .title("New Post")
                .content("New Content")
                .categoryId(categoryId)
                .tagIds(Set.of(tagId))
                .status(PostStatus.DRAFT)
                .build();

        updatePostRequestDto = UpdatePostRequestDto.builder()
                .id(postId)
                .title("Updated Post")
                .content("Updated Content")
                .categoryId(categoryId)
                .tagIds(Set.of(tagId))
                .status(PostStatus.PUBLISHED)
                .build();

        posts = List.of(post);
        postDtos = List.of(postDto);
    }

    @Test
    @DisplayName("Should return all published posts when findAllPosts is called without filters")
    void shouldReturnAllPublishedPostsWhenFindAllPostsIsCalledWithoutFilters() {
        when(postRepository.findAllByStatus(PostStatus.PUBLISHED)).thenReturn(posts);
        when(postMapper.mapToListDto(posts)).thenReturn(postDtos);

        List<PostDto> result = postService.findAllPosts(null, null);

        assertNotNull(result);
        assertEquals(postDtos.size(), result.size());
        assertEquals(postDtos.get(0).getId(), result.get(0).getId());

        verify(postRepository).findAllByStatus(PostStatus.PUBLISHED);
        verify(postMapper).mapToListDto(posts);
    }

    @Test
    @DisplayName("Should return filtered posts by category when findAllPosts is called with categoryId")
    void shouldReturnFilteredPostsByCategoryWhenFindAllPostsIsCalledWithCategoryId() {
        when(categoryService.findCategoryById(categoryId)).thenReturn(category);
        when(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category)).thenReturn(posts);
        when(postMapper.mapToListDto(posts)).thenReturn(postDtos);

        List<PostDto> result = postService.findAllPosts(categoryId, null);

        assertNotNull(result);
        assertEquals(postDtos.size(), result.size());
        assertEquals(postDtos.get(0).getId(), result.get(0).getId());

        verify(categoryService).findCategoryById(categoryId);
        verify(postRepository).findAllByStatusAndCategory(PostStatus.PUBLISHED, category);
        verify(postMapper).mapToListDto(posts);
    }

    @Test
    @DisplayName("Should return filtered posts by tag when findAllPosts is called with tagId")
    void shouldReturnFilteredPostsByTagWhenFindAllPostsIsCalledWithTagId() {
        when(tagService.findTagById(tagId)).thenReturn(tag);
        when(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag)).thenReturn(posts);
        when(postMapper.mapToListDto(posts)).thenReturn(postDtos);

        List<PostDto> result = postService.findAllPosts(null, tagId);

        assertNotNull(result);
        assertEquals(postDtos.size(), result.size());
        assertEquals(postDtos.get(0).getId(), result.get(0).getId());

        verify(tagService).findTagById(tagId);
        verify(postRepository).findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag);
        verify(postMapper).mapToListDto(posts);
    }

    @Test
    @DisplayName("Should return filtered posts by category and tag when findAllPosts is called with both filters")
    void shouldReturnFilteredPostsByCategoryAndTagWhenFindAllPostsIsCalledWithBothFilters() {
        when(categoryService.findCategoryById(categoryId)).thenReturn(category);
        when(tagService.findTagById(tagId)).thenReturn(tag);
        when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag)).thenReturn(posts);
        when(postMapper.mapToListDto(posts)).thenReturn(postDtos);

        List<PostDto> result = postService.findAllPosts(categoryId, tagId);

        assertNotNull(result);
        assertEquals(postDtos.size(), result.size());
        assertEquals(postDtos.get(0).getId(), result.get(0).getId());

        verify(categoryService).findCategoryById(categoryId);
        verify(tagService).findTagById(tagId);
        verify(postRepository).findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag);
        verify(postMapper).mapToListDto(posts);
    }

    @Test
    @DisplayName("Should return user drafts when findAllUserDrafts is called")
    void shouldReturnUserDraftsWhenFindAllUserDraftsIsCalled() {
        when(userService.findUserById(userId)).thenReturn(user);
        when(postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT)).thenReturn(posts);
        when(postMapper.mapToListDto(posts)).thenReturn(postDtos);

        List<PostDto> result = postService.findAllUserDrafts(userId);

        assertNotNull(result);
        assertEquals(postDtos.size(), result.size());
        assertEquals(postDtos.get(0).getId(), result.get(0).getId());

        verify(userService).findUserById(userId);
        verify(postRepository).findAllByAuthorAndStatus(user, PostStatus.DRAFT);
        verify(postMapper).mapToListDto(posts);
    }

    @Test
    @DisplayName("Should create post when createPost is called with valid request")
    void shouldCreatePostWhenCreatePostIsCalledWithValidRequest() {
        when(userService.findUserById(userId)).thenReturn(user);
        when(categoryService.findCategoryById(categoryId)).thenReturn(category);
        when(tagService.findTagsByIds(Set.of(tagId))).thenReturn(List.of(tag));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.mapToDto(post)).thenReturn(postDto);

        PostDto result = postService.createPost(createPostRequestDto, userId);

        assertNotNull(result);
        assertEquals(postDto.getId(), result.getId());
        assertEquals(postDto.getTitle(), result.getTitle());

        verify(userService).findUserById(userId);
        verify(categoryService).findCategoryById(categoryId);
        verify(tagService).findTagsByIds(Set.of(tagId));
        verify(postRepository).save(any(Post.class));
        verify(postMapper).mapToDto(post);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updatePost is called with non-existing post id")
    void shouldThrowEntityNotFoundExceptionWhenUpdatePostIsCalledWithNonExistingPostId() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postService.updatePost(postId, updatePostRequestDto));

        verify(postRepository).findById(postId);
        verifyNoMoreInteractions(postRepository, categoryService, tagService, postMapper);
    }

    @Test
    @DisplayName("Should get post when getPost is called with valid id")
    void shouldGetPostWhenGetPostIsCalledWithValidId() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.mapToDto(post)).thenReturn(postDto);

        PostDto result = postService.getPost(postId);

        assertNotNull(result);
        assertEquals(postDto.getId(), result.getId());
        assertEquals(postDto.getTitle(), result.getTitle());

        verify(postRepository).findById(postId);
        verify(postMapper).mapToDto(post);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getPost is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenGetPostIsCalledWithNonExistingId() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postService.getPost(postId));

        verify(postRepository).findById(postId);
        verifyNoInteractions(postMapper);
    }

    @Test
    @DisplayName("Should delete post when deletePost is called")
    void shouldDeletePostWhenDeletePostIsCalled() {
        postService.deletePost(postId);

        verify(postRepository).deleteById(postId);
    }

    @Test
    @DisplayName("Should calculate reading time correctly")
    void shouldCalculateReadingTimeCorrectly() {
        String content = "This is a test content with exactly five words.";
        int expectedReadingTime = 1;

        CreatePostRequestDto request = CreatePostRequestDto.builder()
                .title("Test")
                .content(content)
                .categoryId(categoryId)
                .status(PostStatus.PUBLISHED)
                .build();

        when(userService.findUserById(userId)).thenReturn(user);
        when(categoryService.findCategoryById(categoryId)).thenReturn(category);
        when(tagService.findTagsByIds(any())).thenReturn(Collections.emptyList());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            assertEquals(expectedReadingTime, savedPost.getReadingTime());
            return savedPost;
        });
        when(postMapper.mapToDto(any(Post.class))).thenReturn(postDto);

        postService.createPost(request, userId);

        verify(postRepository).save(any(Post.class));
    }
}