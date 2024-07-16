package com.septangle.momosachiblog.utils;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;


import java.net.*;
import java.util.*;


@Slf4j
@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender mailSender;

    private static final String from = "2934833295@qq.com";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$";

    private static final Set<String> filter = new HashSet<>(List.of("gmail.com"));

    public static void sendEmail(String to, String subject, String content) throws MessagingException {
//        Properties properties = new Properties();
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true");
//        properties.put("mail.smtp.host", "smtp.qq.com");
//        properties.put("mail.smtp.port", "465");
//
//        Session session = Session.getInstance(properties, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(from, "xzvjnzbjlshudgid");
//            }
//        });
//        Message message = new MimeMessage(session);
//        message.setFrom(new InternetAddress(from));
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//        message.setSubject(subject);
//        message.setText(content, true);
//
//        Transport.send(message);



    }


    /**
     *
     * 校验邮箱：1、格式是否正确 2、是否真实有效的邮箱地址
     * 步骤：
     * 1、从dns缓存服务器上查询邮箱域名对应的SMTP服务器地址
     * 2、尝试建立Socket连接
     * 3、尝试发送一条消息给SMTP服务器
     * 4、设置邮件发送者
     * 5、设置邮件接收者
     * 6、检查响应码是否为250(为250则说明这个邮箱地址是真实有效的)
     * @author Michael Ran
     *
     */
    public static boolean emailVerify(String email, String domain) {

        if (!isEmailSyntaxValid(email)) {
            log.error("email {} 的格式检验未通过", email);
            return false;
        }
        String host = email.substring(email.indexOf("@") + 1);

        //该邮箱无法验证
        if (host.equals(domain) || filter.contains(host)) return false;

        try (Socket socket = new Socket()) {
            //查找 mx 记录
            Record[] mxRecords = new Lookup(host, Type.MX).run();

            //不存在 MX 记录那么说明，该邮箱地址不存在
            if (ArrayUtils.isEmpty(mxRecords)) {
                return false;
            }
            String mxHost = ((MXRecord) mxRecords[0]).getTarget().toString();
            //有多个那么选择优先级最大的
            if (mxRecords.length > 1) {
                List<Record> recordList = new ArrayList<>(List.of(mxRecords));
                recordList.sort(Record::compareTo);
                mxHost = ((MXRecord) recordList.get(0)).getTarget().toString();
            }

            // 开始smtp
            socket.connect(new InetSocketAddress(mxHost, 25));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // 超时时间(毫秒)
            long timeout = 6000;
            // 睡眠时间片段(50毫秒)
            int sleepSect = 50;

            // 连接(服务器是否就绪)
            if (getResponseCode(timeout, sleepSect, bufferedReader) != 220) {
                return false;
            }
            log.info("服务器就绪");

            // 握手
            bufferedWriter.write("HELO " + domain + "\r\n");
            bufferedWriter.flush();
            if (getResponseCode(timeout, sleepSect, bufferedReader) != 250) {
                return false;
            }
            log.info("握手成功");
            // 身份
            bufferedWriter.write("MAIL FROM: <check@" + domain + ">\r\n");
            bufferedWriter.flush();
            if (getResponseCode(timeout, sleepSect, bufferedReader) != 250) {
                return false;
            }
            log.info("身份验证成功");
            // 验证
            bufferedWriter.write("RCPT TO: <" + email + ">\r\n");
            bufferedWriter.flush();
            if (getResponseCode(timeout, sleepSect, bufferedReader) != 250) {
                log.info("{}不存在", email);
                return false;
            }
            // 断开
            bufferedWriter.write("QUIT\r\n");
            bufferedWriter.flush();
            return true;
        } catch (NumberFormatException | InterruptedException | IOException e) {
            log.info("{}连接超时", email);
        }
        return false;
    }

    private static int getResponseCode(long timeout, int sleepSect, BufferedReader bufferedReader) throws InterruptedException, NumberFormatException, IOException {
        int code = 0;
        for(long i = sleepSect; i < timeout; i += sleepSect) {
            Thread.sleep(sleepSect);
            if(bufferedReader.ready()) {
                String outline = bufferedReader.readLine();
                // FIXME 读完……
                while(bufferedReader.ready())
                    /*System.out.println(*/bufferedReader.readLine()/*)*/;
                /*System.out.println(outline);*/
                code = Integer.parseInt(outline.substring(0, 3));
                break;
            }
        }
        return code;
    }

    private static List<String> getMx() {

        List<String> res = new ArrayList<>();

        return res;
    }

    public static boolean isEmailSyntaxValid(String email) {
        return email.matches(EMAIL_REGEX);
    }

    public boolean doesHostExist(String email) {
        String host = email.substring(email.indexOf("@") + 1);
        try {
            Inet4Address inet4Address = (Inet4Address) Inet4Address.getByName(host);
        } catch (UnknownHostException e) {
            log.info("[mail validation] host of mail does not exist email=" + email + " - " + e.getMessage());
            return false;
        }
        return true;
    }

    private int hear(BufferedReader in) throws IOException {
        String line = null;
        int res = 0;

        while ((line = in.readLine()) != null) {
            String pfx = line.substring(0, 3);
            try {
                res = Integer.parseInt(pfx);
            } catch (Exception ex) {
                res = -1;
            }
            if (line.charAt(3) != '-')
                break;
        }

        return res;
    }

    private void say(BufferedWriter wr, String text) throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
    }



}