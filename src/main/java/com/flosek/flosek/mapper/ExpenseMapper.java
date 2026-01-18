package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.response.ExpenseResponseDTO;
import com.flosek.flosek.entity.Expense;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Expense entity
 */
@Mapper(componentModel = "spring", uses = { CategoryMapper.class })
public interface ExpenseMapper {

    ExpenseResponseDTO toResponseDTO(Expense expense);

    List<ExpenseResponseDTO> toResponseDTOList(List<Expense> expenses);
}
