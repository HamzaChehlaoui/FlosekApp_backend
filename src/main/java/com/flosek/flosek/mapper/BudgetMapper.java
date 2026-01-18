package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.response.BudgetResponseDTO;
import com.flosek.flosek.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Budget entity
 */
@Mapper(componentModel = "spring", uses = { CategoryMapper.class })
public interface BudgetMapper {

    @Mapping(target = "remainingAmount", expression = "java(budget.getRemainingAmount())")
    @Mapping(target = "isExceeded", expression = "java(budget.isExceeded())")
    @Mapping(target = "spentPercentage", expression = "java(calculateSpentPercentage(budget))")
    BudgetResponseDTO toResponseDTO(Budget budget);

    List<BudgetResponseDTO> toResponseDTOList(List<Budget> budgets);

    default Double calculateSpentPercentage(Budget budget) {
        if (budget.getAmount() == null || budget.getAmount().doubleValue() == 0) {
            return 0.0;
        }
        double spent = budget.getSpentAmount() != null ? budget.getSpentAmount().doubleValue() : 0;
        return (spent / budget.getAmount().doubleValue()) * 100;
    }
}
