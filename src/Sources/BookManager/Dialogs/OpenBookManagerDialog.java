package Sources.BookManager.Dialogs;

import static Sources.BookManager.BookManager.*;
import static Sources.Common.getLogo;

public class OpenBookManagerDialog {
    public static AddBookDlg openAddBookDlg(){
        AddBookDlg diag = new AddBookDlg();
        diag.setTitle("Ajout d'un livre");
        diag.setIconImage(getLogo("add.png"));
        diag.setSize(1000,730);
        diag.setResizable(false);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static EditBookDlg openEditBookDlg(){
        EditBookDlg diag = new EditBookDlg();
        diag.setTitle("Modification du livre : " +getMTitle());
        diag.setIconImage(getLogo("edit.png"));
        diag.setResizable(false);
        diag.setSize(1000,730);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static AddReading openAddReadingDlg(){
        AddReading diag = new AddReading();
        diag.setTitle("Ajout d'une lecture pour le livre : "+getMTitle());
        diag.setSize(550,265);
        diag.setResizable(false);
        diag.setIconImage(getLogo("add.png"));
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static EditReadingDlg openEditReadingDlg(String startDate, String endDate){
        EditReadingDlg diag = new EditReadingDlg(startDate,endDate);
        diag.setIconImage(getLogo("edit.png"));
        diag.setTitle("Modification d'une lecture");
        diag.setSize(500,220);
        diag.setResizable(false);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static FiltersDlg openFilterDlg(){
        FiltersDlg diag = new FiltersDlg();
        diag.setTitle("Critères de recherche");
        diag.setResizable(false);
        diag.setIconImage(getLogo("search.png"));
        diag.setSize(730,700);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
}
