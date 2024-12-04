package presenter;

import fulllogic.DataBase;
import fulllogic.SearchResult;
import model.SearchModelImp;
import view.SearchViewImpl;

import java.util.LinkedList;

public class SearchPresenterImp implements SearchPresenter {


    private SearchViewImpl view;

    private SearchModelImp model;

    private Thread taskThread;

    public SearchPresenterImp(SearchModelImp model) {
        this.model = model;
        model.setPresenter(this);
    }

    public void start(){
        view = new SearchViewImpl(this);
        view.showView();

        DataBase.loadDatabase();
    }


    @Override
    public void searchSeries() {
        taskThread = new Thread(() -> {
            String seriesName = view.getSeriesName();

            LinkedList<SearchResult> results = model.searchSeries(seriesName);

            view.showResults(results);
        });

        taskThread.start();

    }

    @Override
    public void getSelectedExtract(SearchResult selectedResult){
        String extract = model.searchPageExtract(selectedResult);
        view.setSearchResultTextPane(extract);
    }

    @Override
    public void deleteStoredInfo() {
        if (view.existSavedTitle()) {
            String title = view.getSeletedSavedTitle();
            model.deleteSavedInfo(title);
            view.setSelectSavedComboBox(model.getSavedTitles());
        }
    }

    @Override
    public void saveStoredInfo() {
        if (view.existSavedTitle()) {
            String title = view.getSeletedSavedTitle();
            String text = view.getSearchResultTextPane();
            model.saveStoredInfo(title, text);
        }
    }
}
