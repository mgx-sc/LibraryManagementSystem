
package com.librarymanagement.model;

import java.time.LocalDate;

public class Loan {

    private int loanId;
    private int readerId;
    private int staffId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String notes;

    // Constructor không tham số
    public Loan() {
    }

    // Constructor đầy đủ thông tin
    public Loan(int loanId, int readerId, int staffId,
                LocalDate loanDate, LocalDate dueDate,
                String notes) {

        this.loanId = loanId;
        this.readerId = readerId;
        this.staffId = staffId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    // Constructor dùng khi tạo phiếu mượn mới
    public Loan(int readerId, int staffId,
                LocalDate dueDate, String notes) {

        this.readerId = readerId;
        this.staffId = staffId;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getReaderId() {
        return readerId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
