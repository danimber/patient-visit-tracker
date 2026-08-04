package com.imber.patientvisittracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    protected Doctor() {
    }

    public Doctor(String firstName, String lastName, String timezone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.timezone = timezone;
    }

}
