package presenter;

import model.DataBaseImp;
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

        DataBaseImp.loadDatabase();
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

    public void saveRating() {
        String title = view.getSeriesName();
        int rating = view.getRatingInput();
        model.saveRating(title, rating);
    }

    @Override
    public void loadRating() {
        String title = view.getSeletedSavedTitle();
        int rating = model.getRating(title);
        view.showRating(rating);
    }

    @Override
    public void showAllRatedSeries() {
        view.showRatedSeries(model.getAllRatedSeries());

    }

    @Override
    public boolean isRated(String title) {
        return model.isRated(title);
    }
}
