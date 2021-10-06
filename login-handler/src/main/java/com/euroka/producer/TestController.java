package com.euroka.producer;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TestController {

//    private TokenStore tokenStore;
//    @RequestMapping(method = RequestMethod.GET, value = "/api/logout")
//    @ResponseBody
//    public Boolean revoke(String token) throws Exception {
//        OAuth2AccessToken tokenObj = tokenStore.readAccessToken(token);
//        tokenStore.removeAccessToken(tokenObj);
//        tokenStore.removeRefreshToken(tokenObj.getRefreshToken());
//        return true;
//    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/accessToken/")
    public String getTokenDetails(@RequestHeader HttpHeaders headers) {
        List<String> getHeader = new ArrayList<String>();
        String getAuthorization = "";
        String returnValue = null;
        if(headers == null){
            return "<script> window.location = \"/auth/login\";</script>";
        }
        getAuthorization = headers.get("authorization").toString();

        if (getAuthorization != null) {
            getAuthorization = getAuthorization.substring(1, getAuthorization.length() - 1);
            getAuthorization = getAuthorization.split("Bearer ")[1];
        }
        returnValue = "<script> localStorage[\"accessToken\"] = \"" + getAuthorization + "\";  window.location = \"/\";</script>";
        return returnValue;
    }


}
