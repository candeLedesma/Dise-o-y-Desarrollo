package dyds.tvseriesinfo.fulllogic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainWindow {
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

  public MainWindow() {


    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://en.wikipedia.org/w/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build();

    WikipediaSearchAPI searchAPI = retrofit.create(WikipediaSearchAPI.class);
    WikipediaPageAPI pageAPI = retrofit.create(WikipediaPageAPI.class);

    comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));


    textPane1.setContentType("text/html");
    textPane2.setContentType("text/html");
    // this is needed to open a link in the browser

    textField1.addActionListener(actionEvent -> {System.out.println("ACCION!!!");});
    System.out.println("TYPED!!!");
    textField1.addPropertyChangeListener(propertyChangeEvent -> {
              System.out.println("TYPED!!!");
    });

    //ToAlberto: They told us that you were having difficulties understanding this code,
    //Don't panic! We added several helpful comments to guide you through it ;)

    // From here on is where the magic happends: querying wikipedia, showing results, etc.
    goButton.addActionListener(e -> new Thread(() -> {
              //This may take some time, dear user be patient in the meanwhile!
              setWorkingStatus();
              // get from service
              Response<String> callForSearchResponse;
              try {

                //ToAlberto: First, lets search for the term in Wikipedia
                callForSearchResponse = searchAPI.searchForTerm(textField1.getText() + " (Tv series) articletopic:\"television\"").execute();

                //Show the result for testing reasons, if it works, dont forget to delete!
                System.out.println("JSON " + callForSearchResponse.body());

                //ToAlberto: Very Important Comment 1
                //This is the code parses the string with the search results for the query
                //The string uses the JSON format to the describe the query and the results
                //So we will use the Google library for JSONs (Gson) for its parsing and manipulation
                //Basically, we will turn the string into a JSON object,
                //With such object we can acceses to its fields using get(fieldname) method provided by Gson
                Gson gson = new Gson();
                JsonObject jobj = gson.fromJson(callForSearchResponse.body(), JsonObject.class);
                JsonObject query = jobj.get("query").getAsJsonObject();
                Iterator<JsonElement> resultIterator = query.get("search").getAsJsonArray().iterator();
                JsonArray jsonResults = query.get("search").getAsJsonArray();

                //toAlberto: shows each result in the JSonArry in a Popupmenu
                JPopupMenu searchOptionsMenu = new JPopupMenu("Search Results");
                for (JsonElement je : jsonResults) {
                  JsonObject searchResult = je.getAsJsonObject();
                  String searchResultTitle = searchResult.get("title").getAsString();
                  String searchResultPageId = searchResult.get("pageid").getAsString();
                  String searchResultSnippet = searchResult.get("snippet").getAsString();

                  SearchResult sr = new SearchResult(searchResultTitle, searchResultPageId, searchResultSnippet);
                  searchOptionsMenu.add(sr);

                  //toAlberto: Adding an event to retrive the wikipage when the user clicks an item in the Popupmenu
                  sr.addActionListener(actionEvent -> {
                    try {
                      //This may take some time, dear user be patient in the meanwhile!
                      setWorkingStatus();
                      //Now fetch the info of the select page
                      Response<String> callForPageResponse = pageAPI.getExtractByPageID(sr.pageID).execute();

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
                        text = "No Results";
                      } else {
                        text = "<h1>" + sr.title + "</h1>";
                        selectedResultTitle = sr.title;
                        text += searchResultExtract2.getAsString().replace("\\n", "\n");
                        text = textToHtml(text);
                      }
                      textPane1.setText(text);
                      textPane1.setCaretPosition(0);
                      //Back to edit time!
                      setWatingStatus();
                    } catch (Exception e12) {
                      System.out.println(e12.getMessage());
                    }
                  });
                }
                searchOptionsMenu.show(textField1, textField1.getX(), textField1.getY());
              } catch (IOException e1) {
                e1.printStackTrace();
              }

              //Now you can keep searching stuff!
              setWatingStatus();
    }).start());

    saveLocallyButton.addActionListener(actionEvent -> {
      if(text != ""){
        // save to DB  <o/
        DataBase.saveInfo(selectedResultTitle.replace("'", "`"), text);  //Dont forget the ' sql problem
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
        DataBase.saveInfo(comboBox1.getSelectedItem().toString().replace("'", "`"), textPane2.getText());  //Dont forget the ' sql problem
        //comboBox1.setModel(new DefaultComboBoxModel(DataBase.getTitles().stream().sorted().toArray()));
    });
    storedInfoPopup.add(saveItem);

    textPane2.setComponentPopupMenu(storedInfoPopup);


  }


  private void setWorkingStatus() {
    for(Component c: this.searchPanel.getComponents()) c.setEnabled(false);
    textPane1.setEnabled(false);
  }

  private void setWatingStatus() {
    for(Component c: this.searchPanel.getComponents()) c.setEnabled(true);
    textPane1.setEnabled(true);
  }

  public static void main(String[] args) {
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


    JFrame frame = new JFrame("TV Series Info Repo");


    frame.setContentPane(new MainWindow().contentPane);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

    DataBase.loadDatabase();
    DataBase.saveInfo("test", "sarasa");


    System.out.println(DataBase.getExtract("test"));
    System.out.println(DataBase.getExtract("nada"));
  }

  public static String textToHtml(String text) {

    StringBuilder builder = new StringBuilder();

    builder.append("<font face=\"arial\">");

    String fixedText = text
        .replace("'", "`"); //Replace to avoid SQL errors, we will have to find a workaround..

    builder.append(fixedText);

    builder.append("</font>");

    return builder.toString();
  }

}
