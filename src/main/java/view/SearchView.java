package view;
import fulllogic.DataBase;
import fulllogic.SearchResult;
import presenter.SearchPresenter;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

import static utils.TextoHTML.textToHtml;

public class SearchView {
    private final SearchPresenter searchPresenter;
    private JTextField textField1;
    private JButton goButton;
    private JPanel contentPane;
    private JTextPane textPane1;
    private JButton saveLocallyButton;
    private JTabbedPane tabbedPane1;
    private JPanel searchPanel;
    private JPanel storagePanel;
    private JComboBox comboBox1;
    private JTextPane textPane2;

    DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    String selectedResultTitle = null; //For storage purposes, it may not coincide with the searched term (see below)
    String text = ""; //Last searched text! this variable is central for everything

    public SearchView(SearchPresenter searchPresenter) {
        this.searchPresenter = searchPresenter;

        comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));

        textPane1.setContentType("text/html");
        textPane2.setContentType("text/html");

        textField1.addPropertyChangeListener(propertyChangeEvent -> {
            System.out.println("TYPED!!!");
        });

        //ToAlberto: They told us that you were having difficulties understanding this code,
        //Don't panic! We added several helpful comments to guide you through it ;)

        // From here on is where the magic happends: querying wikipedia, showing results, etc.
        goButton.addActionListener(e ->  { searchPresenter.searchSeries();});

        saveLocallyButton.addActionListener(actionEvent -> {
            if(text != ""){
                // save to DB  <o/
                DataBase.saveInfo(selectedResultTitle.replace("'", "`"), text);
                comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));
            }
        });

        comboBox1.addActionListener(actionEvent -> textPane2.setText(textToHtml(DataBase.getExtract(comboBox1.getSelectedItem().toString()))));

        JPopupMenu storedInfoPopup = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Delete!");
        deleteItem.addActionListener(actionEvent -> {
            if(comboBox1.getSelectedIndex() > -1){
                DataBase.deleteEntry(comboBox1.getSelectedItem().toString());
                comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));
                textPane2.setText("");
            }
        });
        storedInfoPopup.add(deleteItem);

        JMenuItem saveItem = new JMenuItem("Save Changes!");
        saveItem.addActionListener(actionEvent -> {
            // save to DB  <o/
            DataBase.saveInfo(comboBox1.getSelectedItem().toString().replace("'", "`"), textPane2.getText());
            //comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));
        });
        storedInfoPopup.add(saveItem);

        textPane2.setComponentPopupMenu(storedInfoPopup);
    }


    public void showView() {
        setFrame();

        try {
            // Set System L&F
            UIManager.put("nimbusSelection", new Color(247,248,250));
            //UIManager.put("nimbusBase", new Color(51,98,140)); //This is redundant!

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong with UI!");
        }


    }

    private void setFrame() {
        JFrame frame = new JFrame("TV Series Info Repo");
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void setWorkingStatus() {
        for(Component c: this.searchPanel.getComponents()) c.setEnabled(false);
        textPane1.setEnabled(false);
    }


    private void setWatingStatus() {
        for(Component c: this.searchPanel.getComponents()) c.setEnabled(true);
        textPane1.setEnabled(true);
    }


    public String getSeriesName() {
        return textField1.getText();
    }

    public void showResults(LinkedList<SearchResult> results) {
        JPopupMenu searchOptionsMenu = new JPopupMenu("Search Results");
        for(SearchResult searchResult : results){
            searchResult.addActionListener(actionEvent -> {
                selectedResultTitle = searchResult.title;
                searchPresenter.getSelectedExtract(searchResult);

            });
            searchOptionsMenu.add(searchResult);
            searchOptionsMenu.show(textPane1, textPane1.getX(), textPane1.getY());
        }
        setWatingStatus();
    }

    public void setSearchResultTextPane(String text) {
        this.text = text;
        textPane1.setText(text);
        textPane1.setCaretPosition(0);
    }
}
