package application;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class SyntaxHighlighter
{

    private final String PAREN_PATTERN = "\\(|\\)";
    private final String BRACE_PATTERN = "\\{|\\}";
    private final String BRACKET_PATTERN = "\\[|\\]";
    private final String SEMICOLON_PATTERN = "\\;";
    private final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    private String keywordPattern = "\\b(" + String.join("|", keywords()) + ")\\b";
    public Pattern pattern = Pattern.compile("(?<KEYWORD>" + keywordPattern + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>"
            + SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + commentPattern() + ")");

    public String[] keywords()
    {
        return FileHelper.readFile(new File("rules/java/keywords.txt")).split(",");
    }

    public String commentPattern()
    {
        return FileHelper.readFile(new File("rules/java/comments/singleline.txt")) + "|" + FileHelper.readFile(new File("rules/java/comments/multiline.txt"));
    }

    public void addKeyword(String keyword)
    {
        ArrayList<String> keywordsList = new ArrayList<String>(Arrays.asList(keywords()));
        if (!keywordsList.contains(keyword))
        {
            File file = new File("rules/java/keywords.txt");
            String content = FileHelper.readFile(file);
            content += "," + keyword;
            FileHelper.saveFile(file, content);
        }
    }

    public StyleSpans<Collection<String>> computeHighlighting(String text)
    {
        keywordPattern = "\\b(" + String.join("|", keywords()) + ")\\b";
        pattern = Pattern.compile(
                "(?<KEYWORD>" + keywordPattern + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN
                        + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + commentPattern() + ")");
        Matcher matcher = pattern.matcher(text);
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
}
