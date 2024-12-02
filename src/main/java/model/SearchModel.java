package model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fulllogic.SearchResult;
import model.API.WikipediaPageAPI;
import model.API.WikipediaSearchAPI;
import presenter.SearchPresenter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import javax.swing.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static java.awt.SystemColor.text;
import static utils.TextoHTML.textToHtml;

public class SearchModel {

    private SearchPresenter searchPresenter;

    private WikipediaSearchAPI searchAPI;

    private WikipediaPageAPI pageAPI;

    private Gson gson;

    public SearchModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/w/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        searchAPI = retrofit.create(WikipediaSearchAPI.class);
        pageAPI = retrofit.create(WikipediaPageAPI.class);
        gson = new Gson();
    }

    public void setPresenter(SearchPresenter searchPresenter) {
        this.searchPresenter = searchPresenter;
    }

    public LinkedList<SearchResult> searchSeries(String seriesName) {

        Response<String> callForSearchResponse;

        LinkedList<SearchResult> searchResultsArray = new LinkedList<>();
        try {
            //ToAlberto: First, lets search for the term in Wikipedia
            callForSearchResponse = searchAPI.searchForTerm(seriesName + " (Tv series) articletopic:\"television\"").execute();

            //Show the result for testing reasons, if it works,
            System.out.println("JSON " + callForSearchResponse.body());

            //ToAlberto: Very Important Comment 1
            //This is the code parses the string with the search results for the query
            //The string uses the JSON format to the describe the query and the results
            //So we will use the Google library for JSONs (Gson) for its parsing and manipulation
            //Basically, we will turn the string into a JSON object,
            //With such object we can acceses to its fields using get(fieldname) method provided by Gson
            JsonObject jobj = gson.fromJson(callForSearchResponse.body(), JsonObject.class);
            JsonObject query = jobj.get("query").getAsJsonObject();
            Iterator<JsonElement> resultIterator = query.get("search").getAsJsonArray().iterator();
            JsonArray jsonResults = query.get("search").getAsJsonArray();

            //toAlberto: shows each result in the JSonArry in a Popupmenu
            //JPopupMenu searchOptionsMenu = new JPopupMenu("Search Results");
            for (JsonElement je : jsonResults) {
                JsonObject searchResult = je.getAsJsonObject();
                String searchResultTitle = searchResult.get("title").getAsString();
                String searchResultPageId = searchResult.get("pageid").getAsString();
                String searchResultSnippet = searchResult.get("snippet").getAsString();

                SearchResult sr = new SearchResult(searchResultTitle, searchResultPageId, searchResultSnippet);
                searchResultsArray.add(sr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchResultsArray;
    }


    public String searchPageExtract(SearchResult searchResult) {

        Response<String> callForPageResponse;

        String pageContent = "";

        try {
            //This may take some time, dear user be patient in the meanwhile!
            //setWorkingStatus();
            //Now fetch the info of the select page

            //Now fetch the info of the select page
            callForPageResponse = pageAPI.getExtractByPageID(searchResult.pageID).execute();

            System.out.println("JSON " + callForPageResponse.body());

            //toAlberto: This is similar to the code above, but here we parse the wikipage answer.
            //For more details on Gson look for very important coment 1, or just google it :P
            JsonObject jobj2 = gson.fromJson(callForPageResponse.body(), JsonObject.class);
            JsonObject query2 = jobj2.get("query").getAsJsonObject();
            JsonObject pages = query2.get("pages").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> pagesSet = pages.entrySet();
            Map.Entry<String, JsonElement> first = pagesSet.iterator().next();
            JsonObject page = first.getValue().getAsJsonObject();
            JsonElement searchResultExtract2 = page.get("extract");
            if (searchResultExtract2 == null) {
                pageContent = "No Results";
            } else {
                pageContent = "<h1>" + searchResult.title + "</h1>";
                //selectedResultTitle = searchResult.title;
                pageContent+= searchResultExtract2.getAsString().replace("\\n", "\n");
                pageContent = textToHtml(pageContent);
            }
            /*textPane1.setText(text);
            textPane1.setCaretPosition(0);
            //Back to edit time!
            setWatingStatus();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageContent;
    }
}
