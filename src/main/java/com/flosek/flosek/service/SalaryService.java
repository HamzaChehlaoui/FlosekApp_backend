package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.SalaryRequestDTO;
import com.flosek.flosek.dto.response.SalaryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SalaryService {

    SalaryResponseDTO createSalary(SalaryRequestDTO request, UUID userId);

    List<SalaryResponseDTO> getAllSalaries(UUID userId);

    SalaryResponseDTO getCurrentSalary(UUID userId);

    SalaryResponseDTO updateSalary(UUID id, SalaryRequestDTO request, UUID userId);

    void deleteSalary(UUID id, UUID userId);
}
