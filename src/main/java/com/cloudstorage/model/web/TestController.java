package com.cloudstorage.model.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("api/test")
public class TestController {
    
    @PostMapping("/v1")
    public String postMethodName(@RequestBody String entity) {
        return entity;
    }
    
}
