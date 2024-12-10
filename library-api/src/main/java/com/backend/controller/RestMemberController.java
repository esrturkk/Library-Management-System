package com.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.model.Member;
import com.backend.services.MemberService;

@RestController
@RequestMapping("/api")
public class RestMemberController {

    @Autowired
    private final MemberService memberService;

    public RestMemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping(path = "/members")
    public List<Member> getAllMembers(){
        return memberService.getAllMembers();
    }
    
}
