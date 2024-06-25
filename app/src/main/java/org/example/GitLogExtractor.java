package org.example;

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

public class GitLogExtractor extends JFrame {

    private JTextField repoPathField1;
    private JTextField repoPathField2;
    private JTextArea logTextArea;

    public GitLogExtractor() {
        super("Git Log Email Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        JPanel repoPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel repoPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel repoLabel1 = new JLabel("Git Repository Path 1:");
        repoPathField1 = new JTextField(30);
        repoPanel1.add(repoLabel1);
        repoPanel1.add(repoPathField1);

        JLabel repoLabel2 = new JLabel("Git Repository Path 2:");
        repoPathField2 = new JTextField(30);
        repoPanel2.add(repoLabel2);
        repoPanel2.add(repoPathField2);

        JButton extractLogButton = new JButton("Extract Git Logs");
        extractLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String repoPath1 = repoPathField1.getText().trim();
                String repoPath2 = repoPathField2.getText().trim();

                StringBuilder gitLog = new StringBuilder();
                if (!repoPath1.isEmpty()) {
                    gitLog.append("Repository 1:\n");
                    String log1 = getGitLog(new File(repoPath1));
                    gitLog.append(log1 != null ? log1 : "Failed to extract Git log for repository 1.\n");
                }
                if (!repoPath2.isEmpty()) {
                    gitLog.append("\nRepository 2:\n");
                    String log2 = getGitLog(new File(repoPath2));
                    gitLog.append(log2 != null ? log2 : "Failed to extract Git log for repository 2.\n");
                }

                logTextArea.setText(gitLog.toString());
            }
        });
        buttonPanel.add(extractLogButton);

        topPanel.add(repoPanel1);
        topPanel.add(repoPanel2);
        topPanel.add(buttonPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        logTextArea = new JTextArea(20, 50);
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);

        JButton saveFileButton = new JButton("Save Log to File");
        saveFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String repoPath1 = repoPathField1.getText().trim();
                String repoPath2 = repoPathField2.getText().trim();
                String gitLog = logTextArea.getText();
                if ((!repoPath1.isEmpty() || !repoPath2.isEmpty()) && !gitLog.isEmpty()) {
                    saveLogToFile(gitLog);
                } else {
                    JOptionPane.showMessageDialog(GitLogExtractor.this, "Please enter valid repository paths and extract the Git logs first.");
                }
            }
        });
        mainPanel.add(saveFileButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private @Nullable String getGitLog(File repoDir) {
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

    private void saveLogToFile(String gitLog) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Git Log");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".eml")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".eml");
            }

            createAndSaveEmail("sender@example.com", "recipient@example.com", "Git Log Report", gitLog, fileToSave.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Log saved successfully to " + fileToSave.getAbsolutePath());
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
