package com.offer.controller;

import com.newrelic.api.agent.Trace;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    /**
     * Just for the healthcheck for AWS
     * @return
     */
    @Trace(skipTransactionTrace = true)
    @RequestMapping("/")
    public String home() {
        return "Offer management, reporting for duty!";
    }

}
