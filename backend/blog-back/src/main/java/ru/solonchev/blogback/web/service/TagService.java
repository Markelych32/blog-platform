package ru.solonchev.blogback.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.solonchev.blogback.persistence.model.Tag;
import ru.solonchev.blogback.persistence.repository.TagRepository;
import ru.solonchev.blogback.web.dto.CreateTagsRequest;
import ru.solonchev.blogback.web.dto.TagResponse;
import ru.solonchev.blogback.web.mapper.TagMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponse> findAllTags() {
        return tagMapper.mapToListTagResponse(tagRepository.findAllWithPostCount());
    }

    @Transactional
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
    public void deleteTag(UUID tagId) {
        tagRepository.findById(tagId).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Cannot delete tag with posts");
            }
            tagRepository.deleteById(tagId);
        });
    }
}
