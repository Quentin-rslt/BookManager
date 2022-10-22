package Sources.Dialogs;

import Sources.RoundBorderCp;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static Sources.Common.*;

public class ManageReadingDlg extends JDialog {
    private JPanel contentPane;
    private JButton CancelBtn;
    private JLabel ManageTitleLabel;
    private JLabel ManageAuthorLabel;
    private JTable ReadingsTable;


    final DefaultTableModel m_tableModel = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
            //all cells false
            return false;
        }
    };
    private String m_title = "";
    private String m_author = "";
    private String m_startReading = "";
    private String m_endReading = "";
    final JPopupMenu m_popup;
    private boolean m_isEmpty = false;
    private int m_row;

    public ManageReadingDlg(String title, String author) {
        setContentPane(contentPane);
        setMTitle(title);
        setAuthor(author);
        fillBookList();
        ReadingsTable.setRowSelectionInterval(0, 0);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);

        setModal(true);
        CancelBtn.addActionListener((ActionEvent e)-> {
            setIsEmpty(ReadingsTable.getRowCount() <= 0);
            setVisible(false);
            dispose();
        });
        ReadingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                setRow(ReadingsTable.rowAtPoint(evt.getPoint()));
                setStartReading(m_startReading = ReadingsTable.getValueAt(getRow(), 0).toString());
                setEndReading(m_endReading = ReadingsTable.getValueAt(getRow(), 1).toString());
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    ReadingsTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(ReadingsTable, evt.getX(), evt.getY());//show a popup to edit the reading
                }
            }
        });
        cut.addActionListener((ActionEvent evt) ->{
            String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Delete in bdd the item that we want delete
            String BookQry = "DELETE FROM Book WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Delete in bdd the item that we want delete
            String TaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(getMTitle(),getAuthor())+"'";
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            if(ReadingsTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                    ReadingPstmt.executeUpdate();
                    AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                    AvNumPstmt.executeUpdate();

                    contentPane.updateUI();
                    fillBookList();
                    resetIdReading(getMTitle(), getAuthor(), getRowCount());//refresh all ID in the table ReadingDate
                    ReadingsTable.setRowSelectionInterval(0, 0);
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
                    try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement BookPstmt = conn.prepareStatement(BookQry);
                         PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {
                        ReadingPstmt.executeUpdate();
                        BookPstmt.executeUpdate();
                        TaggingPstmt.executeUpdate();
                        fillBookList();
                        ManageTitleLabel.setText("Lectures du livre : ");
                        ManageAuthorLabel.setText("Ecrit par : ");
                        contentPane.updateUI();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        edit.addActionListener((ActionEvent evt) ->{
            EditReadingDlg diag = new EditReadingDlg(getMTitle(), getAuthor(),getStartReading(), getEndReading());//Open a dialog where we can edit the date reading
            diag.setIconImage(getImageEdit());
            diag.setTitle("Modifier une lecture");
            diag.setSize(500,210);
            diag.setLocationRelativeTo(null);
            diag.setVisible(true);

            if(diag.isValid()){
                String sql = "UPDATE Reading SET StartReading=?, EndReading=?" +
                        "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Edit in bdd the item that we want to change the reading date
                String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                    // execute the uptdate statement
                    pstmt.setString(1, diag.getNewStartReading());
                    pstmt.setString(2, diag.getNewEndReading());
                    pstmt.executeUpdate();

                    AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                    AvNumPstmt.executeUpdate();

                    contentPane.updateUI();
                    fillBookList();
                    ReadingsTable.setRowSelectionInterval(getRow(), getRow());//Focus on the reading that we edit
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
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
    public boolean isEmpty() {
        return m_isEmpty;
    }
    public int getRow() {
        return m_row;
    }
    public int getRowCount(){
        return ReadingsTable.getRowCount();
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
                InsetrPstmt.setString(4, ReadingsTable.getValueAt(i, 0).toString());
                InsetrPstmt.setString(5, ReadingsTable.getValueAt(i, 1).toString());
                InsetrPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    public void fillBookList(){
        ManageTitleLabel.setText("Lectures du livre : "+getMTitle());
        ManageAuthorLabel.setText("Ecrit par : "+getAuthor());
        m_tableModel.setRowCount(0);
        try(Connection conn = connect()){
            Statement m_statement = conn.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT StartReading, EndReading FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+ "'");

            while (qry.next()){
                String startReading = qry.getString("StartReading");
                String endReading = qry.getString("EndReading");

                long days;
                String StdDays="";
                boolean isOk = ((qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini")))
                        ||((!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini")))
                        ||((qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Inconnu")) ||
                        (qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Pas fini")));
                if(!isOk){
                    LocalDate start = LocalDate.parse(qry.getString("StartReading")) ;
                    LocalDate stop = LocalDate.parse(qry.getString("EndReading")) ;
                    days = ChronoUnit.DAYS.between(start , stop);
                    StdDays = days+" jours";
                }

                String[] header = {"Début de lecture", "Fin de lecture", "Temps de lecture"};
                Object[] data = {startReading, endReading, StdDays};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data
            }
            ReadingsTable.setModel(m_tableModel);
            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, 400-(ReadingsTable.getRowCount()*ReadingsTable.getRowHeight()),0,0);
            if(ReadingsTable.getRowCount()>11)
                ReadingsTable.setBorder(roundBrdMax);
            else
                ReadingsTable.setBorder(roundBrdMin);

            qry.close();
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
