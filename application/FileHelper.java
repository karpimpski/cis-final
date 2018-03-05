package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileHelper
{
    /*
     * Given a file, return its contents.
     */
    public static String readFile(File file)
    {
        if(file == null) return "";
        
        String result = "";
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            // read each line of the file and add it to the result
            String line;
            while ((line = reader.readLine()) != null)
            {
                result += line + "\n";
            }
            
            reader.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }
    
    /*
     * Given a file and content, save the content to the file.
     */
    public static void saveFile(File file, String content)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(file);
            String[] lines = content.split("\n");
            for(String line : lines)
                fileWriter.write(line + "\n");
            fileWriter.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /*
     * Given a file, return its name. If the file doesn't exist, return Untitled.
     */
    public static String fileName(File file)
    {
        if(file != null)
            return file.getName();
        return "Untitled";
    }
}
