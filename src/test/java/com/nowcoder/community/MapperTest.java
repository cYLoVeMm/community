package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        User user1 = userMapper.selectByName("guanyu");
        User user2 = userMapper.selectByEmail("nowcoder115@sina.com");
        System.out.println(user);
        System.out.println(user1);
        System.out.println(user2);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("潘长源");
        user.setPassword("278157");
        user.setSalt("abc");
        user.setEmail("yanlovemei@cm.love");
        user.setHeaderUrl("http://nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
        System.out.println(userMapper.selectById(user.getId()));
    }

    @Test
    public void updateUser(){
        System.out.println(userMapper.updateStatus(151, 1));
        System.out.println(userMapper.updateHeader(151, "http://nowcoder.com/151.png"));
        System.out.println(userMapper.updatePassword(151, "115927"));
        System.out.println(userMapper.selectById(151));
    }

    @Test
    public void testDiscussPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }

        System.out.println(discussPostMapper.selectDiscussPostRows(149));
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setTicket("cylovemm");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        System.out.println(loginTicketMapper.selectByTicket("cylovemm"));
    }

    @Test
    public void testUpdateLoginTicket(){
        loginTicketMapper.updateStatus("cylovemm", 1);
        System.out.println(loginTicketMapper.selectByTicket("cylovemm"));
    }

    @Test
    public void testMessage(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        List<Message> list1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list1) {
            System.out.println(message);

        }

        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(131,"111_131"));
    }
}
