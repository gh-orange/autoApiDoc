package pers.cz.autoapidoc.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.context.annotation.ComponentScan;
import pers.cz.autoapidoc.common.PathType;

//@EnableAutoConfiguration
//@SpringBootApplication
//@SpringBootTest(classes = {AutoApiDocUtilTest.class})
@ComponentScan("pers.cz.**")
@RunWith(BlockJUnit4ClassRunner.class)
public class AutoApiDocUtilTest {

    @Test
    public void createApiDoc() {
        new AutoApiDocUtil().createApiDoc("pers.cz.autoapidoc.controller.UserController", PathType.TestClassPath);
    }
}
