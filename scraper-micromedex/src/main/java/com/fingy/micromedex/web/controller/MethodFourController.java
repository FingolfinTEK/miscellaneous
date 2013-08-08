package com.fingy.micromedex.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fingy.micromedex.util.HtmlUnitUtil;
import com.fingy.scrape.util.JsoupParserUtil;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@Controller
public class MethodFourController extends AbstractMicromedexController {

    @ResponseBody
    @RequestMapping("/open-url")
    @Cacheable(value = "methodFourCache", key = "#href")
    public String openurl(@RequestParam(value = "href", required = true) final String href) throws FailingHttpStatusCodeException,
            MalformedURLException, IOException {
        WebClient webClient = getWebClientHolder().get();
        return JsoupParserUtil.getPageFromUrlWithCookies(href, HtmlUnitUtil.getCookiesAsMap(webClient)).html();
    }

}
