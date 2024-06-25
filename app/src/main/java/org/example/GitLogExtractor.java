package org.example;

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

public class GitLogExtractor extends JFrame {

    private JTextField repoPathField;
    private JTextArea logTextArea;

    public GitLogExtractor() {
        super("Git Log Email Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel repoLabel = new JLabel("Git Repository Path:");
        repoPathField = new JTextField(30);
        topPanel.add(repoLabel);
        topPanel.add(repoPathField);

        JButton extractLogButton = new JButton("Extract Git Log");
        extractLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String repoPath = repoPathField.getText().trim();
                if (!repoPath.isEmpty()) {
                    String gitLog = getGitLog(new File(repoPath));
                    logTextArea.setText(gitLog != null ? gitLog : "Failed to extract Git log.");
                } else {
                    JOptionPane.showMessageDialog(GitLogExtractor.this, "Please enter a valid repository path.");
                }
            }
        });
        topPanel.add(extractLogButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        logTextArea = new JTextArea(20, 50);
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);

        JButton createEmailButton = new JButton("Create Email");
        createEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String repoPath = repoPathField.getText().trim();
                if (!repoPath.isEmpty()) {
                    String gitLog = logTextArea.getText();
                    if (!gitLog.isEmpty()) {
                        createAndSaveEmail("sender@example.com", "recipient@example.com", "Git Log Report", gitLog, "git_log_email.eml");
                        JOptionPane.showMessageDialog(GitLogExtractor.this, "Email created and saved successfully.");
                    } else {
                        JOptionPane.showMessageDialog(GitLogExtractor.this, "Please extract Git log first.");
                    }
                } else {
                    JOptionPane.showMessageDialog(GitLogExtractor.this, "Please enter a valid repository path.");
                }
            }
        });
        mainPanel.add(createEmailButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String getGitLog(File repoDir) {
        List<String> gitCommand = List.of("git", "log", "--pretty=format:%s%nAuthor: %an%nDate: %ad%nCommit: %H%n");

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

    private void createAndSaveEmail(String from, String to, String subject, String bodyText, String filePath) {
        var props = new Properties();
        props.put("mail.smtp.host", "localhost"); // Dummy SMTP host for creating the email file

        Session session = Session.getDefaultInstance(props, null);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(bodyText);

            // Save the message to a .eml file
            File emlFile = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(emlFile)) {
                message.writeTo(fos);
            }

            System.out.println("Email message saved to " + emlFile.getAbsolutePath());

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GitLogExtractor().setVisible(true);
            }
        });
    }
}