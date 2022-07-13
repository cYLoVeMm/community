package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("2392135045@qq.com", "方小梅小傻瓜","喜欢臭臭的小梅");
    }

    @Test
    public void testHtmlMail(){
        //Context context = new Context();
        //context.setVariable("username", "sunday");
        //
        //String content = templateEngine.process("/mail/demo", context);
        //
        //System.out.println(content);
        //
        //mailClient.sendMail("256882828@qq.com", "HTML", content);
        //发送验证码邮件
        Context context = new Context();
        context.setVariable("email", "506289940@qq.com");
        // http://localhost:8080/community/activation/101/code
        String verificode = CommunityUtil.generateUUID().substring(0, 5);
        context.setVariable("verificode", verificode);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail("506289940@qq.com", "验证码", content);
    }
}
