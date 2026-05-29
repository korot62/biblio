import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Library {

    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";

    public static final String GREEN = "\u001B[32m";


    // =====================================
    // CREATE TABLE
    // =====================================

    public void createTable() {

        String sql = """
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                title TEXT,
                author TEXT,
                year INTEGER,
                extra TEXT,
                available BOOLEAN
            );
            """;

        try (
                Connection conn =
                        DatabaseManager.connect();

                Statement stmt =
                        conn.createStatement()
        ) {

            stmt.execute(sql);

            System.out.println("Table ready.");

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }


    // =====================================
    // SQLITE INSERT
    // =====================================

    public void addBookSql(Book book) {

        String sql =
                "INSERT INTO books(type, title, author, year, extra, available) VALUES(?,?,?,?,?,?)";

        try (
                Connection conn =
                        DatabaseManager.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            if (book instanceof PaperBook pb) {

                stmt.setString(1, "PAPER");
                stmt.setString(5,
                        String.valueOf(pb.getPages()));

            } else if (book instanceof EBook eb) {

                stmt.setString(1, "EBOOK");
                stmt.setString(5,
                        String.valueOf(eb.getFileSize()));
            }

            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setInt(4, book.getYear());
            stmt.setBoolean(6, true);

            stmt.executeUpdate();

            System.out.println("Inserted into DB!");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }



    // =====================================
    // SQLITE LIST
    // =====================================

    public void listBooksSql() {

    String sql = "SELECT * FROM books";

    try (
            Connection conn = DatabaseManager.connect();

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql)
    ) {

        System.out.println();

        System.out.printf(
        "%-3s %-8s %-27s %-18s %-6s %-10s %-10s\n",
        "ID",
        "TYPE",
        "TITLE",
        "AUTHOR",
        "YEAR",
        "EXTRA",
        "STATUS"
        );
        System.out.println(
                "---------------------------------------------------------------------------------------------"
        );

        while (rs.next()) {

            int id = rs.getInt("id");

            String type =
                    rs.getString("type");

            String title =
                    rs.getString("title");

            String author =
                    rs.getString("author");

            int year =
                    rs.getInt("year");

            String extra =
                    rs.getString("extra");

            boolean available =
                    rs.getBoolean("available");

            String status =
                   available
                            ? GREEN + "Available" + RESET 
                            : RED + "Borrowed" + RESET;

  
            System.out.printf(
                    "%-3d %-8s %-27s %-18s %-6d %-10s %-10s\n",
                    id,
                    type,
                    title,
                    author,
                    year,
                    extra,
                    status

            );
        }

    } catch (Exception e) {

        e.printStackTrace();
    }
}

    // =====================================
    // SQLITE SEARCH
    // =====================================

    public void searchBookSql(String title) {

        String sql =
                "SELECT * FROM books WHERE title LIKE ?";

        try (
                Connection conn =
                        DatabaseManager.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, "%" + title + "%");

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                System.out.println(
                        rs.getInt("id") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("author")
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =====================================
    // SQLITE UPDATE
    // =====================================

    public void updateBookSql(
            int id,
            String title,
            String author
    ) {

        String sql =
                "UPDATE books SET title=?, author=? WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, id);

            stmt.executeUpdate();

            System.out.println("Book updated.");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =====================================
    // SQLITE DELETE
    // =====================================

    public void deleteBookSql(int id) {

        String sql =
                "DELETE FROM books WHERE id=?";

        try (
                Connection conn =
                        DatabaseManager.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

            System.out.println("Book deleted.");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================
    // SQLITE  CheckOut
    // =====================================

     public void checkOut(String title) {

    String checkSql = "SELECT available FROM books WHERE LOWER(title)=LOWER(?)";
    String updateSql = "UPDATE books SET available=? WHERE LOWER(title)=LOWER(?)";
    try (
            Connection conn = DatabaseManager.connect();

            PreparedStatement checkStmt =
                    conn.prepareStatement(checkSql)
    ) {

        checkStmt.setString(1, title);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {

            boolean available =
                    rs.getBoolean("available");

            if (!available) {

                System.out.println(
                        "Book already borrowed."
                );

                return;
            }
        } else {

            System.out.println("Book not found.");
            return;
        }

        PreparedStatement updateStmt =
                conn.prepareStatement(updateSql);

        updateStmt.setBoolean(1, false);

        updateStmt.setString(2, title);

        updateStmt.executeUpdate();

        System.out.println("Book checked out.");

    } catch (Exception e) {

        e.printStackTrace();
    }
}



    // =====================================
    // SQLITE CheckIn
    // =====================================
   
     public void checkIn(String title) {

    String checkSql = "SELECT available FROM books WHERE LOWER(title)=LOWER(?)";
    String updateSql = "UPDATE books SET available=? WHERE LOWER(title)=LOWER(?)";
    try (
            Connection conn = DatabaseManager.connect();

            PreparedStatement checkStmt =
                    conn.prepareStatement(checkSql)
    ) {

        checkStmt.setString(1, title);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {

            boolean available =
                    rs.getBoolean("available");

            if (available) {

                System.out.println(
                        "Book already in library."
                );

                return;
            }

        } else {

            System.out.println("Book not found.");
            return;
        }

        PreparedStatement updateStmt =
                conn.prepareStatement(updateSql);

        updateStmt.setBoolean(1, true);

        updateStmt.setString(2, title);

        updateStmt.executeUpdate();

        System.out.println("Book returned.");

    } catch (Exception e) {

        e.printStackTrace();
    }
}




}
