package Sources.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AddBookDlg extends JDialog {
    private JPanel contentPane;
    private JButton ValidateBtn;
    private JButton CancelBtn;
    private JPanel BtnPanel;
    private JPanel PreviewPhotoPanel;
    private JLabel BookAlreadyReadLabel;
    private JComboBox ExitingBookComboBox;
    private JLabel NewBookLabel;
    private JLabel NameLabel;
    private JTextField BookNameTextField;
    private JTextField BookAuthorTextField;
    private JLabel AuthorLabel;
    private JLabel ReleaseYearLabel;
    private JLabel PersonalNoteLabel;
    private JSpinner BookPersonalNoteSpin;
    private JLabel StartReadingLabel;
    private JCheckBox BookUnknownReadDateChecbox;
    private JPanel DateReadPanel;
    private JPanel RightPanel;
    private JLabel PhotoLabel;
    private JButton BookBrowseBtn;
    private JLabel NoteBblLabel;
    private JSpinner BookNoteBblSpin;
    private JLabel SummaryLabel;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookEndReadingSpin;
    private JSpinner BookNumberOPSpin;
    private JLabel NumberOPLabel;
    private JPanel LeftPanel;
    private JCheckBox BookNotDoneReadChecbox;
    private JLabel EndReadingLabel;
    private JSpinner BookStartReadingSpin;

    private String m_author;
    private String m_title;
    private String m_URL="";
    private boolean m_isValide = false;//Useful for determinate if the input are good
    private Connection m_connection;
    private Statement m_statement;

    public AddBookDlg() {
        setContentPane(contentPane);
        fillBookCombobox();
        setModal(true);
        initComponents();
        initComponents(true);

        BookUnknownReadDateChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(BookUnknownReadDateChecbox.isSelected()){//Hide the possibility to add a reading date when we don't know when you read it
                    BookEndReadingSpin.setEnabled(false);
                    BookStartReadingSpin.setEnabled(false);
                    BookNotDoneReadChecbox.setEnabled(false);
                }
                else{
                    BookEndReadingSpin.setEnabled(true);
                    BookStartReadingSpin.setEnabled(true);
                    BookNotDoneReadChecbox.setEnabled(true);
                }
            }
        });
        BookNotDoneReadChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (BookNotDoneReadChecbox.isSelected())
                    BookEndReadingSpin.setEnabled(false);
                else
                    BookEndReadingSpin.setEnabled(true);
            }
        });
        ExitingBookComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {//set the title and the author, retrieved by the ComboBox
                if(ExitingBookComboBox.getSelectedItem() != ""){
                    initComponents(false);
                    String row = Objects.requireNonNull(ExitingBookComboBox.getSelectedItem()).toString();//get the item that we chose
                    String author = "";
                    m_title = "";
                    int i = 0;
                    do {//As long as the character in the string is not '-', we add to our title variable the letter
                        m_title += row.charAt(i);//at the end we have juste the title
                        i++;
                    } while (row.charAt(i+1)!= '-');
                    author = row.replace(m_title, "");//We recover only the end of the line to keep only the author
                    m_author = author.substring(3 , author.length());
                    try{
                        Class.forName("org.sqlite.JDBC");
                        m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                        m_statement = m_connection.createStatement();
                        ResultSet ImageQry = m_statement.executeQuery("SELECT Image FROM Book WHERE Title='"+m_title+"' AND Author='"+m_author+ "'");//Retrieved from the bdd the URL of the book image
                        // in parameters the title and the author enter in parameters of this function
                        addImageToPanel(ImageQry.getString(1));//add the image of the book, with the author and the title recovered on the combobox, in our panel
                        setURL(ImageQry.getString(1));
                        m_connection.close();
                        m_statement.close();
                    } catch ( Exception e ) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                        System.exit(0);
                    }
                }
                else{
                    setURL("");
                    initComponents(true);
                }
            }
        });
        CancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//Quit dlg without taking into account the input
                m_isValide = false;
                setVisible(false);
                dispose();
            }
        });
        ValidateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading";
                if(getIsAlreadyRead() && getExitingBookComboBox().getSelectedItem()!=""){//Can add a new reading if the book exists at the same reading date
                    try {
                        Class.forName("org.sqlite.JDBC");
                        m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                        m_statement = m_connection.createStatement();
                        ResultSet qry = m_statement.executeQuery(sql);

                        boolean bookFind =false;
                        while (qry.next() && !bookFind){//
                            if (!isDateUnknown() && !isNotDOne() && Objects.equals(qry.getString(3), getNewBookStartReading())
                                    && Objects.equals(qry.getString(4), getNewBookEndReading())){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "Les dates de lecture existent déjà !");
                                bookFind = true;//
                            } else if (!isDateUnknown() && isNotDOne() && Objects.equals(qry.getString(3), getNewBookStartReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                                bookFind = true;//
                            } else if (!isDateUnknown() && !isNotDOne() && Objects.equals(qry.getString(4), getNewBookEndReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de fin de lecture existe déjà !");
                                bookFind = true;//
                            } else
                                bookFind = false;
                        }
                        if (!bookFind && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && isDateUnknown()){
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && isNotDOne()) {
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()){
                            //If a book has not been found in the database when leaving the loop, then the book typed is valid
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        }
                        else if(!bookFind && Objects.equals(getNewBookStartReading(), getNewBookEndReading())
                                && !isDateUnknown() && !isNotDOne()){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être identique à la fin de lecture !");
                        }
                        m_connection.close();
                        m_statement.close();
                    }catch (Exception e){
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                        System.exit(0);
                    }
                }
                else if(getIsAlreadyRead() && getExitingBookComboBox().getSelectedItem()==""){//Verif if we select a book in combobox
                    initComponents(true);
                }
                else if(!getIsAlreadyRead() && !Objects.equals(getNewBookAuthor(), "") && !Objects.equals(getNewBookTitle(), "") && !Objects.equals(getNewBookSummary(), "")){//Verif if the input are good to quit the dlg and recovered the data for bdd
                    try{//Can add a new reading if the book already exist
                        Class.forName("org.sqlite.JDBC");
                        m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                        m_statement = m_connection.createStatement();
                        ResultSet bookQry = m_statement.executeQuery(sql);

                        boolean bookFind =false;
                        while (bookQry.next() && !bookFind){//We browse the database until we find a book that already exists, in relation to the book created
                            if (Objects.equals(bookQry.getString(1), getNewBookTitle()) && Objects.equals(bookQry.getString(2), getNewBookAuthor())){//If the created book is already in the database, we exit the loop by setting an error dialog
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "Le livre existe déjà !");
                                bookFind = true;//If you have found a book, you are out of the loop
                            }
                            else
                                bookFind = false;
                        }
                        if (!bookFind && Objects.equals(getURL(), "") && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()){
                            //If a book has not been found in the database when leaving the loop, then the book typed is valid
                            String path = String.valueOf(getClass().getResource("/Ressource/Default.jpg"));
                            String path2 = path.replace("file:/", "");
                            setURL(path2);//create default image if we did'nt choice an image
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && Objects.equals(getURL(), "") && !isDateUnknown() && isNotDOne()) {
                            String path = String.valueOf(getClass().getResource("/Ressource/Default.jpg"));
                            String path2 = path.replace("file:/", "");
                            setURL(path2);
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && isDateUnknown() && Objects.equals(getURL(), "")) {
                            String path = String.valueOf(getClass().getResource("/Ressource/Default.jpg"));
                            String path2 = path.replace("file:/", "");
                            setURL(path2);
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && isDateUnknown() && !Objects.equals(getURL(), "")){
                            /*File source = new File(getURL());
                            File dest = new File(getURL());
                            copyFileUsingChannel(source, dest);*/
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getURL(), "") && !isDateUnknown() && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isNotDOne()) {
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getURL(), "") && !isDateUnknown() && isNotDOne()) {
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if(Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut être identique à la fin de lecture !");
                        }
                        m_connection.close();
                        m_statement.close();
                    } catch ( Exception e ) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                        System.exit(0);
                    }
                }
                else if(Objects.equals(getNewBookAuthor(), getNewBookTitle())){
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Le nom de l'auteur et le titre d'un livre ne peut pas être identique ! ");
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                }
            }
        });
        BookBrowseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf= new JFileChooser();
                if (JFileChooser.APPROVE_OPTION == jf.showOpenDialog(PreviewPhotoPanel)){ //Opens the file panel to select an image
                    String path = jf.getSelectedFile().getPath();//Sélection image
                    setURL(path);
                    addImageToPanel(path);
                }
            }
        });
    }

    public String getMTitle(){
        return m_title;
    }
    public String getAuthor(){
        return m_author;
    }
    public String getNewBookTitle(){//Get the new book title from JtextField
        return BookNameTextField.getText();
    }
    public String getNewBookAuthor(){
        return BookAuthorTextField.getText();
    }
    public String getNewBookNumberOP(){
        return BookNumberOPSpin.getValue().toString();
    }
    public String getNewBookReleaseYear(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have just the year release
        return formater.format(BookReleaseYearSpin.getValue());
    }
    public String getNewBookPersonalNote(){
        return BookPersonalNoteSpin.getValue().toString();
    }
    public String getNewBookBBLNote(){
        return BookNoteBblSpin.getValue().toString();
    }
    public String getNewBookSummary(){
        return BookSummaryTextPane.getText();
    }
    public String getNewBookEndReading(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        return formater.format(BookEndReadingSpin.getValue());
    }
    public String getNewBookStartReading(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        return formater.format(BookStartReadingSpin.getValue());
    }
    public boolean isDateUnknown(){
        return BookUnknownReadDateChecbox.isSelected();
    }
    public boolean isNotDOne(){
        return BookNotDoneReadChecbox.isSelected();
    }
    public boolean isValide(){
        return m_isValide;
    }
    public String getURL(){
        return m_URL;
    }
    public boolean getIsAlreadyRead(){
        if(ExitingBookComboBox.getSelectedItem()=="")
            return false;
        else
            return true;
        //return AlreadyReadChecbox.isSelected();
    }
    public JComboBox getExitingBookComboBox(){
        return ExitingBookComboBox;
    }

    public void fillBookCombobox(){
        ExitingBookComboBox.addItem("");
        try {
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();

            ResultSet rs = m_statement.executeQuery("SELECT Title, Author FROM Book GROUP BY Title, Author ORDER BY Title ASC;");
            while (rs.next()) {
                ExitingBookComboBox.addItem(rs.getString(1)+ " - " + rs.getString(2));//Filled the combobox with the data Recovered from the database
            }
            m_connection.close();
            m_statement.close();
            rs.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void addImageToPanel(String path){//Apply to our panel an image with path
        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(200, 300, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        PreviewPhotoPanel.updateUI();//reload the panel
        PreviewPhotoPanel.removeAll();
        PreviewPhotoPanel.add(imgLabel);
    }
    public void initComponents(){
        SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(5, 0, 5, 0.5);//Set a default and max value for spinner Note
        SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(5, 0, 5, 0.01);
        BookPersonalNoteSpin.setModel(BookPersonalNotelSM);
        BookNoteBblSpin.setModel(BookNoteBbblSM);

        Date dateRelease = new Date();
        SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(dateRelease,null,dateRelease,Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
        BookReleaseYearSpin.setEditor(Year);

        Date dateEnd = new Date();
        SpinnerDateModel BookEndReadSpinDate = new SpinnerDateModel(dateEnd, null,dateEnd, Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookEndReadingSpin.setModel(BookEndReadSpinDate);
        JSpinner.DateEditor ded = new JSpinner.DateEditor(BookEndReadingSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookEndReadingSpin.setEditor(ded);

        Date dateStart = new Date();
        SpinnerDateModel BookStartReadSpinDate = new SpinnerDateModel(dateStart, null,dateStart, Calendar.YEAR);
        BookStartReadingSpin.setModel(BookStartReadSpinDate);
        JSpinner.DateEditor start = new JSpinner.DateEditor(BookStartReadingSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookStartReadingSpin.setEditor(start);
    }
    public void initComponents(boolean bool){
        //ExitingBookComboBox.setEnabled(!bool);
        BookNameTextField.setEnabled(bool);
        BookAuthorTextField.setEnabled(bool);
        BookReleaseYearSpin.setEnabled(bool);
        BookNumberOPSpin.setEnabled(bool);
        BookPersonalNoteSpin.setEnabled(bool);
        BookNoteBblSpin.setEnabled(bool);
        BookSummaryTextPane.setEnabled(bool);
        BookReleaseYearSpin.setEnabled(bool);
        BookBrowseBtn.setEnabled(bool);
        //ExitingBookComboBox.setSelectedIndex(0);
        PreviewPhotoPanel.updateUI();
        PreviewPhotoPanel.removeAll();
    }
    public void setURL(String url){
        m_URL= url;
    }
    public static void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }finally{
            sourceChannel.close();
            destChannel.close();
        }
    }
}