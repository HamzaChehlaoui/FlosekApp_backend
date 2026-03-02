package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.SalaryRequestDTO;
import com.flosek.flosek.dto.response.SalaryResponseDTO;
import com.flosek.flosek.entity.Salary;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.exception.UnauthorizedAccessException;
import com.flosek.flosek.mapper.SalaryMapper;
import com.flosek.flosek.repository.SalaryRepository;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final SalaryMapper salaryMapper;

    @Override
    @Transactional
    public SalaryResponseDTO createSalary(SalaryRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Mark any existing current salary as not current
        Optional<Salary> currentSalary = salaryRepository
                .findByUserIdAndIsCurrentTrueAndDeletedAtIsNull(userId);
        currentSalary.ifPresent(s -> s.setIsCurrent(false));

        Salary salary = Salary.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "MAD")
                .effectiveDate(request.getEffectiveDate())
                .description(request.getDescription())
                .user(user)
                .isCurrent(true)
                .build();

        Salary saved = salaryRepository.save(salary);
        return salaryMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryResponseDTO> getAllSalaries(UUID userId) {
        List<Salary> salaries = salaryRepository.findByUserIdAndDeletedAtIsNullOrderByEffectiveDateDesc(userId);
        return salaryMapper.toResponseDTOList(salaries);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryResponseDTO getCurrentSalary(UUID userId) {
        Salary salary = salaryRepository.findByUserIdAndIsCurrentTrueAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No current salary found"));
        return salaryMapper.toResponseDTO(salary);
    }

    @Override
    @Transactional
    public SalaryResponseDTO updateSalary(UUID id, SalaryRequestDTO request, UUID userId) {
        Salary salary = findSalaryAndValidateOwner(id, userId);

        salary.setAmount(request.getAmount());
        if (request.getCurrency() != null) salary.setCurrency(request.getCurrency());
        salary.setEffectiveDate(request.getEffectiveDate());
        salary.setDescription(request.getDescription());

        Salary saved = salaryRepository.save(salary);
        return salaryMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSalary(UUID id, UUID userId) {
        Salary salary = findSalaryAndValidateOwner(id, userId);
        salary.softDelete();
        salaryRepository.save(salary);
    }

    private Salary findSalaryAndValidateOwner(UUID id, UUID userId) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary", "id", id));
        if (salary.isDeleted()) {
            throw new ResourceNotFoundException("Salary", "id", id);
        }
        if (!salary.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this salary");
        }
        return salary;
    }
}
