package view;
import model.DataBaseImp;
import fulllogic.SearchResult;
import model.RatedSeries;
import presenter.SearchPresenterImp;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class SearchViewImpl implements SearchView {
    private final SearchPresenterImp searchPresenter;
    private JTextField textField1;
    private JButton searchButton;
    private JPanel contentPane;
    private JTextPane searchResultsTextPane;
    private JButton saveLocallyButton;
    private JTabbedPane textPaneRatedSeries;
    private JPanel searchPanel;
    private JPanel storagePanel;
    private JComboBox storedSeriesComboBox;
    private JTextPane storedInfoTextPane;
    private JPanel ratedSeriespanel;
    private JList<String> ratedSeriesList;
    private JSlider ratingSlider;

    DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    String selectedResultTitle = null; //For storage purposes, it may not coincide with the searched term (see below)
    String text = ""; //Last searched text! this variable is central for everything

    public SearchViewImpl(SearchPresenterImp searchPresenter) {
        this.searchPresenter = searchPresenter;
    }


    @Override
    public void showView() {

        JFrame frame = new JFrame("TV Series Info Repo");
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        setSearchPanel();

        setSavedPanel();



        searchPresenter.showAllRatedSeries(); // Load and display rated series

        setRatedSeriesPanel();

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

    private void setRatedSeriesPanel() {
        ratedSeriespanel.add(new JScrollPane(ratedSeriesList), BorderLayout.CENTER);
        textPaneRatedSeries.addTab("Rated Series", ratedSeriespanel);
    }
    private void setSavedPanel() {

        setComboBox();

        storedInfoTextPane.setContentType("text/html");

        setPopupMenu();

    }

    private void setPopupMenu() {
        JPopupMenu storedInfoPopup = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Delete!");
        deleteItem.addActionListener(actionEvent -> {
            System.out.println("eliminando "+ storedSeriesComboBox.getSelectedItem());
            searchPresenter.deleteStoredInfo();
        });
        storedInfoPopup.add(deleteItem);

        JMenuItem saveItem = new JMenuItem("Save Changes!");
        saveItem.addActionListener(actionEvent -> {
            System.out.println("Guardando "+ storedSeriesComboBox.getSelectedItem());
            searchPresenter.saveStoredInfo();
        });
        storedInfoPopup.add(saveItem);

        storedInfoTextPane.setComponentPopupMenu(storedInfoPopup);

    }

    private void setComboBox() {
       storedSeriesComboBox.setModel(new DefaultComboBoxModel(DataBaseImp.getTitles().stream().sorted().toArray()));
    }

    private void setSearchPanel() {
        searchResultsTextPane.setContentType("text/html");

        textField1.addPropertyChangeListener(propertyChangeEvent -> {
            System.out.println("TYPED!!!");
        });

        searchButton.addActionListener(e -> searchPresenter.searchSeries());

        saveLocallyButton.addActionListener(actionEvent -> {
            if (!text.isEmpty()) {
                DataBaseImp.saveInfo(selectedResultTitle.replace("'", "`"), text);
                storedSeriesComboBox.setModel(new DefaultComboBoxModel(DataBaseImp.getTitles().stream().sorted().toArray()));
            }
        });

        ratingSlider.setMinimum(1);
        ratingSlider.setMaximum(10);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);

        ratingSlider.addChangeListener(e -> {
            if (!ratingSlider.getValueIsAdjusting()) {
                searchPresenter.saveRating(); // Save the rating when the slider value is changed
            }
        });

        searchPanel.add(ratingSlider, BorderLayout.SOUTH); // Adjust layout as needed
    }


    private void setWorkingStatus() {
        for(Component c: this.searchPanel.getComponents()) c.setEnabled(false);
        searchResultsTextPane.setEnabled(false);
    }


    private void setWatingStatus() {
        for(Component c: this.searchPanel.getComponents()) c.setEnabled(true);
        searchResultsTextPane.setEnabled(true);
    }


    @Override
    public String getSeriesName() {
        return textField1.getText();
    }

    @Override
    public void showResults(LinkedList<SearchResult> results) {
        JPopupMenu searchOptionsMenu = new JPopupMenu("Search Results");
        for (SearchResult searchResult : results) {
            boolean isRated = searchPresenter.isRated(searchResult.title); // Método para verificar calificación
            String menuText = (isRated ? "⭐ " : "") + searchResult.title; // Prefijo visual

            JMenuItem menuItem = new JMenuItem(menuText);
            menuItem.addActionListener(actionEvent -> {
                System.out.println("Mostrando resultados: " + searchResult.title);
                selectedResultTitle = searchResult.title;
                searchPresenter.getSelectedExtract(searchResult);
                searchPresenter.loadRating(); // Actualizar JSlider con la puntuación
            });
            searchOptionsMenu.add(menuItem);
        }
        searchOptionsMenu.show(searchResultsTextPane, searchResultsTextPane.getX(), searchResultsTextPane.getY());
        setWatingStatus();
    }


    @Override
    public void setSearchResultTextPane(String text) {
        this.text = text;
        searchResultsTextPane.setText(text);
        searchResultsTextPane.setCaretPosition(0);
    }

    @Override
    public boolean existSavedTitle() {
        return storedSeriesComboBox.getSelectedItem() != null;
    }

    @Override
    public String getSeletedSavedTitle() {
        return (String) storedSeriesComboBox.getSelectedItem();
    }

    @Override
    public void showRating(int rating) {
        ratingSlider.setValue(rating);
    }

    @Override
    public int getRatingInput() {
        return ratingSlider.getValue();
    }

    @Override
    public void showRatedSeries(List<RatedSeries> ratedSeries) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (RatedSeries series : ratedSeries) {
            String displayText = String.format("%s - Rating: %d (Last updated: %s)",
                    series.getTitle(), series.getRating(), series.getLastUpdated());
            listModel.addElement(displayText);
        }
        ratedSeriesList.setModel(listModel);
        ratedSeriespanel.revalidate();
        ratedSeriespanel.repaint();
    }


    @Override
    public void setSelectSavedComboBox(Object[] savedTitles) {
        storedSeriesComboBox.setModel(new DefaultComboBoxModel(savedTitles));
        storedSeriesComboBox.addActionListener(actionEvent -> {
            System.out.println("Seleccioando "+ storedSeriesComboBox.getSelectedItem());
        });
    }

    public String getSearchResultTextPane() {
        return searchResultsTextPane.getText();
    }

}
