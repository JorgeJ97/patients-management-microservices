package com.api.patients_service.mapper;

import com.api.patients_service.dto.PatientRequestDTO;
import com.api.patients_service.dto.PatientResponseDTO;
import com.api.patients_service.model.Patient;

import java.time.LocalDate;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient){
        return new PatientResponseDTO(
                patient.getId().toString(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth().toString()
        );
    }

    public static Patient toModel(PatientRequestDTO patient){
        return Patient.builder()
                .name(patient.name())
                .email(patient.email())
                .address(patient.address())
                .dateOfBirth(LocalDate.parse(patient.dateOfBirth()))
                .registeredDate(LocalDate.now())
                .build();
    }

}
