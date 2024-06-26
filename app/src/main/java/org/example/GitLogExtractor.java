package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Properties;

import static javax.swing.JOptionPane.showMessageDialog;

public class GitLogExtractor extends JFrame {

    private static final Logger log = LogManager.getLogger(GitLogExtractor.class);
    private final JTextField repoPathField1;
    private final JTextField repoPathField2;
    private JTextArea logTextArea;
    private final Properties properties;
    private static final String PROPERTIES_FILE = "src/main/resources/config.properties";

    public GitLogExtractor() {
        super("Git Log Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        properties = new Properties();
        loadProperties();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        JPanel repoPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel repoPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel repoLabel1 = new JLabel("Dictionary repository:      ");
        repoPathField1 = new JTextField(30);
        JButton browseButton1 = new JButton("Browse");
        browseButton1.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(GitLogExtractor.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                repoPathField1.setText(selectedFile.getAbsolutePath());
            }
        });
        repoPanel1.add(repoLabel1);
        repoPanel1.add(repoPathField1);
        repoPanel1.add(browseButton1);

        JLabel repoLabel2 = new JLabel("Yellow Pages repository:");
        repoPathField2 = new JTextField(30);
        JButton browseButton2 = new JButton("Browse");
        browseButton2.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(GitLogExtractor.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                repoPathField2.setText(selectedFile.getAbsolutePath());
            }
        });
        repoPanel2.add(repoLabel2);
        repoPanel2.add(repoPathField2);
        repoPanel2.add(browseButton2);

        JButton extractLogButton = new JButton("Extract git logs");
        extractLogButton.addActionListener(e -> {
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
        });
        topButtonPanel.add(extractLogButton);

        JButton clearLogsButton = new JButton("Clear logs");
        clearLogsButton.addActionListener(e -> logTextArea.setText(""));
        topButtonPanel.add(clearLogsButton);

        topPanel.add(repoPanel1);
        topPanel.add(repoPanel2);
        topPanel.add(topButtonPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        logTextArea = new JTextArea(20, 50);
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);

        JButton saveFileButton = new JButton("Save log to email file");
        saveFileButton.addActionListener(e -> {
            String repoPath1 = repoPathField1.getText().trim();
            String repoPath2 = repoPathField2.getText().trim();
            String gitLog = logTextArea.getText();
            if (!gitLog.isEmpty()) {
                saveLogToFile(gitLog);
            } else {
                showMessageDialog(GitLogExtractor.this, "The Git logs must be extracted first.");
            }
        });
        bottomButtonPanel.add(saveFileButton);

        JButton saveGitLogButton = new JButton("Save log to text file");
        saveGitLogButton.addActionListener(e -> {
            String gitLog = logTextArea.getText();
            if (!gitLog.isEmpty()) {
                saveGitLogToTextFile(gitLog);
            } else {
                showMessageDialog(GitLogExtractor.this, "The Git logs must be extracted first.");
            }
        });
        bottomButtonPanel.add(saveGitLogButton);

        mainPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadProperties() {
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(input);
            String repoPath1 = properties.getProperty("repoPathField1", "");
            String repoPath2 = properties.getProperty("repoPathField2", "");

            // Update GUI components
            SwingUtilities.invokeLater(() -> {
                repoPathField1.setText(repoPath1);
                repoPathField2.setText(repoPath2);
            });

        } catch (FileNotFoundException ex) {
            log.error("Configuration properties not found!");
        } catch (IOException ex) {
            log.error("Unable to read configuration properties file!");
        }
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
            log.error("Unable to fetch Git log!");
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
            showMessageDialog(this, "Log saved successfully to " + fileToSave.getAbsolutePath());
        }
    }

    private void saveGitLogToTextFile(String gitLog) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Git Log");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getAbsolutePath().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                writer.println(gitLog);
                showMessageDialog(this, "Git log saved successfully to " + fileToSave.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                log.error("Unable to write to text file!");
                showMessageDialog(this, "Error saving Git log to file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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

            // Mark the message as a draft
            message.addHeader("X-Unsent", "1");

            // Save the message to a .eml file
            File emlFile = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(emlFile)) {
                message.writeTo(fos);
            }

            System.out.println("Email draft saved to " + emlFile.getAbsolutePath());

        } catch (MessagingException | IOException e) {
            log.error("Unable to create draft email file!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GitLogExtractor().setVisible(true));
    }
}
