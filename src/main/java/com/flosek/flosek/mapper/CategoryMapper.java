package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.response.CategoryResponseDTO;
import com.flosek.flosek.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Category entity
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDTO(Category category);

    List<CategoryResponseDTO> toResponseDTOList(List<Category> categories);
}
