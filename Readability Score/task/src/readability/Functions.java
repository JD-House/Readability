package readability;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class Functions {
    private static final int[] AGES = {0, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24};


    public static void analyzer(String path) {

        File file = new File(path);
        String text = "";

        try {
            text = readUsingFiles(path);
        } catch (IOException e) {
            System.out.println("File not found" + path);
        }

        List<String> sentences= new ArrayList<>(List.of(text.split("[!.?]")));
        double wordsNum = 0;
        for (String sentence : sentences) {
            wordsNum += wordCounter(sentence);
        }
        double lettersNum = letterCounter(text);
        double sentencesNum = sentences.size();
        double syllablesNum = syllablesCounter(text);
        double polySyllablesNum = polySyllablesCounter(text);


        double scoreARI = scoreCounterARI(wordsNum, lettersNum, sentencesNum);
        double gradeFK = gradeCounterFK(wordsNum, syllablesNum, sentencesNum);
        double gradeSMOG = gradeCounterSMOG(polySyllablesNum, sentencesNum);
        double gradeCL = gradeCounterCL(wordsNum, sentencesNum, lettersNum);

        double ARI = ageDetermination(scoreARI);
        double FK = ageDetermination(gradeFK);
        double SMOG = ageDetermination(gradeSMOG);
        double CL = ageDetermination(gradeCL);

        printResult(wordsNum, lettersNum, sentencesNum, syllablesNum, polySyllablesNum);

        Scanner scanner = new Scanner(System.in);

        switch (scanner.nextLine()) {
            case "ARI" : System.out.printf("Automated Readability Index: %.2f (about %.0f-year-olds).\n", scoreARI, ARI);
            case "FK" : System.out.printf("Flesch–Kincaid readability tests: %.2f (about %.0f-year-olds).\n", gradeFK, FK);
            case "SMOG" : System.out.printf("Simple Measure of Gobbledygook: %.2f (about %.0f-year-olds).\n", gradeSMOG, SMOG);
            case "CL" : System.out.printf("Coleman–Liau index: %.2f (about %.0f-year-olds).\n", gradeCL, CL);
            case "all" : {
                System.out.printf("Automated Readability Index: %.2f (about %.0f-year-olds).\n", scoreARI, ARI);
                System.out.printf("Flesch–Kincaid readability tests: %.2f (about %.0f-year-olds).\n", gradeFK, FK);
                System.out.printf("Simple Measure of Gobbledygook: %.2f (about %.0f-year-olds).\n", gradeSMOG, SMOG);
                System.out.printf("Coleman–Liau index: %.2f (about %.0f-year-olds).\n", gradeCL, CL);
                System.out.printf("\nThis text should be understood in average by %.2f-year-olds.\n", (ARI + FK + SMOG +CL) / 4.0 );
            }
        }
    }


    private static int wordCounter(String sentence) {
        return sentence.trim().split("\\s+").length;
    }

    private static int letterCounter(String text) {
        return text.trim().replaceAll("\\s+", "").length();
    }

    private static int syllablesCounter(String text) {
        String[] editedText = text.split("[\\s.,!?\\t\\n\\v\\f\\r]");
        List<String> words = new ArrayList<>(Arrays.asList(editedText));
        words.removeIf(member -> Objects.equals(member, ""));
        int syllablesCounter = 0;
        for (String word : words) {
            int counter = 0;
            for (int i = 0; i < word.length(); ++i) {
                if (Character.toString(word.charAt(i)).matches("[aeiouyAEIOUY]")) {
                    ++counter;
                    if (i != 0 && Character.toString(word.charAt(i - 1)).matches("[aeiouyAEIOUY]")) {
                        --counter;
                    }
                }
            }
            if (word.length() > 0 && Character.toString(word.charAt(word.length() - 1)).matches("[eE]")) {
                --counter;
            }
            syllablesCounter += counter <= 0 ? 1 : counter;
        }
        return syllablesCounter == 0 ? 1 : syllablesCounter;
    }

    private static int polySyllablesCounter(String text) {
        String[] editedText = text.split("[\\s.,!?\\t\\n\\v\\f\\r]");
        List<String> words = new ArrayList<>(Arrays.asList(editedText));
        words.removeIf(member -> Objects.equals(member, ""));
        int polySyllablesCounter = 0;
        int counter;

        for (String word : words) {
            counter = 0;
            for (int i = 0; i < word.length(); ++i) {
                if (Character.toString(word.charAt(i)).matches("[aeiouyAEIOUY]")) {
                    ++counter;
                    if (i != 0 && Character.toString(word.charAt(i - 1)).matches("[aeiouyAEIOUY]")) {
                        --counter;
                    }
                }
            }
            if (word.length() > 0 && Character.toString(word.charAt(word.length() - 1)).matches("[eE]")) {
                --counter;
            }
            polySyllablesCounter += counter > 2 ? 1 : 0;
        }
        return polySyllablesCounter;
    }



    private static String readUsingFiles(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private static double scoreCounterARI(double wordsNum, double lettersNum, double sentencesNum) {
        return 4.71 * (lettersNum / wordsNum) + 0.5 * (wordsNum / sentencesNum) - 21.43;
    }

    private static double gradeCounterFK(double wordsNum, double syllables, double sentencesNum) {
        return 0.39 * (wordsNum / sentencesNum) + 11.8 * (syllables / wordsNum) - 15.59;
    }

    private static double gradeCounterSMOG(double polySyllables, double sentencesNum) {
        return 1.043 * Math.sqrt(polySyllables * (30.0 / sentencesNum)) + 3.1291;
    }

    private static double gradeCounterCL(double wordsNum, double sentencesNum, double lettersNum) {
        final double L = lettersNum / (wordsNum / 100.0);
        final double S = sentencesNum / (wordsNum / 100.0);
        return 0.0588 * L - 0.296 * S - 15.8;
    }

    private static double ageDetermination(double score) {
        score = Math.ceil(score);
        for (int i = 0; i < AGES.length; ++i) {
            if (score == i) {
                return AGES[i];
            }
        }
        return 24;
    }

    private static void printResult(double wordsNum, double lettersNum, double sentencesNum,
                                    double syllablesNum, double polySyllablesNum) {
        System.out.printf("Words: %.0f\n", wordsNum);
        System.out.printf("Sentences: %.0f\n", sentencesNum);
        System.out.printf("Characters: %.0f\n", lettersNum);
        System.out.printf("Syllables: %.0f\n", syllablesNum);
        System.out.printf("Polysyllables: %.0f\n", polySyllablesNum);
        System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all):");
    }

}
