package org.example;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class GitLogExtractor {

    public static void main(String @NotNull [] args) {

        // Get the repository path from the command line arguments
        String repositoryPath = "C:\\Users\\Douglas\\IdeaProjects\\GitLogMail";
        File repoDir = new File(repositoryPath);

        // Check if the specified directory exists and is a directory
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            System.out.println("The specified path is not a valid directory: " + repositoryPath);
            return;
        }

        // Extract the git log
        String gitLog = getGitLog(repoDir);
        if (gitLog == null) {
            System.out.println("Failed to extract git log.");
            return;
        }

        // Email details
        String to = "recipient@example.com";
        String from = "sender@example.com";
        String subject = "Git Log Report";
        String bodyText = "This is the git log of the repository:\n\n" + gitLog;

        // Create and save the email message
        createAndSaveEmail(from, to, subject, bodyText, "git_log_email.eml");
    }

    private static @Nullable String getGitLog(File repoDir) {
        List<String> gitCommand = List.of("git", "log");

        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand);
        processBuilder.directory(repoDir);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder gitLog = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                gitLog.append(line).append("\n");
            }

            reader.close();
            process.waitFor();
            return gitLog.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createAndSaveEmail(String from, String to, String subject, String bodyText, String filePath) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost"); // Dummy SMTP host for creating the email file

        Session session = Session.getDefaultInstance(props, null);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(bodyText);

            // Save the message to a .eml file
            File emlFile = new File("C:\\Users\\Douglas\\IdeaProjects\\GitLogMail\\logs\\email_message.eml");
            try (FileOutputStream fos = new FileOutputStream(emlFile)) {
                message.writeTo(fos);
            }

            System.out.println("Email message saved to " + filePath);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}