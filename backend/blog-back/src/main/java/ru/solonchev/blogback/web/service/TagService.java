package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.solonchev.blogback.persistence.model.Tag;
import ru.solonchev.blogback.persistence.repository.TagRepository;
import ru.solonchev.blogback.web.dto.CreateTagsRequest;
import ru.solonchev.blogback.web.dto.TagResponse;
import ru.solonchev.blogback.web.dto.TagResponseWithPagination;
import ru.solonchev.blogback.web.mapper.TagMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "tags")
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Cacheable(value = "tagsCache", key = "'tags_page_' + #page + '_size_' + #size")
    public TagResponseWithPagination findAllTags(int page, int size) {
        log.info("Find All Tags method");
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tagPage = tagRepository.findAllWithPostCount(pageable);
        List<TagResponse> result = tagPage.getContent().stream()
                .map(tagMapper::mapToTagResponse)
                .toList();
        return new TagResponseWithPagination()
                .setContent(result)
                .setTotalPages(tagPage.getTotalPages())
                .setTotalElements(tagPage.getTotalElements())
                .setCurrentPage(page);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public List<TagResponse> createTags(CreateTagsRequest request) {
        Set<String> existingTagNames = tagRepository.findByNameIn(request.getNames()).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        List<Tag> tagsToSave = new ArrayList<>();
        for (String tagName : request.getNames()) {
            if (!existingTagNames.contains(tagName)) {
                tagsToSave.add(new Tag().setName(tagName));
            }
        }
        return tagMapper.mapToListTagResponse(tagRepository.saveAll(tagsToSave));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteTag(UUID tagId) {
        tagRepository.findById(tagId).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Cannot delete tag with posts");
            }
            tagRepository.deleteById(tagId);
        });
    }

    @Cacheable(key = "#tagId")
    public Tag findTagById(UUID tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + tagId));
    }

    @Cacheable(key = "#tagIds.hashCode()")
    public List<Tag> findTagsByIds(Set<UUID> tagIds) {
        List<Tag> foundedTags = tagRepository.findAllById(tagIds);
        if (foundedTags.size() != tagIds.size()) {
            throw new EntityNotFoundException("Not all specified tag IDs exist");
        }
        return foundedTags;
    }
}
