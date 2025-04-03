package ru.solonchev.blogback.web.mapper;

import org.mapstruct.*;
import ru.solonchev.blogback.persistence.model.Post;
import ru.solonchev.blogback.persistence.model.PostStatus;
import ru.solonchev.blogback.persistence.model.Tag;
import ru.solonchev.blogback.web.dto.TagResponse;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    TagResponse mapToTagResponse(Tag source);

    List<TagResponse> mapToListTagResponse(List<Tag> tags);

    @Named("calculatePostCount")
    default Integer calculatePostCount(Set<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return 0;
        }
        return (int) posts.stream()
                .filter(post -> post.getStatus().equals(PostStatus.PUBLISHED))
                .count();
    }
}
