package application;

import java.io.File;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application
{
    private VBox root;
    private Stage stage;
    private CodeArea area;

    @Override
    public void start(Stage primaryStage)
    {
        stage = primaryStage;
        home(null);
    }

    /*
     * Display the home screen.
     */
    public void home(File file)
    {
        root = new VBox();

        // create menu buttons
        menu(file);

        // code area
        area = new CodeArea();
        root.getChildren().add(area);
        area.setId("area");
        area.prefWidthProperty().bind(root.widthProperty());
        area.prefHeightProperty().bind(root.heightProperty());

        // create context menu
        ContextMenu contextMenu = new ContextMenu();
        addContextItems(contextMenu);

        area.setOnMouseClicked(e ->
        {
            if (e.getButton() == MouseButton.SECONDARY)
            {
                contextMenu.show(area, e.getScreenX(), e.getScreenY());
            } else if (e.getButton() == MouseButton.PRIMARY)
            {
                contextMenu.hide();
            }
        });

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change ->
        {
            area.setStyleSpans(0, SyntaxHighlighter.computeHighlighting(area.getText()));
        });
        area.replaceText(0, 0, FileHelper.readFile(file));

        // create stage
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.setTitle(FileHelper.fileName(file));
        stage.setScene(scene);
        stage.show();
    }

    public void addContextItems(ContextMenu contextMenu)
    {
        MenuItem addKeywordItem = new MenuItem("Add Keyword");
        addKeywordItem.setOnAction(e ->
        {
            SyntaxHighlighter.addKeyword(area.getSelectedText());
        });
        contextMenu.getItems().addAll(addKeywordItem);
    }

    /*
     * Add menu.
     */
    public void menu(File file)
    {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu(file));

        root.getChildren().add(menuBar);
    }

    /*
     * Add file menu.
     */
    public Menu fileMenu(File file)
    {
        final Menu menu = new Menu("File");

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(event -> save(file));

        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        saveAsItem.setOnAction(event -> saveAs());

        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(event -> openFile());

        menu.getItems().addAll(saveItem, saveAsItem, openItem);

        return menu;
    }

    /*
     * Display an open file dialog and refresh the home screen based on selected
     * file.
     */
    public void openFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a text file");
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile != null)
            home(selectedFile);
    }

    /*
     * Given a file, save it. If no file is given, open the save as dialog.
     */
    public void save(File file)
    {
        if (file != null)
            FileHelper.saveFile(file, area.getText());
        else
            saveAs();
    }

    /*
     * Open a save as dialog to write the contents of the text area to a new file.
     */
    public void saveAs()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        File selectedFile = chooser.showSaveDialog(stage);
        if (selectedFile != null)
        {
            FileHelper.saveFile(selectedFile, area.getText());
            home(selectedFile);
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
