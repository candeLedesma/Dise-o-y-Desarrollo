package presenter;

import fulllogic.DataBase;
import fulllogic.SearchResult;
import model.SearchModel;
import view.SearchView;

import java.util.LinkedList;

public class SearchPresenter {


    private SearchView view;

    private SearchModel model;

    private Thread taskThread;

    public SearchPresenter(SearchModel model) {
        this.model = model;
        model.setPresenter(this);
    }

    public void start(){
        view = new SearchView(this);
        view.showView();

        DataBase.loadDatabase();
    }



    public void searchSeries() {
        taskThread = new Thread(() -> {
            String seriesName = view.getSeriesName();

            LinkedList<SearchResult> results = model.searchSeries(seriesName);

            view.showResults(results);
        });

        taskThread.start();

    }

    public void getSelectedExtract(SearchResult selectedResult){
        String extract = model.searchPageExtract(selectedResult);
        view.setSearchResultTextPane(extract);
    }
}
