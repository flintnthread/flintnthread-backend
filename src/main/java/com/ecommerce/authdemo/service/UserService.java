package com.ecommerce.authdemo.service;


import com.ecommerce.authdemo.dto.ProfileResponseDTO;
import com.ecommerce.authdemo.dto.UpdateProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

        ProfileResponseDTO getProfile();

        ProfileResponseDTO updateProfile(UpdateProfileDTO dto);

    Object uploadProfileImage(MultipartFile file);

    void deleteAccount();
    }

