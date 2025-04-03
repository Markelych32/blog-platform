package ru.solonchev.blogback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.solonchev.blogback.persistence.model.Post;
import ru.solonchev.blogback.web.dto.CreatePostRequest;
import ru.solonchev.blogback.web.dto.CreatePostRequestDto;
import ru.solonchev.blogback.web.dto.PostDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMapper.class, UserMapper.class, TagMapper.class}
)
public interface PostMapper {

    PostDto mapToDto(Post post);

    List<PostDto> mapToListDto(List<Post> posts);

    CreatePostRequest mapToCreatePostRequest(CreatePostRequestDto source);
}
