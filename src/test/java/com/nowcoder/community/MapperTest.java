package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.AlphaService;
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
import org.thymeleaf.spring5.context.SpringContextUtils;

import javax.xml.stream.XMLOutputFactory;
import java.text.SimpleDateFormat;
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
}
