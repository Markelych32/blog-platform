package ru.solonchev.blogback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.solonchev.blogback.persistence.model.User;
import ru.solonchev.blogback.web.dto.AuthorDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    AuthorDto toDto(User source);
}
