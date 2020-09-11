package net.brianhuntley.bookrecommendations;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String img;

    public Book(String isbn, String title, String author, String img) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.img = img;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return  title + '\n' +
                author + '\n' +
                isbn + '\n';
    }
}
