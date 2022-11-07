package Sources;

import Sources.Dialogs.EditReadingDlg;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.openEditReadingDlg;

public class ManageReading {
    JTable m_readingsTable;
    private String m_title = "";
    private String m_author = "";
    private String m_startReading = "";
    private String m_endReading = "";
    final JPopupMenu m_popup;
    private int m_row;

    public ManageReading(MainWindow parent, String title, String author, JTable ReadingsTable) {
        this.m_readingsTable = ReadingsTable;
        setMTitle(title);
        setAuthor(author);
        parent.fillReadingsList(getMTitle(),getAuthor());

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);


        ReadingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
            setRow(ReadingsTable.rowAtPoint(evt.getPoint()));
            parent.setRowReading(getRow());
            setStartReading(m_startReading = ReadingsTable.getValueAt(getRow(), 0).toString());
            setEndReading(m_endReading = ReadingsTable.getValueAt(getRow(), 1).toString());
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    ReadingsTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(ReadingsTable, evt.getX(), evt.getY());//show a popup to edit the reading
                }
//                if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
//                    EditReadingDlg diag = openEditReadingDlg(getMTitle(), getAuthor(),getStartReading(), getEndReading());//Open a dialog where we can edit the date reading
//                    editReading(diag,getMTitle(), getAuthor(), parent, ManageReading.this);
//                }
            }
        });
        cut.addActionListener((ActionEvent evt) ->{
            String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Delete in bdd the item that we want delete
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            if(ReadingsTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                    ReadingPstmt.executeUpdate();
                    AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                    AvNumPstmt.executeUpdate();

                    parent.getContentPanel().updateUI();
                    parent.fillReadingsList(getMTitle(),getAuthor());
                    resetIdReading(getMTitle(), getAuthor(), getRowCount());//refresh all ID in the table ReadingDate
                    setRow(getRow()-1);
                    parent.setRowReading(getRow());
                    ReadingsTable.setRowSelectionInterval(getRow(), getRow());
                    //load bdd in MainWindow
                    parent.loadDB(parent.isFiltered());
                    isItInFilteredBookList(getMTitle(), getAuthor(), parent);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            else{
                deleteBook(parent);
            }
        });
        edit.addActionListener((ActionEvent evt) ->{
            EditReadingDlg diag = openEditReadingDlg(getMTitle(), getAuthor(),getStartReading(), getEndReading());//Open a dialog where we can edit the date reading
            editReading(diag,getMTitle(), getAuthor(), parent,this);
        });
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
    public int getRow() {
        return m_row;
    }
    public int getRowCount(){
        return m_readingsTable.getRowCount();
    }
    public JTable getReadingsTable() {
        return m_readingsTable;
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setMTitle(String m_title) {
        this.m_title = m_title;
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
                InsetrPstmt.setString(4, m_readingsTable.getValueAt(i, 0).toString());
                InsetrPstmt.setString(5, m_readingsTable.getValueAt(i, 1).toString());
                InsetrPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
