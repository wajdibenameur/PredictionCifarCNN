package com.cifar10.security;

import com.cifar10.model.User;
import com.cifar10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {


    @Autowired
    private UserRepository userRepository;

        @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
            System.out.println("Loaded user: " + user.getUsername() + ", hash: " + user.getPassword());
            System.out.println("Loading user: " + username + ", active: " + user.getActive());
            System.out.println("Password hash: " + user.getPassword());

        return UserDetailsImpl.build(user);
    }
}