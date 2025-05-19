package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.solonchev.blogback.persistence.model.Post;
import ru.solonchev.blogback.persistence.model.Tag;
import ru.solonchev.blogback.persistence.repository.TagRepository;
import ru.solonchev.blogback.web.dto.CreateTagsRequest;
import ru.solonchev.blogback.web.dto.TagResponse;
import ru.solonchev.blogback.web.dto.TagResponseWithPagination;
import ru.solonchev.blogback.web.mapper.TagMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private UUID tagId;
    private Tag tag;
    private TagResponse tagResponse;
    private List<Tag> tags;
    private List<TagResponse> tagResponses;
    private Page<Tag> tagPage;
    private CreateTagsRequest createTagsRequest;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();

        tag = new Tag()
                .setId(tagId)
                .setName("Test Tag")
                .setPosts(new HashSet<>());

        tagResponse = new TagResponse()
                .setId(tagId)
                .setName("Test Tag")
                .setPostCount(0);

        tags = List.of(tag);
        tagResponses = List.of(tagResponse);

        tagPage = new PageImpl<>(tags);

        createTagsRequest = new CreateTagsRequest()
                .setNames(Set.of("New Tag"));
    }

    @Test
    @DisplayName("Should return tags with pagination when findAllTags is called")
    void shouldReturnTagsWithPaginationWhenFindAllTagsIsCalled() {
        int page = 0;
        int size = 10;
        long totalElements = 1;
        int totalPages = 1;

        Pageable pageable = PageRequest.of(page, size);
        when(tagRepository.findAllWithPostCount(pageable)).thenReturn(tagPage);
        when(tagMapper.mapToTagResponse(tag)).thenReturn(tagResponse);

        TagResponseWithPagination result = tagService.findAllTags(page, size);

        assertNotNull(result);
        assertEquals(tagResponses.size(), result.getContent().size());
        assertEquals(tagResponses.get(0).getId(), result.getContent().get(0).getId());
        assertEquals(tagResponses.get(0).getName(), result.getContent().get(0).getName());
        assertEquals(totalElements, result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(page, result.getCurrentPage());

        verify(tagRepository).findAllWithPostCount(pageable);
        verify(tagMapper).mapToTagResponse(tag);
    }

    @Test
    @DisplayName("Should create tags when createTags is called with valid request")
    void shouldCreateTagsWhenCreateTagsIsCalledWithValidRequest() {
        Set<String> tagNames = createTagsRequest.getNames();
        when(tagRepository.findByNameIn(tagNames)).thenReturn(Collections.emptyList());
        when(tagRepository.saveAll(anyList())).thenReturn(tags);
        when(tagMapper.mapToListTagResponse(tags)).thenReturn(tagResponses);

        List<TagResponse> result = tagService.createTags(createTagsRequest);

        assertNotNull(result);
        assertEquals(tagResponses.size(), result.size());
        assertEquals(tagResponses.get(0).getId(), result.get(0).getId());
        assertEquals(tagResponses.get(0).getName(), result.get(0).getName());

        verify(tagRepository).findByNameIn(tagNames);
        verify(tagRepository).saveAll(anyList());
        verify(tagMapper).mapToListTagResponse(tags);
    }

    @Test
    @DisplayName("Should delete tag when deleteTag is called with valid id")
    void shouldDeleteTagWhenDeleteTagIsCalledWithValidId() {
        Tag emptyTag = new Tag()
                .setId(tagId)
                .setName("Test Tag")
                .setPosts(new HashSet<>());

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(emptyTag));

        tagService.deleteTag(tagId);

        verify(tagRepository).findById(tagId);
        verify(tagRepository).deleteById(tagId);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when deleteTag is called with tag having posts")
    void shouldThrowIllegalStateExceptionWhenDeleteTagIsCalledWithTagHavingPosts() {
        Tag tagWithPosts = new Tag()
                .setId(tagId)
                .setName("Test Tag")
                .setPosts(new HashSet<>());

        Post post = new Post();
        tagWithPosts.getPosts().add(post);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagWithPosts));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tagService.deleteTag(tagId)
        );

        assertEquals("Cannot delete tag with posts", exception.getMessage());

        verify(tagRepository).findById(tagId);
        verifyNoMoreInteractions(tagRepository);
    }

    @Test
    @DisplayName("Should find tag by id when findTagById is called with valid id")
    void shouldFindTagByIdWhenFindTagByIdIsCalledWithValidId() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        Tag result = tagService.findTagById(tagId);

        assertNotNull(result);
        assertEquals(tag.getId(), result.getId());
        assertEquals(tag.getName(), result.getName());

        verify(tagRepository).findById(tagId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when findTagById is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenFindTagByIdIsCalledWithNonExistingId() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> tagService.findTagById(tagId)
        );

        verify(tagRepository).findById(tagId);
    }

    @Test
    @DisplayName("Should find tags by ids when findTagsByIds is called with valid ids")
    void shouldFindTagsByIdsWhenFindTagsByIdsIsCalledWithValidIds() {
        Set<UUID> tagIds = Set.of(tagId);
        when(tagRepository.findAllById(tagIds)).thenReturn(tags);

        List<Tag> result = tagService.findTagsByIds(tagIds);

        assertNotNull(result);
        assertEquals(tags.size(), result.size());
        assertEquals(tags.get(0).getId(), result.get(0).getId());
        assertEquals(tags.get(0).getName(), result.get(0).getName());

        verify(tagRepository).findAllById(tagIds);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when findTagsByIds is called with non-existing ids")
    void shouldThrowEntityNotFoundExceptionWhenFindTagsByIdsIsCalledWithNonExistingIds() {
        Set<UUID> tagIds = Set.of(tagId, UUID.randomUUID());
        when(tagRepository.findAllById(tagIds)).thenReturn(tags);

        assertThrows(
                EntityNotFoundException.class,
                () -> tagService.findTagsByIds(tagIds)
        );

        verify(tagRepository).findAllById(tagIds);
    }
}