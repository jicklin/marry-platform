package com.marry.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.marry.common.core.exception.BizException;
import com.marry.common.core.domain.BizCode;
import com.marry.domain.entity.SysFile;
import com.marry.persistence.mapper.SysFileMapper;
import com.marry.system.service.IFileService;
import com.marry.system.service.props.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements IFileService {

    private final SysFileMapper fileMapper;
    private final FileStorageProperties props;

    @Override
    public SysFile upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "文件为空");
        }
        String original = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(original);

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storageKey = props.getBucket() + "/" + dateDir + "/" + IdUtil.fastSimpleUUID() +
                (suffix.isBlank() ? "" : "." + suffix);
        String relativePath = storageKey;
        File target = new File(props.getPath(), relativePath);
        FileUtil.mkParentDirs(target);
        file.transferTo(target);

        SysFile record = new SysFile();
        record.setName(target.getName());
        record.setOriginalName(original);
        record.setBucket(props.getBucket());
        record.setPath(relativePath);
        record.setUrl(props.getPublicBase() + relativePath);
        record.setContentType(file.getContentType());
        record.setSize(file.getSize());
        record.setStorageType("local");
        fileMapper.insert(record);
        log.info("[File] uploaded id={} path={}", record.getId(), relativePath);
        return record;
    }

    @Override
    public SysFile getById(Long id) {
        return fileMapper.selectById(id);
    }
}