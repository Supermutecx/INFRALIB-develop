package ro.infrasoft.infralib.email;

import ro.infrasoft.infralib.email.account.AbstractEmailAccount;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Client mail.
 */
public class EmailClient {

    /**
     * Trimite efectiv un mail.
     *
     * @param account             contul de mail de pe care va trimite
     * @param toAddresses         adrese la care va trimite
     * @param ccAddresses         cc addresses
     * @param subject             subiectul mail-ului
     * @param message             mesajul mail-ului
     * @param attachmentFiles optional - atasamente
     */
    public void email(AbstractEmailAccount account, List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String message, List<File> attachmentFiles, List<String> attachmentFileNames, Long timeout) throws Exception {
        Properties props = new Properties();
        props.put("mail.debug", true);
        props.put("mail.smtp.host", account.getHost());
        props.put("mail.smtp.port", account.getPort());
        props.put("mail.smtp.localhost", account.getHost()); //hello name/bug
        props.put("mail.smtp.auth", account.isAuth());
        props.put("mail.smtp.connectiontimeout", timeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", timeout);

        if (account.isDebug()){
            props.put("mail.debug", "true");
            props.put("mail.debug.auth", "true");
        }

        if (account.isSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", account.isStarttls());
            props.put("mail.smtp.starttls.required", account.isStarttlsRequired());
            props.put("mail.smtp.ssl.trust", "*");
        }

        if (account.isNtlm()) {
            props.put("mail.smtp.auth.mechanisms", "NTLM");
            props.put("mail.smtp.auth.ntlm.domain", account.getNtlmDomain());
        }

        Authenticator authenticator = null;
        final String[] username = new String[1];
        username[0] = "";
        final String[] password = new String[1];
        password[0] = "";

        if (account.isAuth()){
            username[0] = account.getUsername();
            password[0] = account.getPassword();

            authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(username[0], password[0]);
                }
            };
        }

        Session mailSession = null;
        if (account.isAuth())
            mailSession = Session.getDefaultInstance(props, authenticator);
        else
            mailSession = Session.getDefaultInstance(props);

        if (account.isDebug()){
            mailSession.setDebug(true);
        }

        InternetAddress fromAddr = null;

        if (account.getFromName() != null){
            fromAddr = new InternetAddress(account.getFrom(), account.getFromName());
        } else {
            fromAddr = new InternetAddress(account.getFrom());
        }

        Message msg = new MimeMessage(mailSession);

        List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        for (String toAddress : toAddresses) {
            InternetAddress toAddr = new InternetAddress(toAddress);
            addresses.add(toAddr);
        }
        msg.setRecipients(Message.RecipientType.TO, addresses.toArray(new InternetAddress[addresses.size()]));

        addresses = new ArrayList<InternetAddress>();
        for (String ccAddress : ccAddresses) {
            InternetAddress ccAddr = new InternetAddress(ccAddress);
            addresses.add(ccAddr);
        }
        if (!addresses.isEmpty())
            msg.setRecipients(Message.RecipientType.CC, addresses.toArray(new InternetAddress[addresses.size()]));

        addresses = new ArrayList<InternetAddress>();
        for (String bccAddress : bccAddresses) {
            InternetAddress bccAddr = new InternetAddress(bccAddress);
            addresses.add(bccAddr);
        }
        if (!addresses.isEmpty())
            msg.setRecipients(Message.RecipientType.BCC, addresses.toArray(new InternetAddress[addresses.size()]));


        msg.setFrom(fromAddr);
        msg.setSubject(subject);
        String encodingOptions = "text/html; charset=UTF-8";
        msg.setHeader("Content-Type", encodingOptions);
        msg.setSentDate(new Date());

        //mod simplu sau cu atasamente
        if (attachmentFiles.size() == 0) {
            msg.setContent(message, "text/html; charset=utf-8");
        } else {
            //multipart
            Multipart multipart = new MimeMultipart();

            // partea cu text
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            // partea cu atasamente
            int index = 0;
            for (File attachFile : attachmentFiles) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachFile);
                messageBodyPart.setDataHandler(new DataHandler(source));

                if (attachmentFileNames.isEmpty())
                    messageBodyPart.setFileName(attachFile.getName());
                else
                    messageBodyPart.setFileName(attachmentFileNames.get(index));

                multipart.addBodyPart(messageBodyPart);

                index++;
            }

            // le combinam
            msg.setContent(multipart);
        }

        // trimitem
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Address addr: msg.getAllRecipients()){
            sb.append(sep).append(addr.toString());
            sep = ",";
        }
        System.out.println("start sending mail to: " + sb.toString());
        Transport.send(msg);
        System.out.println("done sending mail: " + sb.toString());
    }

    public void email(AbstractEmailAccount account, List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String message, List<File> attachmentFiles, List<String> attachmentFileNames) throws Exception {
        email(account, toAddresses, ccAddresses, bccAddresses, subject, message, attachmentFiles, attachmentFileNames, 10000l);
    }
}
