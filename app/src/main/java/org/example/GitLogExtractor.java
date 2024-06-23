package org.example;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Properties;

import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

public class GitLogExtractor extends JFrame {

    private final JTextField repoPathField;
    private final JTextArea logTextArea;

    public static void main(String[] args) {
        invokeLater(() -> new GitLogExtractor().setVisible(true));
    }

    public GitLogExtractor() {
        super("Git Log Email Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        val mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        val topPanel  = new JPanel(new FlowLayout(FlowLayout.LEFT));
        val repoLabel = new JLabel("Git Repository Path:");
        repoPathField = new JTextField(30);
        topPanel.add(repoLabel);
        topPanel.add(repoPathField);

        val extractLogButton = getLogButton();
        topPanel.add(extractLogButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        logTextArea = new JTextArea(20, 50);
        logTextArea.setEditable(false);
        val logScrollPane = new JScrollPane(logTextArea);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);

        val createEmailButton = getEmailButton();
        mainPanel.add(createEmailButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private @NotNull JButton getEmailButton() {
        val createEmailButton = new JButton("Create Email");
        createEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                val repoPath = repoPathField.getText().trim();
                if (!repoPath.isEmpty()) {
                    val gitLog = logTextArea.getText();
                    if (!gitLog.isEmpty()) {
                        createAndSaveEmail("sender@example.com", "recipient@example.com", "Git Log Report", gitLog, "git_log_email.eml");
                        showMessageDialog(GitLogExtractor.this, "Email created and saved successfully.");
                    } else {
                        showMessageDialog(GitLogExtractor.this, "Please extract Git log first.");
                    }
                } else {
                    showMessageDialog(GitLogExtractor.this, "Please enter a valid repository path.");
                }
            }
        });
        return createEmailButton;
    }

    private @NotNull JButton getLogButton() {
        val extractLogButton = new JButton("Extract Git Log");
        extractLogButton.addActionListener(e -> {
            val repoPath = repoPathField.getText().trim();
            if (!repoPath.isEmpty()) {
                String gitLog = getGitLog(new File(repoPath));
                logTextArea.setText(gitLog != null ? gitLog : "Failed to extract Git log.");
            } else {
                showMessageDialog(GitLogExtractor.this, "Please enter a valid repository path.");
            }
        });
        return extractLogButton;
    }

    private @Nullable String getGitLog(File repoDir) {
        val gitCommand = List.of("git", "log", "--pretty=format:%s%nAuthor: %an%nDate: %ad%nCommit: %H%n");

        val processBuilder = new ProcessBuilder(gitCommand);
        processBuilder.directory(repoDir);
        processBuilder.redirectErrorStream(true);

        try {
            val process = processBuilder.start();
            val inputStream = process.getInputStream();
            val reader = new BufferedReader(new InputStreamReader(inputStream));
            val gitLog = new StringBuilder();
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

    private void createAndSaveEmail(String from, String to, String subject, String bodyText, String filePath) {
        val properties = new Properties();
        properties.put("mail.smtp.host", "localhost"); // Dummy SMTP host for creating the email file

        val session = Session.getDefaultInstance(properties, null);

        try {
            val message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(bodyText);

            // Save the message to a .eml file
            val emlFile = new File(filePath);
            try (val fos = new FileOutputStream(emlFile)) {
                message.writeTo(fos);
            }

            System.out.println("Email message saved to " + emlFile.getAbsolutePath());

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}