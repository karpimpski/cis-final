package application;

import java.io.File;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application
{
    private VBox root;
    private Stage stage;
    private CodeArea area;
    private SyntaxHighlighter highlighter;

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
        highlighter = new SyntaxHighlighter(file);
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
        setupContext(contextMenu);

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change ->
        {
            setHighlighting();
        });
        area.replaceText(0, 0, FileHelper.readFile(file));

        // create stage
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.setTitle(FileHelper.fileName(file));
        stage.setScene(scene);
        stage.show();
    }

    public void setupContext(ContextMenu contextMenu)
    {
        MenuItem addKeywordItem = new MenuItem("Add Keyword");

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

        addKeywordItem.setOnAction(e ->
        {
            highlighter.addKeyword(area.getSelectedText());
        });
        contextMenu.getItems().addAll(addKeywordItem);
    }

    public void setHighlighting()
    {
        area.setStyleSpans(0, highlighter.computeHighlighting(area.getText()));
    }

    /*
     * Add menu.
     */
    public void menu(File file)
    {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu(file), settingsMenu(file));

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

    public Menu settingsMenu(File file)
    {
        final Menu menu = new Menu("Settings");

        MenuItem commentPatternItem = new MenuItem("Comment Patterns");

        commentPatternItem.setAccelerator(
                new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        commentPatternItem.setOnAction(event -> commentPatternPopup());

        menu.getItems().addAll(commentPatternItem);

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

    public void commentPatternPopup()
    {
        Stage popupStage = new Stage();
        GridPane popupRoot = new GridPane();

        Label singleLineLabel = new Label("Single Line Start");
        popupRoot.add(singleLineLabel, 0, 0);
        TextField singleLineField = new TextField(highlighter.singleLineCommentStart());
        popupRoot.add(singleLineField, 1, 0);

        Label multiLineStartLabel = new Label("Multi Line (wrapped comment) Start");
        popupRoot.add(multiLineStartLabel, 0, 1);
        TextField multiLineStartField = new TextField(highlighter.multiLineCommentStart());
        popupRoot.add(multiLineStartField, 1, 1);
        
        Label multiLineEndLabel = new Label("Multi Line (wrapped comment) Start");
        popupRoot.add(multiLineEndLabel, 0, 2);
        TextField multiLineEndField = new TextField(highlighter.multiLineCommentEnd());
        popupRoot.add(multiLineEndField, 1, 2);

        Button submitBtn = new Button("Save Changes");
        submitBtn.setOnAction(e ->
        {
            highlighter.updateCommentPatterns(singleLineField.getText(), multiLineStartField.getText(), multiLineEndField.getText());
        });
        popupRoot.add(submitBtn, 1, 3);

        // create stage
        Scene scene = new Scene(popupRoot, 400, 400);
        scene.getStylesheets().add(getClass().getResource("popup.css").toExternalForm());
        popupStage.setTitle("Comment Patterns");
        popupStage.setScene(scene);
        popupStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
