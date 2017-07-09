package com.ldz.fpt.businesscardscannerandroid.utils;

import android.content.Context;

import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.utils.xml_parser.LineModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linhdq on 7/9/17.
 */

public class StringUtil {
    private static final String phoneErr = "iIlLoOzZAsSbg";
    private static final String phoneVal = "1111002245569";

    private static List<String> listNames;

    private static StringUtil inst;

    public StringUtil(Context context) {
        listNames = Arrays.asList(context.getResources().getStringArray(R.array.ho_vietnam));
    }

    public static StringUtil getInst(Context context) {
        if (inst == null) {
            inst = new StringUtil(context);
        }
        return inst;
    }

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
    }


    /**
     * Phone number
     */

    private boolean isPhoneChar(char c) {
        if (Character.isDigit(c) || (c == '-') || (c == ' ') || (c == '(') || (c == ')') || (c == '.')
                || phoneErr.indexOf(c) >= 0) {
            return true;
        }
        return false;
    }

    public String trimString(String input) {
        if (input == null) {
            return input;
        }
        input = input.replaceAll("\\n", "/");
        StringBuilder builder = new StringBuilder();
        char[] list = input.toCharArray();
        for (char c : list) {
            if (c < 122 && c != ' ') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String tryConvertToPhone(String input, boolean hasPlus) {
        int count = 0;
        char[] listChars = input.toCharArray();
        input = "";
        for (int i = 0; i < listChars.length; i++) {
            if (Character.isDigit(listChars[i])) {
                count++;
                input += listChars[i];
            }
        }
        if (count > 7) {
            if (hasPlus) {
                input = "+" + input;
            }
            return input;
        }
        return null;
    }

    public List<String> extractPhone(String input) {
        if (input == null) {
            return null;
        }
        input = trimString(input);
        List<String> listPhones = new ArrayList<>();
        String current = "";
        String temp;
        char[] listChars = input.trim().toCharArray();
        boolean hasPlus = false;
        for (int i = 0; i < listChars.length; i++) {
            if (isPhoneChar(listChars[i])) {
                current += listChars[i];
            } else {
                temp = tryConvertToPhone(current, hasPlus);
                if (temp != null && !temp.trim().isEmpty()) {
                    listPhones.add(temp);
                }
                current = "";
                if (listChars[i] == '+') {
                    hasPlus = true;
                }
            }
        }
        temp = tryConvertToPhone(current, hasPlus);
        if (temp != null && !temp.trim().isEmpty()) {
            listPhones.add(temp);
        }
        return listPhones;
    }

    /**
     * Email
     */

    private String correctString(String input) {
        char[] listChars = input.trim().toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < listChars.length; i++) {
            if (listChars[i] == 169 || listChars[i] == 174) {
                listChars[i] = '@';
            }
            if (i + 1 < listChars.length && (listChars[i + 1] == 169 || listChars[i + 1] == 174)) {
                listChars[i + 1] = '@';
            }
            if (listChars[i] == ' ' && (listChars[i + 1] == '@' || (i - 1 > 0 && listChars[i - 1] == '@'))) {
                continue;
            }

            builder.append(listChars[i]);
        }
        return builder.toString();
    }

    public List<String> extractEmails(String input) {
        if (input == null) {
            return null;
        }
        input = correctString(input);
        List<String> listEmails = new ArrayList<>();
        final String RE_MAIL = "([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Za-z]{2,4})";
        Pattern p = Pattern.compile(RE_MAIL);
        Matcher m = p.matcher(input);
        while (m.find()) {
            if (!listEmails.contains(m.group(1))) {
                listEmails.add(m.group(1));
            }
        }
        if (listEmails.size() == 0) {
            String[] listWords = input.split(" ");
            String temp;
            for (int i = 0; i < listWords.length; i++) {
                temp = listWords[i];
                if (temp.contains("@")) {
                    if (!temp.contains("com")) {
                        if (i + 1 > listWords.length) {
                            if (listWords[i + 1].equalsIgnoreCase("com")) {
                                temp += "com";
                            }
                        }
                    }
                    listEmails.add(temp);
                }
            }
        }
        return listEmails;
    }

    /**
     * Extract Ho&Ten
     */

    public String getNameFromRawOutput(List<LineModel> list) {
        String text;
        String[] listWords;
        List<String> listExpect = new ArrayList<>();
        List<String> listAvailable = new ArrayList<>();
        char[] listChars;
        int count1, count2;
        for (LineModel model : list) {
            text = model.getText().trim();
            if (text.contains(":") || text.contains("_")) {
                continue;
            }
            listChars = text.toCharArray();
            listWords = text.split(" ");
            count1 = count2 = 0;
            for (char c : listChars) {
                if (Character.isLetter(c)) {
                    count1++;
                    if (Character.isUpperCase(c)) {
                        count2++;
                    }
                }
            }
            if (count1 == count2) {
                if (listWords.length <= 7) {
                    for (String word : listWords) {
                        if (listNames.contains(word.toUpperCase())) {
                            listExpect.add(text);
                            break;
                        }
                    }
                    continue;
                }
            }
            if (listWords.length <= 7) {
                for (String word : listWords) {
                    if (listNames.contains(word.toUpperCase())) {
                        listAvailable.add(text);
                    }
                }
            }
        }
        if (listExpect.size() != 0) {
            for (String s : listExpect) {
                if (s.toUpperCase().contains("TS")) {
                    return s;
                }
            }
            return listExpect.get(0);
        } else if (listAvailable.size() != 0) {
            for (String s : listAvailable) {
                if (s.toUpperCase().contains("TS")) {
                    return s;
                }
            }
            return listAvailable.get(0);
        }
        if (listAvailable.size() == 0 && listExpect.size() == 0) {
            for (LineModel model : list) {
                listWords = model.getText().trim().split(" ");
                if (listWords.length <= 7 || listWords[0].toCharArray().length >= 5) {
                    return model.getText();
                }
            }
        }
        return null;
    }
}
