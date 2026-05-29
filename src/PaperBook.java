public class PaperBook extends Book {
   int pages;

    public PaperBook(String title, String author, int year, int pages) {
        super(title, author, year);
        this.pages = pages;
    }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

}
