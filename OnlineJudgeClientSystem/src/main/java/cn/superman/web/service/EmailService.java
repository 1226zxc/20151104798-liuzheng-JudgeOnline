package cn.superman.web.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.mail.Session;

import org.springframework.stereotype.Service;

import cn.superman.constant.ConstantParameter;
import cn.superman.util.Log4JUtil;
import cn.superman.web.constant.MailConstant;
import cn.superman.web.util.mail.Mail;
import cn.superman.web.util.mail.SendMailUtil;

/**
 * Email服务，用于邮箱验证码验证
 * 发送邮件采用多线程方式发送。在Runnable实例方法run()中
 * 定义好发送邮件的规则。最后利用线程池去运行定义好的邮件
 * 发送规则。
 * @author 刘铮
 */
@Service
public class EmailService {

    /**
     * 采用无限多线程的线程池，即使有再多的邮件发送线程也能处理
     */
    private ExecutorService emailExecutorService = Executors.newCachedThreadPool();
    private Semaphore emailThreadSemaphore = new Semaphore(30);

    /**
     * 发送邮件实际的执行方法
     * @param runnable 接受一个已经定义好的邮件发送规则的多线程实例对象
     */
    public void sendEmail(EmailRunnable runnable) {
        try {
            emailThreadSemaphore.acquire();
            emailExecutorService.execute(runnable);
        } catch (Exception e) {
            Log4JUtil.logError(e);
        } finally {
            emailThreadSemaphore.release();
        }
    }

    /**
     * 发送邮件的线程类
     * 包含成员属性
     * - mail
     * - session 与邮箱连接的会话链接
     */
    public static class EmailRunnable implements Runnable {
        /**
         * 即将发送的邮件
         */
        private Mail mail;

        /**
         * session 与邮箱连接的会话链接
         */
        private Session session;

        public EmailRunnable(String emailSubject, String emailContent, String emailReceiver) {

            session = SendMailUtil.createDefaultSession(MailConstant.host.getValue(), MailConstant.userName.getValue(), MailConstant.password.getValue());
            mail = new Mail(MailConstant.userName.getValue(), emailReceiver);
            mail.setSubject(emailSubject);
            mail.setContent(emailContent);
        }

        /**
         * 发送邮箱方法
         */
        @Override
        public void run() {
            try {
                SendMailUtil.sendMail(mail, session, ConstantParameter.DEFAULT_CHARSET_CODE);
            } catch (Exception e) {
                Log4JUtil.logError(e);
            }
        }
    }
}
