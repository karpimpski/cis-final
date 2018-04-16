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
    private Pattern pattern = Pattern.compile("(?<KEYWORD>" + keywordPattern + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>"
            + SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + commentPattern() + ")");

    private String extension;

    public SyntaxHighlighter(File file)
    {
        try
        {
            String fileName = file.getName();
            if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
                extension = fileName.substring(fileName.lastIndexOf(".")+1);
            else
                extension = "plain_text";
        } catch (NullPointerException ex)
        {
            extension = "plain_text";
        }

    }

    public String[] keywords()
    {
        return FileHelper.readFile(new File("rules/" + extension + "/keywords.txt")).split(",");
    }
    
    public String getExtension()
    {
        return extension;
    }
    
    public String singleLineCommentStart()
    {
        return FileHelper.readFile(new File("rules/" + extension + "/comments/singleline.txt"));
    }
    
    public String multiLineCommentStart()
    {
        return FileHelper.readFile(new File("rules/" + extension + "/comments/multiline_start.txt"));
    }
    
    public String multiLineCommentEnd()
    {
        return FileHelper.readFile(new File("rules/" + extension + "/comments/multiline_end.txt"));
    }
    
    public void updateCommentPatterns(String singleLineStart, String multiLineStart, String multiLineEnd)
    {
        FileHelper.saveFile(new File("rules/" + extension + "/comments/singleline.txt"), singleLineStart);
        FileHelper.saveFile(new File("rules/" + extension + "/comments/multiline_start.txt"), multiLineStart);
        FileHelper.saveFile(new File("rules/" + extension + "/comments/multiline_end.txt"), multiLineEnd);
    }

    public String singleLineCommentPattern()
    {
        return FileHelper.readFile(new File("rules/" + extension + "/comments/singleline.txt")) + "[^\\n]*";
    }

    public String multiLineCommentPattern()
    {
        String start = FileHelper.readFile(new File("rules/" + extension + "/comments/multiline_start.txt"));
        String end = FileHelper.readFile(new File("rules/" + extension + "/comments/multiline_end.txt"));
        try
        {
            return start + "((.|\n)*)" + end;
        }
        catch(Exception ex)
        {
            return "";
        }
    }

    public String commentPattern()
    {
        return singleLineCommentPattern() + "|" + multiLineCommentPattern();
    }

    public void addKeyword(String keyword)
    {
        ArrayList<String> keywordsList = new ArrayList<String>(Arrays.asList(keywords()));
        if (!keywordsList.contains(keyword))
        {
            File file = new File("rules/" + extension + "/keywords.txt");
            String content = FileHelper.readFile(file);
            if(!content.equals(""))
                content += ",";
            content += keyword;
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
