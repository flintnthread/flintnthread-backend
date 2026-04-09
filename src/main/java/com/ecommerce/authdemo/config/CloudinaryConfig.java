package com.ecommerce.authdemo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

    @Configuration
    public class CloudinaryConfig {

        @Bean
        public Cloudinary cloudinary() {
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dslsxj2uw",
                    "api_key", "373137988284327",
                    "api_secret", "ou3VsL_zQX7zuBu9XTFexjIxLCU",
                    "secure", true
            ));
        }
    }

