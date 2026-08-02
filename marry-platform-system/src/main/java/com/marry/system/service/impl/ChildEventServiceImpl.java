package com.marry.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.common.base.PageQuery;
import com.marry.common.core.domain.BizCode;
import com.marry.common.core.exception.BizException;
import com.marry.domain.entity.ChildEvent;
import com.marry.domain.entity.ChildEventFile;
import com.marry.domain.entity.SysFile;
import com.marry.persistence.mapper.ChildEventFileMapper;
import com.marry.persistence.mapper.ChildEventMapper;
import com.marry.persistence.mapper.SysFileMapper;
import com.marry.security.util.SecurityUtil;
import com.marry.system.service.IChildEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildEventServiceImpl extends ServiceImpl<ChildEventMapper, ChildEvent> implements IChildEventService {

    /** Illegal filename characters replaced with underscore in dir names. */
    private static final Pattern ILLEGAL_DIR_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\s]");

    private final ChildEventFileMapper eventFileMapper;
    private final SysFileMapper sysFileMapper;

    @Override
    public IPage<ChildEvent> page(PageQuery q, String keyword, String category, String tag,
                                  Integer importance, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<ChildEvent> w = new LambdaQueryWrapper<>();
        // Strict data isolation: a user only ever sees their own events.
        w.eq(ChildEvent::getCreateBy, SecurityUtil.currentUserId());
        if (StrUtil.isNotBlank(keyword)) {
            w.and(x -> x.like(ChildEvent::getTitle, keyword).or().like(ChildEvent::getContent, keyword));
        }
        if (StrUtil.isNotBlank(category)) w.eq(ChildEvent::getCategory, category);
        if (StrUtil.isNotBlank(tag)) w.like(ChildEvent::getTags, tag);
        if (importance != null) w.eq(ChildEvent::getImportance, importance);
        if (startDate != null) w.ge(ChildEvent::getEventDate, startDate);
        if (endDate != null) w.le(ChildEvent::getEventDate, endDate);
        w.orderByDesc(ChildEvent::getEventDate).orderByDesc(ChildEvent::getCreateTime);
        return baseMapper.selectPage(q.toPage(), w);
    }

    @Override
    public ChildEvent detail(Long id) {
        ChildEvent e = requireOwned(id);
        e.setAttachFiles(listAttachments(id));
        return e;
    }

    @Override
    @Transactional
    public Long create(ChildEvent e) {
        if (StrUtil.isBlank(e.getTitle())) throw new BizException(BizCode.BAD_REQUEST, "请填写事件标题");
        if (e.getEventDate() == null) throw new BizException(BizCode.BAD_REQUEST, "请选择事件日期");
        if (e.getImportance() == null) e.setImportance(0);
        if (StrUtil.isBlank(e.getDirName())) e.setDirName(generateDirName(e));
        Long uid = SecurityUtil.currentUserId();
        e.setCreateBy(uid);
        e.setUpdateBy(uid);
        baseMapper.insert(e);
        return e.getId();
    }

    @Override
    @Transactional
    public void update(ChildEvent e) {
        if (e.getId() == null) throw new BizException(BizCode.BAD_REQUEST, "缺少事件 id");
        requireOwned(e.getId());
        // keep ownership stable; only allow content fields to be updated
        e.setCreateBy(null);
        baseMapper.updateById(e);
    }

    @Override
    @Transactional
    public void remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return;
        for (Long id : ids) {
            requireOwned(id);
            baseMapper.deleteById(id); // logical delete via del_flag
            eventFileMapper.delete(new LambdaQueryWrapper<ChildEventFile>()
                    .eq(ChildEventFile::getEventId, id));
        }
    }

    @Override
    @Transactional
    public void attach(Long eventId, Long fileId, String mediaType) {
        requireOwned(eventId);
        if (fileId == null) throw new BizException(BizCode.BAD_REQUEST, "缺少文件 id");
        SysFile f = sysFileMapper.selectById(fileId);
        if (f == null) throw new BizException(BizCode.BAD_REQUEST, "文件不存在");
        long existing = eventFileMapper.selectCount(new LambdaQueryWrapper<ChildEventFile>()
                .eq(ChildEventFile::getEventId, eventId)
                .eq(ChildEventFile::getFileId, fileId));
        if (existing > 0) return; // idempotent
        ChildEventFile link = new ChildEventFile();
        link.setEventId(eventId);
        link.setFileId(fileId);
        link.setMediaType(StrUtil.isBlank(mediaType) ? "file" : mediaType);
        link.setSortNo(0);
        link.setCreateBy(SecurityUtil.currentUserId());
        eventFileMapper.insert(link);
    }

    @Override
    @Transactional
    public void detach(Long eventId, Long fileId) {
        requireOwned(eventId);
        eventFileMapper.delete(new LambdaQueryWrapper<ChildEventFile>()
                .eq(ChildEventFile::getEventId, eventId)
                .eq(ChildEventFile::getFileId, fileId));
    }

    // ------------------------------------------------------------

    private ChildEvent requireOwned(Long id) {
        ChildEvent e = baseMapper.selectById(id);
        if (e == null) throw new BizException(BizCode.NOT_FOUND, "事件不存在");
        Long uid = SecurityUtil.currentUserId();
        if (e.getCreateBy() != null && !e.getCreateBy().equals(uid)) {
            throw new BizException(BizCode.FORBIDDEN, "无权操作该事件");
        }
        return e;
    }

    private List<ChildEventFile> listAttachments(Long eventId) {
        List<ChildEventFile> links = eventFileMapper.selectList(new LambdaQueryWrapper<ChildEventFile>()
                .eq(ChildEventFile::getEventId, eventId)
                .orderByAsc(ChildEventFile::getSortNo)
                .orderByAsc(ChildEventFile::getId));
        if (CollUtil.isEmpty(links)) return links;
        return links.stream().map(link -> {
            SysFile f = sysFileMapper.selectById(link.getFileId());
            if (f != null) {
                link.setUrl(f.getUrl());
                link.setOriginalName(f.getOriginalName());
                link.setContentType(f.getContentType());
                link.setSize(f.getSize());
            }
            return link;
        }).collect(Collectors.toList());
    }

    /** Generates `{eventDate}_{sanitizedTitle}`, appending _2/_3... when the name is taken. */
    private String generateDirName(ChildEvent e) {
        String base = e.getEventDate() + "_" + ILLEGAL_DIR_CHARS.matcher(e.getTitle().trim()).replaceAll("_");
        if (base.length() > 180) base = base.substring(0, 180);
        String candidate = base;
        int seq = 1;
        while (countByDirName(candidate) > 0) {
            candidate = base + "_" + (++seq);
        }
        return candidate;
    }

    private long countByDirName(String dirName) {
        return baseMapper.selectCount(new LambdaQueryWrapper<ChildEvent>()
                .eq(ChildEvent::getDirName, dirName)
                .eq(ChildEvent::getCreateBy, SecurityUtil.currentUserId()));
    }
}
