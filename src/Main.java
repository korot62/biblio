import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Library library = new Library();

        library.createTable();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Library Management System =====");
            System.out.println("\n1. Add Paper Book");
            System.out.println("2. Add EBook");
            System.out.println("3. List Books");
            System.out.println("4. Search Book");
            System.out.println("5. Update Book");
	    System.out.println("6. Delete Book");
            System.out.println("7. CheckIn");
            System.out.println("8. CheckOut");
            System.out.println("9. Exit");

            System.out.print("Choose: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
//=================================
//  Add Paper book
//=================================

                case 1:

                    System.out.print("Title: ");
                    String pTitle = scanner.nextLine();

                    System.out.print("Author: ");
                    String pAuthor = scanner.nextLine();

                    System.out.print("Year: ");
                    int pYear = scanner.nextInt();

                    System.out.print("Pages: ");
                    int pages = scanner.nextInt();

                    scanner.nextLine();

                    Book paperBook = new PaperBook(
                            pTitle,
                            pAuthor,
                            pYear,
                            pages
                    );

                    library.addBookSql(paperBook);
                    break;

//=========================================
// Add Ebook
//=========================================
                case 2:

                    System.out.print("Title: ");
                    String eTitle = scanner.nextLine();

                    System.out.print("Author: ");
                    String eAuthor = scanner.nextLine();

                    System.out.print("Year: ");
                    int eYear = scanner.nextInt();

                    System.out.print("File size (MB): ");
                    scanner.nextLine();
                    double fileSize =
                              Double.parseDouble(
                              scanner.nextLine().replace(",", ".")
                                );

                    Book eBook = new EBook(
                            eTitle,
                            eAuthor,
			    eYear,
                            fileSize
                    );
                    library.addBookSql(eBook);

                    break;
//===================================================
//    List all books
//===================================================

                case 3:

                    library.listBooksSql();
                    break;
              
//=======================================================
//  Search book by title
//======================================================  

               case 4:

                    System.out.print("Enter title: ");
                    String searchTitle = scanner.nextLine();

                    library.searchBookSql(searchTitle);

                    break;
//========================================================
//   Update book
//========================================================

		case 5:

		    System.out.print("Book ID: ");

                              int id =
                                     Integer.parseInt(
                                      scanner.nextLine()
                            );

                    System.out.print("New title: ");
                    String title =
                            scanner.nextLine();

                    System.out.print("New author: ");
                    String author =
                            scanner.nextLine();

                    library.updateBookSql(
                            id,
                            title,
                            author
                    );
                 break;
//=========================================================
//    Delete book by title
//=========================================================


                case 6:

                    System.out.print("Book ID: ");

                    int idDel =
                            Integer.parseInt(
                                    scanner.nextLine()
                            );

                    library.deleteBookSql(idDel);
                    break;
//=========================================================
// CheckIN
//========================================================
               case 7:

                    System.out.print("Title: ");
                    title =  scanner.nextLine();

                    library.checkIn(title);
                    break;                   
//==========================================================
//  CheckOut
//==========================================================

                case 8:

                     System.out.print("Title: ");
                     title = scanner.nextLine();

                     library.checkOut(title);
                     break;                   
//==========================================================
//  Exit
//==========================================================
                case 9:
                    
                    System.out.println("Goodbye!");
                    return;

                default:

                    System.out.println("Invalid option.");


                }
}
}
}
