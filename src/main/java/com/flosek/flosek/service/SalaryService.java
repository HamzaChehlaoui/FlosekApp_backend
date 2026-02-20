package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.SalaryRequestDTO;
import com.flosek.flosek.dto.response.SalaryResponseDTO;

import java.util.UUID;

public interface SalaryService {

    SalaryResponseDTO createSalary(SalaryRequestDTO request, UUID userId);
}
