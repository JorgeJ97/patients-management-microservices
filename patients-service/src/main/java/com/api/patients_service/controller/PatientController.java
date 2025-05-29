package com.api.patients_service.controller;

import com.api.patients_service.dto.PatientRequestDTO;
import com.api.patients_service.dto.PatientResponseDTO;
import com.api.patients_service.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;


    @GetMapping("/get-patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients(){
        final var patients = patientService.getPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/create")
    public ResponseEntity<PatientResponseDTO> create(@Valid @RequestBody PatientRequestDTO request){
        final var newPatient = patientService.createPatient(request);
        return ResponseEntity.ok(newPatient);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PatientResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody PatientRequestDTO request){
        final var updatedPatient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
    }
