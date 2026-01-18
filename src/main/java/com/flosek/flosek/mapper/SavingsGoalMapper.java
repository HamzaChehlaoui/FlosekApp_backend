package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.response.SavingsGoalResponseDTO;
import com.flosek.flosek.entity.SavingsGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for SavingsGoal entity
 */
@Mapper(componentModel = "spring")
public interface SavingsGoalMapper {

    @Mapping(target = "progressPercentage", expression = "java(savingsGoal.getProgressPercentage())")
    @Mapping(target = "isCompleted", expression = "java(savingsGoal.isCompleted())")
    SavingsGoalResponseDTO toResponseDTO(SavingsGoal savingsGoal);

    List<SavingsGoalResponseDTO> toResponseDTOList(List<SavingsGoal> savingsGoals);
}
