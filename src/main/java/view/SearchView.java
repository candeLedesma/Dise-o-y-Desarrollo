package view;

import fulllogic.SearchResult;

import java.util.LinkedList;

public interface SearchView {

    void showView();

    void showResults(LinkedList<SearchResult> results);

    void setSearchResultTextPane(String text);

    void setSelectSavedComboBox(Object[] savedTitles);

    String getSeriesName();

    boolean existSavedTitle();

    String getSeletedSavedTitle();
}
