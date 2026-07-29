package com.marry.system.service;

import com.marry.domain.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileService {

    SysFile upload(MultipartFile file) throws IOException;

    SysFile getById(Long id);
}