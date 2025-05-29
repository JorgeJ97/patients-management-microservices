package com.api.patients_service.kafka;

import com.api.patients_service.model.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducer {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${kafka.topic.patient}")
    private String topic;


    public void sendEvent(Patient patient){
        PatientEvent event = buildPatientEvent(patient);

        try {
            kafkaTemplate.send(topic, event.toByteArray());

        }
        catch (Exception exception){
            log.error("Error sending PatientCreated event {}", event);
        }

    }

    private PatientEvent buildPatientEvent(Patient patient) {
        return  PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();
    }

}
