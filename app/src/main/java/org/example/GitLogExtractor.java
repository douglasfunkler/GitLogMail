package org.example;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitLogExtractor {

    public static void main(String[] args) {
        new GitLogExtractor().extractGitLog();
    }

    public void extractGitLog() {
        final var repositoryPath = "C:\\Users\\Douglas\\IdeaProjects\\GitLogMail";
        String outputPath = "C:\\Users\\Douglas\\IdeaProjects\\GitLogMail\\logs\\gitLog.txt";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new java.io.File(repositoryPath));
        processBuilder.command("git", "log");

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            FileWriter fileWriter = new FileWriter(outputPath);

            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.write(line + System.lineSeparator());
            }

            fileWriter.close();
            reader.close();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Git log extracted successfully.");
            } else {
                System.out.println("Error occurred while extracting git log. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}