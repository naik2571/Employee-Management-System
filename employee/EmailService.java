package employee;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;

public class EmailService {
    public static void sendWelcomeEmail(String toEmail, String name) {
        // Run asynchronously
        new Thread(() -> {
            try {
                Properties props = new Properties();
                try (FileInputStream in = new FileInputStream("config.properties")) {
                    props.load(in);
                } catch (Exception e) {
                    System.err.println("Could not load email config.");
                    return;
                }

                String host = props.getProperty("smtp.host", "smtp.gmail.com");
                String port = props.getProperty("smtp.port", "587");
                String username = props.getProperty("smtp.username", "");
                String password = props.getProperty("smtp.password", "");
                
                if (username.isEmpty() || password.isEmpty()) {
                    System.err.println("SMTP credentials not configured.");
                    return;
                }

                Properties smtpProps = new Properties();
                smtpProps.put("mail.smtp.auth", "true");
                smtpProps.put("mail.smtp.starttls.enable", "true");
                smtpProps.put("mail.smtp.host", host);
                smtpProps.put("mail.smtp.port", port);

                Session session = Session.getInstance(smtpProps, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Welcome to the Company, " + name + "!");
                message.setText("Hi " + name + ",\n\nWelcome to the team! Your employee profile has been successfully added to the system.\n\nBest Regards,\nHR Team");

                Transport.send(message);
                System.out.println("Welcome email sent to " + toEmail);
            } catch (Exception e) {
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }
        }).start();
    }
}
