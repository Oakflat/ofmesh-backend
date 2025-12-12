package com.ofmesh.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 前端部署后的域名 (用于点击链接跳转)
    // 暂时写 localhost，上线后改成 https://ofmesh.com
    private static final String FRONTEND_URL = "http://localhost:5173";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("OfMesh 安全中心 <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject("OfMesh 身份验证"); // 标题简洁，去掉了【】以防被拦截

            // 生成精美的 HTML 内容
            String htmlContent = buildVerificationEmail(code, to);

            helper.setText(htmlContent, true); // true = 开启 HTML 模式

            mailSender.send(message);
            System.out.println("✅ [邮件服务] 验证码已发送至: " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ [邮件服务] 发送失败: " + e.getMessage());
        }
    }

    /**
     * 构建 HTML 邮件模板
     * 使用 Java Text Blocks (三引号) 编写，所见即所得
     */
    private String buildVerificationEmail(String code, String userEmail) {
        int currentYear = Year.now().getValue();

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                    .header { background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 24px; letter-spacing: 1px; font-weight: 800; }
                    .content { padding: 40px 30px; color: #334155; }
                    .code-box { background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; padding: 20px; text-align: center; margin: 30px 0; }
                    .code { font-size: 32px; font-weight: 800; color: #4f46e5; letter-spacing: 6px; font-family: 'Courier New', monospace; }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; }
                    .link { color: #6366f1; text-decoration: none; margin: 0 5px; }
                    .link:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>OfMesh</h1>
                    </div>
                    
                    <div class="content">
                        <h2 style="margin-top: 0; color: #1e293b;">身份验证</h2>
                        <p style="font-size: 16px; line-height: 1.6;">尊敬的用户：</p>
                        <p style="font-size: 16px; line-height: 1.6;">
                            您正在进行敏感操作（注册、登录或修改密码）。请使用下方的验证码完成验证。
                        </p>
                        
                        <div class="code-box">
                            <span class="code">%s</span>
                        </div>
                        
                        <p style="font-size: 14px; color: #64748b; text-align: center;">
                            验证码 <strong>5分钟</strong> 内有效。<br>
                            如果这不是您的操作，请忽略此邮件，您的账号是安全的。
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿直接回复。</p>
                        <p>
                            &copy; %d OfMesh Network. All rights reserved.
                        </p>
                        <p>
                            <a href="%s/privacy" class="link">隐私政策</a> • 
                            <a href="%s/terms" class="link">用户协议</a> • 
                            <a href="%s/help" class="link">帮助中心</a>
                        </p>
                        <p style="margin-top: 10px; color: #cbd5e1;">Sent to %s</p>
                    </div>
                </div>
            </body>
            </html>
            """, code, currentYear, FRONTEND_URL, FRONTEND_URL, FRONTEND_URL, userEmail);
    }
}