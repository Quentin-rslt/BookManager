package Sources.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ManageReadingDlg extends JDialog {
    private JPanel contentPane;
    private JPanel BookListPanel;
    private JPanel BookBtnPanel;
    private JButton CancelBtn;


    private Statement m_statement;
    private JTable  m_bookListTable = new JTable(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private String m_title = "";
    private String m_author = "";
    private String m_startReading = "";
    private String m_endReading = "";
    private JPopupMenu m_popup;
    private boolean m_isEmpty = false;
    private int m_row;

    public ManageReadingDlg(String title, String author) {
        setContentPane(contentPane);
        setMTitle(title);
        setAuthor(author);
        fillBookList();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer");
        JMenuItem edit = new JMenuItem("Modifier");
        m_popup.add(cut);
        m_popup.add(edit);

        setModal(true);
        CancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(m_bookListTable.getRowCount()>0)
                    setIsEmpty(false);
                else
                    setIsEmpty(true);
                setVisible(false);
                dispose();
            }
        });
        m_bookListTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                setRow(m_bookListTable.rowAtPoint(evt.getPoint()));
                setMTitle(m_bookListTable.getValueAt(getRow(), 0).toString()); //get the value of the column of the table
                setAuthor(m_bookListTable.getValueAt(getRow(), 1).toString());
                setStartReading(m_startReading = m_bookListTable.getValueAt(getRow(), 2).toString());
                setEndReading(m_endReading = m_bookListTable.getValueAt(getRow(), 3).toString());
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    m_bookListTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(contentPane, evt.getX(), evt.getY());//show a popup to edit the reading
                }
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Delete in bdd the item that we want delete
                String BookQry = "DELETE FROM Book WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Delete in bdd the item that we want delete
                if(m_bookListTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                    try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry, 1); PreparedStatement BookPstmt = conn.prepareStatement(BookQry)) {
                        ReadingPstmt.executeUpdate();
                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        fillBookList();
                        resetIdReading(getMTitle(), getAuthor(), getRowCount());//refresh all ID in the table ReadingDate
                        conn.close();
                        ReadingPstmt.close();

                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                else{
                    JFrame jFrame = new JFrame();
                    int n = JOptionPane.showConfirmDialog(//Open a optionPane to verify if the user really want to delete the book return 0 il they want and 1 if they refuse
                            jFrame,
                            "Cette acion supprimeras complétement le livre et sera irréversible. \n"+"Etes-vous sûr de vouloir le faire ?",
                            "An Inane Question",
                            JOptionPane.YES_NO_OPTION);
                    if(n==0){
                        try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement BookPstmt = conn.prepareStatement(BookQry)) {
                            ReadingPstmt.executeUpdate();
                            BookPstmt.executeUpdate();
                            fillBookList();
                            contentPane.updateUI();
                            BookListPanel.removeAll();
                            conn.close();
                            BookPstmt.close();
                            ReadingPstmt.close();

                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        });
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                EditReadingDlg diag = new EditReadingDlg(getMTitle(), getAuthor(),getStartReading(), getEndReading());//Open a dialog where we can edit the date reading
                diag.setTitle("Modifier une lecture");
                diag.setSize(500,200);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if(diag.isValid()){
                    String sql = "UPDATE Reading SET StartReading=?, EndReading=?" +
                            "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Edit in bdd the item that we want to change the reading date
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        // execute the uptdate statement
                        pstmt.setString(1, diag.getNewStartReading());
                        pstmt.setString(2, diag.getNewEndReading());
                        pstmt.executeUpdate();
                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        fillBookList();
                        m_bookListTable.setRowSelectionInterval(getRow(), getRow());//Focus on the reading that we edit
                        conn.close();
                        pstmt.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
    }

    private Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
    public String getAuthor() {
        return m_author;
    }
    public String getMTitle() {
        return m_title;
    }
    public String getStartReading() {
        return m_startReading;
    }
    public String getEndReading() {
        return m_endReading;
    }
    public boolean isEmpty() {
        return m_isEmpty;
    }
    public int getRow() {
        return m_row;
    }
    public int getRowCount(){
        return m_bookListTable.getRowCount();
    }
    public String getReadingTime(String start, String en){
        String time = "";
        return time;
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setMTitle(String m_title) {
        this.m_title = m_title;
    }
    public void setIsEmpty(boolean m_isEmpty) {
        this.m_isEmpty = m_isEmpty;
    }
    public void setStartReading(String m_dateReading) {
        this.m_startReading = m_dateReading;
    }
    public void setEndReading(String m_dateReading) {
        this.m_endReading = m_dateReading;
    }
    public void setRow(int m_row) {
        this.m_row = m_row;
    }
    public void resetIdReading(String title, String author, int rowCount){
        String ReadingQry = "DELETE FROM Reading WHERE Title='"+title+"' AND Author='"+author+"'";//clear all the table
        String InsetrQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                "VALUES (?,?,?,?,?);";
        try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement InsetrPstmt = conn.prepareStatement(InsetrQry)){
            ReadingPstmt.executeUpdate();//Delete all the table
            for(int i=0;i<rowCount; i++){//filled the table against the bookList
                InsetrPstmt.setInt(1, i);
                InsetrPstmt.setString(2, title);
                InsetrPstmt.setString(3, author);
                InsetrPstmt.setString(4, m_bookListTable.getValueAt(i, 2).toString());
                InsetrPstmt.setString(5, m_bookListTable.getValueAt(i, 3).toString());
                InsetrPstmt.executeUpdate();
            }
            conn.close();
            ReadingPstmt.close();
            InsetrPstmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    public void fillBookList(){
        m_tableModel.setRowCount(0);
        try(Connection conn = connect()){
            m_statement = conn.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT Title, Author, StartReading, EndReading FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+ "'");

            while (qry.next()){
                String title = qry.getString("Title");//Retrieve the title
                String author = qry.getString("Author");//Retrieve the author
                String startReading = qry.getString("StartReading");
                String endReading = qry.getString("EndReading");

                long days = 0;
                String StdDays="";
                if((qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini"))){
                } else if((!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini"))){
                } else if((qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Inconnu")) ||
                        ((qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Pas fini")))){
                }
                else{
                    LocalDate start = LocalDate.parse(qry.getString("StartReading")) ;
                    LocalDate stop = LocalDate.parse(qry.getString("EndReading")) ;
                    days = ChronoUnit.DAYS.between(start , stop);
                    StdDays = days+" jours";
                }

                String[] header = {"Titre","Auteur","Début de lecture", "Fin de lecture", "Temps de lecture"};
                Object[] data = {title, author, startReading, endReading, StdDays};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data

                m_bookListTable.setModel(m_tableModel);
                m_bookListTable.setFocusable(false);
                m_pane = new JScrollPane(m_bookListTable);//Create a scrollpane with the Jtable for the error that did not display the header

                BookListPanel.add(m_pane);//add the scrolpane to our Jpanel
            }
            qry.close();
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}