package com.fandesunstar.eship;

import com.fandesunstar.eship.entity.User;
import com.fandesunstar.eship.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库连接测试类
 */
@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    /**
     * 测试数据库连接
     */
    @Test
    public void testConnection() throws Exception {
        Connection connection = dataSource.getConnection();
        System.out.println("数据库连接成功！");
        System.out.println("数据库类型：" + connection.getMetaData().getDatabaseProductName());
        System.out.println("数据库版本：" + connection.getMetaData().getDatabaseProductVersion());
        System.out.println("连接URL：" + connection.getMetaData().getURL());
        connection.close();
    }

    /**
     * 测试查询管理员用户
     */
    @Test
    public void testQueryAdmin() {
        User admin = userMapper.selectByUsername("admin");
        if (admin != null) {
            System.out.println("查询管理员成功！");
            System.out.println("用户名：" + admin.getUsername());
            System.out.println("邮箱：" + admin.getEmail());
            System.out.println("角色：" + admin.getRole());
            System.out.println("公司：" + admin.getCompanyName());
        } else {
            System.out.println("未找到管理员用户");
        }
    }

    /**
     * 测试统计用户数量
     */
    @Test
    public void testCountUsers() {
        Long totalCount = userMapper.countTotal();
        Long adminCount = userMapper.countByRole("ADMIN");
        Long ownerCount = userMapper.countByRole("SHIP_OWNER");
        Long renterCount = userMapper.countByRole("RENTER");

        System.out.println("用户统计：");
        System.out.println("总用户数：" + totalCount);
        System.out.println("管理员数：" + adminCount);
        System.out.println("船东数：" + ownerCount);
        System.out.println("租家数：" + renterCount);
    }
}
