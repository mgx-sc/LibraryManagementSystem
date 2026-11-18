
package com.librarymanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reader {

    private int readerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime registeredAt;
    private boolean active;

    // Constructor không tham số
    public Reader() {
    }

    // Constructor đầy đủ thông tin
    public Reader(int readerId, String fullName,
                  LocalDate dateOfBirth, String phone,
                  String email, String address,
                  LocalDateTime registeredAt, boolean active) {

        this.readerId = readerId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.registeredAt = registeredAt;
        this.active = active;
    }

    // Constructor dùng khi thêm độc giả mới
    public Reader(String fullName, LocalDate dateOfBirth,
                  String phone, String email, String address) {

        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.active = true;
    }

    public int getReaderId() {
        return readerId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
