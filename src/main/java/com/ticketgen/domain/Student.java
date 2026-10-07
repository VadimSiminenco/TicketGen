package com.ticketgen.domain;

public record Student(String group, String lastName, String firstName) {
    public Student {
        group = group.trim();
        lastName = lastName.trim();
        firstName = firstName.trim();
        if (group.isEmpty() || lastName.isEmpty() || firstName.isEmpty()) {
            throw new IllegalArgumentException("Student identity is incomplete");
        }
    }

    public String label() { return lastName + " " + firstName; }
    public String key() { return lastName + "\u001f" + firstName; }
}
