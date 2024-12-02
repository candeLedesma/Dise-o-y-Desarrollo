package presenter;

import model.SearchModel;

public class Main {
    public static void main(String[] args) {
        SearchModel model = new SearchModel();
        SearchPresenter presenter = new SearchPresenter(model);
        presenter.start();
    }
}