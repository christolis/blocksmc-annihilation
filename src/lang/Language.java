package me.blocksmc.annihilation.lang;

import me.blocksmc.annihilation.Main;
import org.bukkit.ChatColor;

import java.io.*;

public class Language {

    private String locale_ID;
    public LanguageType languageType;

    public Language(String locale) {
        this.locale_ID = locale;
    }

    public String getLocale() {
        return locale_ID;
    }

    public String string(String key) {
        String line = null;
//        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ").replaceAll("\\\\", "/") + "/me/blocksmc/annihilation/lang/" + getLocale() + ".txt";
        String path = "C:/" + getLocale() + ".txt";
        char equalizer = '=';
        int currentLine = 0;
        FileReader fileReader;
        BufferedReader bufferedReader;

        try {
            fileReader = new FileReader(path); //  Our file reader.
            bufferedReader = new BufferedReader(fileReader); //  Wrapper for our file reader.

            while ((line = bufferedReader.readLine()) != null) {
                //  Analyze each line, find out about the key and value and run the logical comparisons.
                String k = ""; String v = "";

                //  If this line's length is 0 then don't even bother searching, you'll get errors. :)
                if(line.length() == 0) {
                    System.out.println("[LANGUAGE INFO]: Skipping line no. " + currentLine + "!");
                    continue; //  Ignore everything that has to do with that line and move on to the next one.
                }

                //  If the first character of the line is a comment symbol.
                if((line.charAt(1) == '#' || line.charAt(1) == ';')) {
                    continue; //  Ignore everything that has to do with that line and move on to the next one.
                }

                //  Start iterating through each character in the line and find out if the equalizer exists.
                boolean foundEqualizer = false;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == equalizer) { //  If it's the equalizer.
                        foundEqualizer = true; //  We found the equalizer.
                        break; //  Break this loop, no need to search for the equalizer anymore.
                    }
                    else { //  If it's not the equalizer.
                        if(line.charAt(i) != ' ') k = k.concat(Character.toString(line.charAt(i))); //  If the specific character is not a space then concat it to the key string.
                    }
                }

                if(!foundEqualizer) System.out.println("[LANGUAGE INFO]: Couldn't find equalizer in line no. " + currentLine + "! (" + line + ")");
                else { //  We found the equalizer so the line is valid!
                    //  Continue the loop and let's try to get the value now!
                    for (int i = (k.length() + 2); i <= line.length(); i++) {
                        v = v.concat(Character.toString(line.charAt(i - 1)));
                    }

                    //  Check if the key is what we wanted.
                    if(k.equals(key)) return ChatColor.translateAlternateColorCodes('&', v); //  If the key we inputted is equal with the key we found in the line, then return its value and end the algorithm!
                }

                currentLine++;
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
//            System.out.println("[LANGUAGE ERROR]: Couldn't read from file \"" + path + "\"! Aborting algorithm...");
            e.printStackTrace();
            return "en_US";
        } catch (IOException e) {
//            System.out.println("[LANGUAGE ERROR]: Error reading file \"" + path + "\" (Reason is IOException)! Aborting algorithm...");
            e.printStackTrace();
            return "null";
        }
        return null;
    }
}
