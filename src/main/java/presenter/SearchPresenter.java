package presenter;

import fulllogic.SearchResult;

public interface SearchPresenter {

    void searchSeries();

    void getSelectedExtract(SearchResult searchResult);

    void deleteStoredInfo();

    void saveStoredInfo();
}
