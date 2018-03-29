package application;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application
{
    private VBox root;
    private Stage stage;
    private CodeArea area;

    private static final String[] KEYWORDS = new String[] { "abstract", "assert", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
            "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
            "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while" };

    /* RichTextFX configuration */
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN
                    + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");

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

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change ->
        {
            area.setStyleSpans(0, computeHighlighting(area.getText()));
        });
        area.replaceText(0, 0, FileHelper.readFile(file));

        // create stage
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.setTitle(FileHelper.fileName(file));
        stage.setScene(scene);
        stage.show();
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text)
    {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find())
        {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("PAREN") != null ? "paren"
                            : matcher.group("BRACE") != null ? "brace"
                                    : matcher.group("BRACKET") != null ? "bracket"
                                            : matcher.group("SEMICOLON") != null ? "semicolon"
                                                    : matcher.group("STRING") != null ? "string"
                                                            : matcher.group("COMMENT") != null ? "comment" : null;
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
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
