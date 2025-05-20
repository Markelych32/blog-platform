package ru.solonchev.blogback.web.mapper;

import org.mapstruct.*;
import ru.solonchev.blogback.persistence.model.Category;
import ru.solonchev.blogback.persistence.model.PostStatus;
import ru.solonchev.blogback.web.dto.CategoryDto;
import ru.solonchev.blogback.web.dto.CreateCategoryRequest;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "postCount", source = ".", qualifiedByName = "mapPostCount")
    CategoryDto mapEntityToDto(Category source);

    List<CategoryDto> mapListEntityToListDto(List<Category> source);

    Category mapCreateCategoryRequestToEntity(CreateCategoryRequest source);

    @Named(value = "mapPostCount")
    default long mapPostCount(Category source) {
        if (source.getPosts() == null) {
            return 0;
        }
        return source.getPosts().stream()
                .filter(post -> post.getStatus().equals(PostStatus.PUBLISHED))
                .count();
    }
}
