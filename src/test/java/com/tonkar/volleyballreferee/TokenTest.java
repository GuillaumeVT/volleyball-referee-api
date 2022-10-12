package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.configuration.VbrTestConfiguration;
import com.tonkar.volleyballreferee.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@Import(VbrTestConfiguration.class)
@ActiveProfiles("test")
public class TokenTest {

    @Test
    void test(@Autowired SubscriptionService subscriptionService) {
        subscriptionService.validatePurchaseToken("bcnaebfecopnllljkdejhakl.AO-J1OxCcVKqbC2ig7mo_ACek71uiwmgYI32rzRCT4A-b9_8B0Zivyt95FweTl_yH0k8WKHa5HPKesbQki-i6mLtAS7ASPbWRNTsC3VIPJEPRCqA4i0l7nk");
        subscriptionService.validatePurchaseToken("dbpcifhlckhcmejbikiicfgg.AO-J1OwO7sMpbtLoDhn26TnFoioNEWxivaBtgP1rhH33WRK0X2lskeGDeqKAjDJlrBqaLJyRymsbyE_wBdQYilBsThokuZlY_UpCNgfJ3yEGQA3qxvAw368");
        subscriptionService.validatePurchaseToken("mjhkcmchjehkjidlaflbglmh.AO-J1Ow81uevUSYEANXi6ekVw6gSsML7b9NWhPfdiwrS2MkQHGXuJp2CEdkF2kLM_rZ3z7BGkpVyYTUd0yWCKTHcsGs7U4rhiMNvGVzajIskwM9DPSLEv4E");
        subscriptionService.validatePurchaseToken("bjaahofpgoacianmackhlikk.AO-J1OydmUFcG9jzvJgTrsCq15utavqcXkqOHeozZrVX4V8981TUCai_04o-il1aaYbvDKjz-Wx7CdbWbm0vS33x-UPNq3fOnUL81-hDiPmCZsoDjFXhV2SELoD-exYbm3XGOmszl9oo");
        subscriptionService.validatePurchaseToken("lihnomnpemmlfcdkehddfphp.AO-J1Oy-YgHeDHBd3fEJDGdmjcztgMSydzF-xEURsICnxgre6TZckQ0M_wIXjthjFxlpcxYhsp0uvyeU0YthteOMTGa-9CLKlmI5yYaXCKVfojoXvqLSBZjjWkio8jia4VPfgC61ysZe");
        subscriptionService.validatePurchaseToken("akahalokohepedhigeipfhlj.AO-J1OxQFvyAqhAPhIEOyT4NVmd0oEO-I_6QivKz44yqlmKqemW7nkCreqVFdMa5KzMkrQAmit-ZehuZmnXlLgpP3sIAtpno98rzSITnZdy5M6jeqxfQkdZKscR6xypL1sjSml11-eVY");
        subscriptionService.validatePurchaseToken("kddojjciceinalmoelndajfl.AO-J1OxnrZLDun4s6JbYVS7h5B2ZWM7hn42SOcsA0M_6dAp0A28xYl6tDSgeXg4vSbA9XKtCIhHOFiICWhq7i6yC_0ToArYxgWS5Y8okQ3_TdmmSGWYBqD5XdPWf9z1FagKQ0PkOZgLB");
        subscriptionService.validatePurchaseToken("hjdjknnmkeocmdojggebknjh.AO-J1OzR0w3erRvZicP99GV905V3_SeWi9szaUrMEC2jVNevOHLfrWKmqSfkjN_vBNwieAhU-IMJqSOmQPZF0m9FBUmhf2cIS00_S6hT61ScwwKfjJ7sa84");
        subscriptionService.validatePurchaseToken("ppdnmhnopchppdggnglmnjlk.AO-J1Oxj6_rF6uJqx4XTnVlEyuilSodJUgClD0Tw4OH0ziNN4d4x9Ckv6mYXa4xWSL-Z6I4HPpsmO-_WLy-0wMH-p5N8HIA-qVzK8jVK-MazOWrCvYH5SpE");
        subscriptionService.validatePurchaseToken("ocacbbkknofdodbdgocjggnf.AO-J1Oy-chLXoWbjMO4xSxpnSwOyUczuUh59SLQv47MjvJ6mvLXiOvl_TQND-DyooUOI1RZKtYEFVs8n6zFDJIf4C4Da61vyhAjN6IlAkZXIiJUpCDp0T0oxHuEOzKU6lfse_8TXSOB3");
        subscriptionService.validatePurchaseToken("plclncikepgbedhljopgdoca.AO-J1Ozez_Greq79W8151t48iZ_8g8yizxXoYQ4Q7a4DLvcVaGeSmybIJfqZ-XZPY8VLHg4BDEipy1bbRaSenbFlye6OieCWsSNSjGNN_T7VApwapb0vOlnM-l4LeAchm_EIPw5vRJDX");
    }
}
