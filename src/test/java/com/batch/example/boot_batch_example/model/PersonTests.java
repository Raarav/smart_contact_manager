package com.batch.example.boot_batch_example.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PersonTests {

    @Test
    void supportsNoArgumentConstructionAndAllProperties() {
        Person person = new Person();
        person.setPersonId("person-1");
        person.setFirstName("Ada");
        person.setLastName("Lovelace");
        person.setContact("555-0100");
        person.setEmail("ada@example.com");

        assertThat(person.getPersonId()).isEqualTo("person-1");
        assertThat(person.getFirstName()).isEqualTo("Ada");
        assertThat(person.getLastName()).isEqualTo("Lovelace");
        assertThat(person.getContact()).isEqualTo("555-0100");
        assertThat(person.getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void supportsConstructionWithAllProperties() {
        Person person = new Person("person-2", "Grace", "Hopper", "555-0101", "grace@example.com");

        assertThat(person.getPersonId()).isEqualTo("person-2");
        assertThat(person.getFirstName()).isEqualTo("Grace");
        assertThat(person.getLastName()).isEqualTo("Hopper");
        assertThat(person.getContact()).isEqualTo("555-0101");
        assertThat(person.getEmail()).isEqualTo("grace@example.com");
    }
}
