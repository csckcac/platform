package org.wso2.carbon.bam.analyzer.analyzers;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AlertConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.analyzer.util.EmailGateway;
import org.wso2.carbon.bam.core.dataobjects.Record;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

public class AlertTrigger extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(AlertTrigger.class);
    public static final String FAULT_MESSAGE_SEPARATOR = "";

    private AlertConfig alertConfig;
    private StringBuffer buffer;

    public AlertTrigger(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {
        alertConfig = (AlertConfig) getAnalyzerConfig();
        String[] fields = alertConfig.getFields();
        buffer = new StringBuffer();

        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                List<Record> records = (List<Record>) result;
                for (Record record : records) {

                    Map<String, String> columns = record.getColumns();

                    buffer.append(FAULT_MESSAGE_SEPARATOR);
                    buffer.append("\n \n <table border=\"1\">");

                    for (Map.Entry<String, String> entry : columns.entrySet()) {
                        String columnKey = entry.getKey();
                        for (String field : fields) {
                            if (columnKey.equals(field.trim())) {
                                buffer.append("<tr>");
                                buffer.append("<td>").append(columnKey).append("</td>");
                                buffer.append("<td>").append(entry.getValue()).append("</td>");
                                buffer.append("</tr>");
                            }
                        }
                    }
                    buffer.append("</table> \n \n ");
                }
            }
        }
        if (buffer.length() > 0) {
            sendEmailMessage(buffer.toString(), alertConfig);
        }
    }

    private void sendEmailMessage(String messageBody, AlertConfig alertConfig) {
        InternetAddress addressFrom = null;
        Transport trns = null;
        Session mailSession = null;
        Message msg = null;

        EmailGateway emailGateway = EmailGateway.getInstance();

        try {
            String fromAddress = alertConfig.getFromAddress();
            String mailHost = alertConfig.getMailHost();
            String userName = alertConfig.getUserName();
            String password = alertConfig.getPassword();
            String mailTransport = alertConfig.getTransport();
            addressFrom = new InternetAddress(fromAddress);
            InternetAddress[] toEmailAddrs = new InternetAddress[1];

            String toAddress = alertConfig.getToEmailAddr();
            toEmailAddrs[0] = new InternetAddress(toAddress);
            String subject = alertConfig.getSubject();
            mailSession = emailGateway.getMailSession();
            msg = new MimeMessage(mailSession);
            trns = mailSession.getTransport(mailTransport);
            trns.connect(mailHost, userName, password);
            msg.setFrom(addressFrom);
            msg.setSubject(subject);
            msg.setContent(messageBody, "text/html");
            trns.sendMessage(msg, toEmailAddrs);

        } catch (AddressException e) {
            log.error("Invalid email Address", e);
        } catch (MessagingException e) {
            log.error("Messaging exception", e);
        }

    }
}
