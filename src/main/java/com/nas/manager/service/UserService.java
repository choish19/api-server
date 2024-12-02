package com.nas.manager.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nas.manager.model.User;
import com.nas.manager.repository.UserRepository;
import com.nas.manager.util.LogUtil;
import com.nas.manager.model.Setting;
import com.nas.manager.repository.SettingRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LogUtil.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        try {
            return userRepository.findByUsername(username)
                    .map(user -> {
                        List<GrantedAuthority> authorities = user.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
                    })
                    .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
        } catch (UsernameNotFoundException e) {
            logger.error("사용자 이름을 찾을 수 없습니다: " + username, e);
            throw e;
        }
    }

    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            return userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("현재 사용자를 찾을 수 없습니다."));
        } catch (Exception e) {
            logger.error("현재 사용자를 가져오는 데 실패했습니다.", e);
            throw e;
        }
    }

    public User updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            return userRepository.save(existingUser);
        } catch (Exception e) {
            logger.error("사용자 업데이트 중 오류 발생: " + user.getId(), e);
            throw e;
        }
    }

    public Setting updateUserSetting(Setting setting) {
        try {
            User user = getCurrentUser();
            setting.setId(user.getSetting().getId());
            Setting updatedSetting = settingRepository.save(setting);
            user.setSetting(updatedSetting);
            userRepository.save(user);
            return updatedSetting;
        } catch (Exception e) {
            logger.error("사용자 설정 업데이트 중 오류 발생", e);
            throw e;
        }
    }
}