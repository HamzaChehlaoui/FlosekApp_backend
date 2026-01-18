package com.flosek.flosek.mapper;

import com.flosek.flosek.dto.response.SalaryResponseDTO;
import com.flosek.flosek.entity.Salary;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Salary entity
 */
@Mapper(componentModel = "spring")
public interface SalaryMapper {

    SalaryResponseDTO toResponseDTO(Salary salary);

    List<SalaryResponseDTO> toResponseDTOList(List<Salary> salaries);
}
