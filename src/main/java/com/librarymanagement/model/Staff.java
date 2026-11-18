
package com.librarymanagement.model;

import java.time.LocalDateTime;

public class Staff {

    private int staffId;
    private String fullName;
    private String username;
    private String passwordHash;
    private String staffRole;
    private boolean active;
    private LocalDateTime createdAt;

    // Constructor không tham số
    public Staff() {
    }

    // Constructor đầy đủ thông tin
    public Staff(int staffId, String fullName,
                 String username, String passwordHash,
                 String staffRole, boolean active,
                 LocalDateTime createdAt) {

        this.staffId = staffId;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.staffRole = staffRole;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Constructor dùng khi tạo nhân viên mới
    public Staff(String fullName, String username,
                 String passwordHash, String staffRole) {

        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.staffRole = staffRole;
        this.active = true;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
