package amu.editor;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientController {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;
    private PrintWriter out;
    private BufferedReader in;

    public void setConnection(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    @FXML
    public void initialize() {
        handleRefresh(); // get last version of the document

        // Activate "Delete Line" option when a line is selected
        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, string, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);

            // For editing selected line
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
    }

    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        String newLine = "(New Line)";
        if (selectedIndex == -1) {
            listView.getItems().add("(New Line)");
            sendRequest("ADDL " + listView.getItems().size() + " " + newLine);
        } else {
            // new line added below selected line
            listView.getItems().add(selectedIndex+1, "(New Line)");
            sendRequest("ADDL " + (selectedIndex + 1) + " " + newLine);
        }
        //TODO request server to add a new line
    }

    @FXML
    private void handleDeleteLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            listView.getItems().remove(selectedIndex);
            sendRequest("RMVL " + selectedIndex);
        }
        // TODO request server to remove line
    }

    @FXML
    private void handleTextFieldUpdate() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        String newText = textField.getText();
        if (selectedIndex != -1) {
            listView.getItems().set(selectedIndex, textField.getText());
            listView.getItems().set(selectedIndex, newText);
            sendRequest("MDFL " + selectedIndex + " " + newText);
        }
        // TODO request server to modify line
    }

    @FXML
    private void  handleRefresh() {
        // TODO request server last version of the document
        /*String[] textSample = { "FIRST WITCH  When shall we three meet again?\n",
                "   In thunder, lightning, or in rain?\n",
                "SECOND WITCH  When the hurly-burly’s done\n",
                "   When the battle’s lost and won.\n",
                "THIRD WITCH  That will be ere the set of sun\n"};*/
        listView.getItems().clear();
        /*for(String line : textSample){
            listView.getItems().add(line);
        }*/
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("DONE")) break;
                listView.getItems().add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String request) {
        if (out != null) {
            out.println(request);
        }
    }

}

