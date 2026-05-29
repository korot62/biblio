import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryApp extends JFrame {

    // ── Colours ──────────────────────────────────────────────
    private static final Color BG        = new Color(250, 250, 249);
    private static final Color SIDEBAR   = new Color(243, 242, 240);
    private static final Color WHITE     = Color.WHITE;
    private static final Color BORDER    = new Color(220, 218, 213);
    private static final Color TEXT      = new Color(28,  27,  25);
    private static final Color MUTED     = new Color(120, 118, 113);
    private static final Color ACCENT    = new Color(28,  27,  25);
    private static final Color GREEN_BG  = new Color(234, 243, 222);
    private static final Color GREEN_FG  = new Color(39,  80,  10);
    private static final Color AMBER_BG  = new Color(250, 238, 218);
    private static final Color AMBER_FG  = new Color(133, 79,  11);
    private static final Color BLUE_BG   = new Color(230, 241, 251);
    private static final Color BLUE_FG   = new Color(12,  68, 124);
    private static final Color PURPLE_BG = new Color(238, 237, 254);
    private static final Color PURPLE_FG = new Color(60,  52, 137);
    private static final Color RED_BG    = new Color(252, 235, 235);
    private static final Color RED_FG    = new Color(120, 31,  31);

    // ── State ─────────────────────────────────────────────────
    private String activeFilter = "ALL";
    private int    editingId    = -1;

    // ── UI Components ─────────────────────────────────────────
    private JTable          table;
    private DefaultTableModel model;
    private JTextField      searchField;
    private JLabel          lblTotal, lblAvail, lblOut, lblEbook;

    // Detail panel
    private JPanel     detailPanel;
    private JTextField fTitle, fAuthor, fYear, fExtra;
    private JComboBox<String> fType;
    private JLabel     lblExtra, lblPanelTitle;
    private JButton    btnSave, btnDelete;

    // ── Constructor ───────────────────────────────────────────
    public LibraryApp() {
        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        new Library().createTable();

        buildUI();
        refreshTable();
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════
    //  UI BUILDER
    // ═══════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildSidebar(),    BorderLayout.WEST);
        add(buildMainPanel(),  BorderLayout.CENTER);
        add(buildDetailPanel(),BorderLayout.EAST);
    }

    // ── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SIDEBAR);
        p.setPreferredSize(new Dimension(175, 0));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        p.add(Box.createVerticalStrut(16));
        p.add(sideLabel("МЕНЮ"));
        p.add(navBtn("Все книги",    "📚", () -> setFilter("ALL")));
        p.add(navBtn("Добавить",     "➕", this::openAdd));
        p.add(navBtn("Выдача / Возврат","🔄", null));

        p.add(Box.createVerticalStrut(16));
        p.add(sideLabel("ТИП"));
        p.add(navBtn("Все",          "▤",  () -> setFilter("ALL")));
        p.add(navBtn("Бумажные",     "📖", () -> setFilter("PAPER")));
        p.add(navBtn("Эл. книги",    "💻", () -> setFilter("EBOOK")));

        p.add(Box.createVerticalGlue());
        return p;
    }

    private JLabel sideLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(4, 14, 2, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton navBtn(String text, String icon, Runnable action) {
        JButton b = new JButton(icon + "  " + text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setForeground(TEXT);
        b.setBackground(SIDEBAR);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(175, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(BORDER); }
            public void mouseExited(MouseEvent e)  { b.setBackground(SIDEBAR); }
        });
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    // ── Main panel (toolbar + stats + table) ──────────────────
    private JPanel buildMainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel title = new JLabel("Библиотека");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(TEXT);
        toolbar.add(title);

        toolbar.add(Box.createHorizontalStrut(12));

        searchField = roundTextField("Поиск по названию или автору…", 220);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });
        toolbar.add(searchField);

        JButton addBtn = accentButton("+ Добавить книгу");
        addBtn.addActionListener(e -> openAdd());
        toolbar.add(addBtn);

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 0));
        stats.setBackground(BG);
        stats.setBorder(new EmptyBorder(12, 16, 0, 16));

        lblTotal = new JLabel("0"); lblAvail = new JLabel("0");
        lblOut   = new JLabel("0"); lblEbook = new JLabel("0");

        stats.add(statCard("Всего книг",  lblTotal, TEXT));
        stats.add(statCard("Доступно",    lblAvail, GREEN_FG));
        stats.add(statCard("Выдано",      lblOut,   AMBER_FG));
        stats.add(statCard("Эл. книг",    lblEbook, BLUE_FG));

        // Table
        String[] cols = {"ID","Тип","Название","Автор","Год","Доп.","Статус","Действия"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        scroll.getViewport().setBackground(WHITE);
        scroll.setBorder(new EmptyBorder(10, 16, 16, 16));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.add(stats, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(center,  BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color valColor) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(new Color(243, 242, 240));
        c.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(MUTED);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(valColor);

        c.add(lbl);
        c.add(valueLabel);
        return c;
    }

    private void styleTable() {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(WHITE);
        table.setSelectionBackground(BLUE_BG);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("SansSerif", Font.PLAIN, 11));
        h.setForeground(MUTED);
        h.setBackground(WHITE);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Column widths
        int[] widths = {36, 90, 200, 140, 54, 90, 90, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom renderer for badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
                lbl.setOpaque(true);
                lbl.setBackground(sel ? BLUE_BG : WHITE);
                lbl.setForeground(TEXT);

                if (col == 1) { // Type badge
                    String s = v != null ? v.toString() : "";
                    if (s.equals("PAPER")) { lbl.setForeground(BLUE_FG); lbl.setText("📖 Книга"); }
                    else                   { lbl.setForeground(PURPLE_FG); lbl.setText("💻 Ebook"); }
                } else if (col == 6) { // Status badge
                    String s = v != null ? v.toString() : "";
                    if (s.equals("Доступна")) { lbl.setForeground(GREEN_FG); lbl.setText("✓ Есть"); }
                    else                       { lbl.setForeground(AMBER_FG); lbl.setText("⏳ Выдана"); }
                } else if (col == 0) {
                    lbl.setForeground(MUTED);
                }
                return lbl;
            }
        });

        // Action buttons column
        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
                p.setBackground(sel ? BLUE_BG : WHITE);
                p.add(smallBtn("🔄", AMBER_BG, AMBER_FG));
                p.add(smallBtn("✎",  BLUE_BG,  BLUE_FG));
                return p;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                if (col == 7) {
                    Rectangle cellRect = table.getCellRect(row, col, false);
                    int relX = e.getX() - cellRect.x;
                    if (relX < 36) toggleAvailability(id);
                    else           openEdit(id);
                } else {
                    openEdit(id);
                }
            }
        });
    }

    private JButton smallBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new EmptyBorder(3, 7, 3, 7));
        b.setFocusPainted(false);
        return b;
    }

    // ── Detail / Edit panel ───────────────────────────────────
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(WHITE);
        detailPanel.setPreferredSize(new Dimension(240, 0));
        detailPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
        detailPanel.setVisible(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(14, 16, 10, 16));

        lblPanelTitle = new JLabel("Новая книга");
        lblPanelTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPanelTitle.setForeground(TEXT);
        header.add(lblPanelTitle, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        closeBtn.setForeground(MUTED);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> closePanel());
        header.add(closeBtn, BorderLayout.EAST);

        detailPanel.add(header);
        detailPanel.add(separator());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(10, 16, 10, 16));

        fTitle  = formField(form, "НАЗВАНИЕ");
        fAuthor = formField(form, "АВТОР");
        fYear   = formField(form, "ГОД");

        // Type selector
        form.add(formLabel("ТИП"));
        fType = new JComboBox<>(new String[]{"PAPER", "EBOOK"});
        fType.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        fType.setAlignmentX(LEFT_ALIGNMENT);
        fType.addActionListener(e -> updateExtraLabel());
        form.add(fType);
        form.add(Box.createVerticalStrut(10));

        lblExtra = formLabel("СТР.");
        form.add(lblExtra);
        fExtra = new JTextField();
        styleInput(fExtra);
        form.add(fExtra);
        form.add(Box.createVerticalStrut(16));

        detailPanel.add(form);
        detailPanel.add(separator());

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setBackground(WHITE);
        actions.setBorder(new EmptyBorder(12, 16, 16, 16));

        btnSave = accentButton("✓  Сохранить");
        btnSave.addActionListener(e -> saveBook());

        btnDelete = new JButton("🗑");
        btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnDelete.setForeground(RED_FG);
        btnDelete.setBackground(WHITE);
        btnDelete.setBorder(BorderFactory.createLineBorder(BORDER));
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteBook());

        actions.add(btnSave);
        actions.add(btnDelete);
        detailPanel.add(actions);

        return detailPanel;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        l.setForeground(MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField formField(JPanel parent, String label) {
        parent.add(formLabel(label));
        JTextField f = new JTextField();
        styleInput(f);
        parent.add(f);
        parent.add(Box.createVerticalStrut(10));
        return f;
    }

    private void styleInput(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    // ═══════════════════════════════════════════════════════════
    //  LOGIC
    // ═══════════════════════════════════════════════════════════
    private void refreshTable() {
        model.setRowCount(0);
        String q = searchField.getText().trim().toLowerCase();

        String sql = "SELECT * FROM books WHERE (LOWER(title) LIKE ? OR LOWER(author) LIKE ?)"
                + (activeFilter.equals("ALL") ? "" : " AND type=?");

        int total = 0, avail = 0, out = 0, ebook = 0;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + q + "%");
            ps.setString(2, "%" + q + "%");
            if (!activeFilter.equals("ALL")) ps.setString(3, activeFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int    id     = rs.getInt("id");
                String type   = rs.getString("type");
                String title  = rs.getString("title");
                String author = rs.getString("author");
                int    year   = rs.getInt("year");
                String extra  = rs.getString("extra");
                boolean ok    = rs.getBoolean("available");

                model.addRow(new Object[]{
                    id, type, title, author, year, extra,
                    ok ? "Доступна" : "Выдана", ""
                });
                total++;
                if (ok) avail++; else out++;
                if ("EBOOK".equals(type)) ebook++;
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        // Refresh stats from full DB (not just filtered)
        try (Connection conn = DatabaseManager.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) c, SUM(available) a, SUM(type='EBOOK') e FROM books")) {
            if (rs.next()) {
                lblTotal.setText(String.valueOf(rs.getInt("c")));
                lblAvail.setText(String.valueOf(rs.getInt("a")));
                lblOut.setText(String.valueOf(rs.getInt("c") - rs.getInt("a")));
                lblEbook.setText(String.valueOf(rs.getInt("e")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void setFilter(String f) {
        activeFilter = f;
        refreshTable();
    }

    private void openAdd() {
        editingId = -1;
        lblPanelTitle.setText("Новая книга");
        fTitle.setText(""); fAuthor.setText("");
        fYear.setText(String.valueOf(java.time.Year.now().getValue()));
        fType.setSelectedIndex(0);
        fExtra.setText("");
        btnDelete.setVisible(false);
        updateExtraLabel();
        detailPanel.setVisible(true);
        fTitle.requestFocus();
    }

    private void openEdit(int id) {
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                editingId = id;
                lblPanelTitle.setText("Редактировать");
                fTitle.setText(rs.getString("title"));
                fAuthor.setText(rs.getString("author"));
                fYear.setText(String.valueOf(rs.getInt("year")));
                fType.setSelectedItem(rs.getString("type"));
                fExtra.setText(rs.getString("extra"));
                btnDelete.setVisible(true);
                updateExtraLabel();
                detailPanel.setVisible(true);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void closePanel() {
        detailPanel.setVisible(false);
        editingId = -1;
    }

    private void updateExtraLabel() {
        String t = fType.getSelectedItem().toString();
        lblExtra.setText("PAPER".equals(t) ? "СТР." : "РАЗМЕР (MB)");
    }

    private void saveBook() {
        String title  = fTitle.getText().trim();
        String author = fAuthor.getText().trim();
        String yearS  = fYear.getText().trim();
        String type   = fType.getSelectedItem().toString();
        String extra  = fExtra.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Введите название и автора.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int year;
        try { year = Integer.parseInt(yearS); }
        catch (NumberFormatException e) { year = 2024; }

        Library lib = new Library();
        if (editingId == -1) {
            Book b = "PAPER".equals(type)
                ? new PaperBook(title, author, year, parseIntSafe(extra))
                : new EBook(title, author, year, parseDoubleSafe(extra));
            lib.addBookSql(b);
        } else {
            lib.updateBookSql(editingId, title, author);
            // also update year, type, extra
            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE books SET year=?, type=?, extra=? WHERE id=?")) {
                ps.setInt(1, year);
                ps.setString(2, type);
                ps.setString(3, extra);
                ps.setInt(4, editingId);
                ps.executeUpdate();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        closePanel();
        refreshTable();
    }

    private void deleteBook() {
        if (editingId < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Удалить книгу?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Library().deleteBookSql(editingId);
            closePanel();
            refreshTable();
        }
    }

    private void toggleAvailability(int id) {
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE books SET available = NOT available WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
        refreshTable();
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════
    private JTextField roundTextField(String placeholder, int width) {
        JTextField f = new JTextField() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(160, 158, 153));
                    g2.setFont(getFont());
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(width, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return f;
    }

    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(ACCENT);
        b.setForeground(WHITE);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private int    parseIntSafe(String s)    { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; } }
    private double parseDoubleSafe(String s) { try { return Double.parseDouble(s.trim().replace(",",".")); } catch (Exception e) { return 0.0; } }

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(LibraryApp::new);
    }
}
