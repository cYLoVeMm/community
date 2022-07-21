package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.lang.model.element.NestingKind;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }


    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // 忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    /**
     * 账号注册
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经给您的邮箱发送了一封激活邮件，请尽快激活吧！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code

    /**
     * 激活账号
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCESS) {
            model.addAttribute("msg", "激活成功！！");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该用户已激活成功");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码错误");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 图片验证码
     * @param response
     * @param session
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        /*将验证码存入session
        session.setAttribute("kaptcha", text);

        重构验证码方法前
         */

        //重构验证码方法
        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        //将验证🐎的临时凭证存入Cookie
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath) ;
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKet(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text,60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }

    /**
     * 登录账号
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,
                        Model model,/*HttpSession session,*/HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        //验证验证码
        /*String kaptcha = (String) session.getAttribute("kaptcha"); 重构前*/

        String kaptcha = null;
        if (StringUtils.isNoneBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKet(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);

        }//重构后


        if (StringUtils.isBlank(code)){
            model.addAttribute("codeMsg", "验证码不能为空！");
            return "/site/login";
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码错误！");
            return "/site/login";
        }

        //验证账号、密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 退出账号
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //重定向
        return "redirect:/login";
    }

    //@RequestMapping(path = "/",method = RequestMethod.GET)
    //public void getVerifiCode(){
    //    userService.forget(email);
    //    // 发送验证码邮件
    //    Context context = new Context();
    //    context.setVariable("email", user.getEmail());
    //    // http://localhost:8080/community/activation/101/code
    //    String verificode = CommunityUtil.generateUUID();
    //    context.setVariable("verificode", verificode);
    //    String content = templateEngine.process("/mail/forget", context);
    //    mailClient.sendMail(user.getEmail(), "验证码", content);
    //}

    //@RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    //public String getVerifiCode(String email,HttpSession session){
    //    Map<String,Object> map = userService.getCode(email);
    //    if (map.containsKey(email)){
    //        userService.sendEamil(email);
    //    }
    //    return "/site/forget";
    //}

    // 获取验证码
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }
        //// 发送邮件
        //Context context = new Context();
        //context.setVariable("email", email);
        //String code = CommunityUtil.generateUUID().substring(0, 4);
        //context.setVariable("verifyCode", code);
        //String content = templateEngine.process("/mail/forget", context);
        //mailClient.sendMail(email, "找回密码", content);

        Map<String, Object> map = userService.isNull(email);
        if (map.containsKey("user")) {
            // 保存验证码
            session.setAttribute(email + "verifyCode", map.get("code"));
            return CommunityUtil.getJSONString(0);
        }
        return CommunityUtil.getJSONString(1, "查询不到该邮箱注册信息");
    }

    // 重置密码
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute(email + "verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            model.addAttribute("msg","修改密码成功，请重新登录");
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}
