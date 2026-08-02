package com.marry.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements IFileService {

    /**
     * Chars kept in stored filenames: ASCII word chars, dot, CJK, hyphen.
     * Everything else (spaces, parens, #, %, &, …) becomes {@code _} so the
     * persisted url stays markdown/URL-safe while keeping readable names.
     */
    private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[^\\w.\\u4e00-\\u9fa5\\-]");

    private static final int MAX_NAME_LEN = 180;

    private final SysFileMapper fileMapper;
    private final FileStorageProperties props;

    @Override
    public SysFile upload(MultipartFile file) throws IOException {
        return upload(file, null);
    }

    @Override
    public SysFile upload(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "文件为空");
        }
        if (StrUtil.isNotBlank(dir)) {
            validateDir(dir);
        }
        String original = file.getOriginalFilename();

        String relativeDir;
        if (StrUtil.isNotBlank(dir)) {
            relativeDir = props.getBucket() + "/" + props.getEventRoot() + "/" + dir;
        } else {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            relativeDir = props.getBucket() + "/" + dateDir;
        }

        String fileName = uniqueFileName(relativeDir, original);
        String relativePath = relativeDir + "/" + fileName;
        File target = new File(props.getPath(), relativePath);
        FileUtil.mkParentDirs(target);
        file.transferTo(target);

        SysFile record = new SysFile();
        record.setName(fileName);
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

    /**
     * Resolves the stored filename inside {@code relativeDir}, preferring the
     * client-supplied original name. When the name is taken, {@code _2/_3/…}
     * is appended before the extension, mirroring the event-dir convention.
     */
    private String uniqueFileName(String relativeDir, String original) {
        String safe = sanitizeFileName(original);
        if (safe.isEmpty()) {
            String suffix = StrUtil.isBlank(original) ? "" : FileUtil.getSuffix(original);
            safe = IdUtil.fastSimpleUUID() + (suffix.isBlank() ? "" : "." + suffix);
        }
        String candidate = safe;
        File target = new File(props.getPath(), relativeDir + "/" + candidate);
        int seq = 1;
        while (target.exists()) {
            int dot = safe.lastIndexOf('.');
            String stem = dot > 0 ? safe.substring(0, dot) : safe;
            String ext = dot > 0 ? safe.substring(dot) : "";
            candidate = stem + "_" + (++seq) + ext;
            target = new File(props.getPath(), relativeDir + "/" + candidate);
        }
        return candidate;
    }

    /**
     * Cleans the original filename: strips client-supplied path segments,
     * drops leading dots (hidden / ".." names), keeps only URL-safe chars plus
     * CJK, and caps the stem length. Returns "" when nothing usable remains.
     */
    private String sanitizeFileName(String original) {
        if (StrUtil.isBlank(original)) return "";
        String name = original.trim().replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("^\\.+", "");
        name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("_");
        name = name.trim().replaceAll("^_+|_+$", "");
        if (name.isEmpty() || name.equals(".") || name.equals("..")) return "";
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        if (stem.length() > MAX_NAME_LEN) stem = stem.substring(0, MAX_NAME_LEN);
        return stem + ext;
    }

    /**
     * Path-traversal guard for the optional custom directory. Only allows safe
     * relative segments (letters, digits, CJK, space, -, _, /, .) without any
     * leading separator or ".." escape.
     */
    private void validateDir(String dir) {
        String d = dir.trim();
        if (d.isEmpty()) throw new BizException(BizCode.BAD_REQUEST, "非法目录名");
        if (d.startsWith("/") || d.startsWith(".")) throw new BizException(BizCode.BAD_REQUEST, "非法目录名");
        if (d.contains("..")) throw new BizException(BizCode.BAD_REQUEST, "非法目录名");
        if (d.length() > 100) throw new BizException(BizCode.BAD_REQUEST, "目录名过长");
        if (!d.matches("^[\\w\\u4e00-\\u9fa5\\-\\s/.]+$")) throw new BizException(BizCode.BAD_REQUEST, "非法目录名");
    }

    @Override
    public SysFile getById(Long id) {
        return fileMapper.selectById(id);
    }
}