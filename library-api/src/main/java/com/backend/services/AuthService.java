package com.backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.model.Member;
import com.backend.repository.MemberRepository;

@Service
public class AuthService {
     private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(Member member) {
        // Kullanıcının var olup olmadığını kontrol et
        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            return "Username already exists!";
        }

        // Şifreyi encode et ve kullanıcıyı kaydet
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);

        return "User registered successfully!";
    }
}
