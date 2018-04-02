package application;

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
    private static String[] keywords = getKeywords();
    
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
    public static Pattern pattern = Pattern.compile("(?<KEYWORD>" + keywordPattern + ")" + "|(?<PAREN>" + PAREN_PATTERN
            + ")" + "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>"
            + SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");;

    public static void setup()
    {
        keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        pattern = Pattern.compile(
                "(?<KEYWORD>" + keywordPattern + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN
                        + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");
    }

    public static String[] getKeywords()
    {
        return new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
                "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
                "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
    }

    public static void addKeyword(String keyword)
    {
        ArrayList<String> keywordsList = new ArrayList<String>(Arrays.asList(keywords));
        if (!keywordsList.contains(keyword))
        {
            keywordsList.add(keyword);
            System.out.println("adding " + keyword);
        }
        keywords = keywordsList.toArray(new String[keywordsList.size()]);
        setup();
    }

    public static StyleSpans<Collection<String>> computeHighlighting(String text)
    {
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
