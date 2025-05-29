package com.api.patients_service.service;

import com.api.patients_service.dto.PatientRequestDTO;
import com.api.patients_service.dto.PatientResponseDTO;
import com.api.patients_service.exception.EmailAlreadyExistsException;
import com.api.patients_service.exception.PatientNotFoundException;
import com.api.patients_service.grcp.BillingServiceGrpcClient;
import com.api.patients_service.kafka.KafkaProducer;
import com.api.patients_service.mapper.PatientMapper;
import com.api.patients_service.model.Patient;
import com.api.patients_service.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PatientService {
    private final PatientRepository repository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;


    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = repository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patient){
        if(repository.existsByEmail(patient.email())) {
            throw new EmailAlreadyExistsException(patient.email());
        }
        Patient newPatient = repository.save(PatientMapper.toModel(patient));

        log.info("patient info {} {}", newPatient.getName(), newPatient.getEmail());

        Thread.startVirtualThread(() -> {
            try {
                billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
                        newPatient.getName(), newPatient.getEmail());
            } catch (Exception e) {
                log.error("Error creating billing account for patient {}: {}",
                        newPatient.getId(), e.getMessage(), e);
            }
        });

        Thread.startVirtualThread(() -> {
            try {
                kafkaProducer.sendEvent(newPatient);
            } catch (Exception e) {
                log.error("Error sending Kafka event for patient {}: {}",
                        newPatient.getId(), e.getMessage(), e);
            }
        });

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = repository.findById(id).orElseThrow(() -> new PatientNotFoundException(id.toString()));
        if(repository.existsByEmailAndIdNot(patientRequestDTO.email(), id)){
            throw new EmailAlreadyExistsException(patientRequestDTO.email());
        }
        Patient updatedPatient = updatePatientFields(patient, patientRequestDTO);
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id){
        repository.deleteById(id);
    }

    private Patient updatePatientFields(Patient patient, PatientRequestDTO patientRequestDTO){
        patient.setName(patientRequestDTO.name());
        patient.setEmail(patientRequestDTO.email());
        patient.setAddress(patientRequestDTO.address());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.dateOfBirth()));

        return repository.save(patient);
    }
}
