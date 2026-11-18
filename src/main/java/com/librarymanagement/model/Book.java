
package com.librarymanagement.model;

public class Book {

    private int bookId;
    private int categoryId;
    private String title;
    private String author;
    private String publisher;
    private Short publicationYear;
    private String isbn;
    private int totalQuantity;
    private int availableQuantity;
    private boolean active;

    // Constructor không tham số
    public Book() {
    }

    // Constructor đầy đủ thông tin
    public Book(int bookId, int categoryId, String title,
                String author, String publisher,
                Short publicationYear, String isbn,
                int totalQuantity, int availableQuantity,
                boolean active) {

        this.bookId = bookId;
        this.categoryId = categoryId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.active = active;
    }

    // Constructor dùng khi thêm sách mới
    public Book(int categoryId, String title, String author,
                String publisher, Short publicationYear,
                String isbn, int totalQuantity) {

        this.categoryId = categoryId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.active = true;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Short getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Short publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return title;
    }
}
