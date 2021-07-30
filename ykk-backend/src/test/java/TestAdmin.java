import com.yikekong.YkkApplication;
import com.yikekong.entity.AdminEntity;
import com.yikekong.mapper.AdminMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = YkkApplication.class)
@RunWith(SpringRunner.class)
public class TestAdmin {


    @Resource
    private AdminMapper adminMapper;

    /**
     * 数据库没有用户，新建一个。admin admin
     */
    @Test
    public void addUser() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String admin = passwordEncoder.encode("admin");
        AdminEntity user = new AdminEntity();
        user.setLoginName("admin");
        user.setPassword(admin);

        System.out.println(user.getPassword());
        adminMapper.insert(user);
    }
}
