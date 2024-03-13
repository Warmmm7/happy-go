package com.shark;

import com.shark.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class HappyGoApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop(){
        shopService.saveShop2Redis(1L,10L);
    }
}
