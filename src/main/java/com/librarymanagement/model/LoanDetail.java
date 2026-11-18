
package com.librarymanagement.model;

import java.time.LocalDate;

public class LoanDetail {

    private int loanDetailId;
    private int loanId;
    private int bookId;
    private LocalDate returnDate;
    private String status;
    private String notes;

    // Constructor không tham số
    public LoanDetail() {
    }

    // Constructor đầy đủ thông tin
    public LoanDetail(int loanDetailId, int loanId,
                      int bookId, LocalDate returnDate,
                      String status, String notes) {

        this.loanDetailId = loanDetailId;
        this.loanId = loanId;
        this.bookId = bookId;
        this.returnDate = returnDate;
        this.status = status;
        this.notes = notes;
    }

    // Constructor dùng khi thêm sách vào phiếu mượn
    public LoanDetail(int loanId, int bookId) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.status = "BORROWING";
    }

    public int getLoanDetailId() {
        return loanDetailId;
    }

    public void setLoanDetailId(int loanDetailId) {
        this.loanDetailId = loanDetailId;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
