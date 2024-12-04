package model;

import fulllogic.SearchResult;

import java.util.LinkedList;

public interface SearchModel {
    LinkedList<SearchResult> searchSeries(String query);

    String searchPageExtract(SearchResult searchResult);

    void deleteSavedInfo(String title);

    Object[] getSavedTitles();

    void saveStoredInfo(String title, String text);
}
