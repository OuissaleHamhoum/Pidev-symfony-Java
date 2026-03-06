package edu.Loopi.tools;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;

public class EmailService {

    // Replace with your actual API key from the SendGrid website
    private final String apiKey = "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";

    public void sendEmail(String toEmail, String subject, String body) {
        Email from = new Email("werfellimehdi00@gmail.com"); // Must be verified in SendGrid
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("✅ Status Code: " + response.getStatusCode());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}