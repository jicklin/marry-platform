package com.marry.gen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marry.domain.entity.GenTable;
import com.marry.domain.entity.GenTableColumn;
import com.marry.gen.service.IGenTableService;
import com.marry.persistence.mapper.GenTableColumnMapper;
import com.marry.persistence.mapper.GenTableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements IGenTableService {

    private final GenTableColumnMapper columnMapper;
    private final DataSource dataSource;
    private final VelocityEngine velocityEngine;

    @Override
    public List<GenTable> listDbTables() {
        try (Connection conn = dataSource.getConnection()) {
            Set<String> skip = Set.of("flyway_schema_history", "qrtz_");
            List<GenTable> tables = new ArrayList<>();
            ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), "public", "%", new String[]{"TABLE"});
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (skip.stream().anyMatch(name::startsWith)) continue;
                GenTable t = new GenTable();
                t.setTableName(name);
                t.setTableComment(rs.getString("REMARKS"));
                tables.add(t);
            }
            return tables;
        } catch (Exception e) {
            log.error("listDbTables", e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public void importTables(List<String> tableNames) {
        try (Connection conn = dataSource.getConnection()) {
            for (String tn : tableNames) {
                if (baseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GenTable>().eq("table_name", tn)) > 0) {
                    continue;
                }
                GenTable t = new GenTable();
                t.setTableName(tn);
                try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), "public", tn, new String[]{"TABLE"})) {
                    if (rs.next()) t.setTableComment(rs.getString("REMARKS"));
                }
                t.setClassName(toCamelUpper(tn));
                t.setTplCategory("crud");
                t.setPackageName("com.marry.app");
                t.setModuleName(tn.replaceFirst("^sys_", "").split("_")[0]);
                t.setBusinessName(tn);
                t.setFunctionName(tn);
                t.setGenType("zip");
                baseMapper.insert(t);

                int order = 1;
                try (ResultSet rs = conn.getMetaData().getColumns(null, "public", tn, "%")) {
                    while (rs.next()) {
                        GenTableColumn c = new GenTableColumn();
                        c.setTableId(t.getId());
                        c.setColumnName(rs.getString("COLUMN_NAME"));
                        c.setColumnComment(rs.getString("REMARKS"));
                        c.setColumnType(rs.getString("TYPE_NAME") + (rs.getInt("COLUMN_SIZE") > 0 ? "(" + rs.getInt("COLUMN_SIZE") + ")" : ""));
                        c.setJavaType(sqlTypeToJava(rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE")));
                        c.setJavaField(toCamelLower(rs.getString("COLUMN_NAME")));
                        c.setIsPk("id".equalsIgnoreCase(rs.getString("COLUMN_NAME")) ? 1 : 0);
                        c.setSort(order++);
                        c.setIsInsert(1);
                        c.setIsEdit(1);
                        c.setIsList(1);
                        c.setIsQuery(1);
                        c.setHtmlType("input");
                        c.setQueryType("EQ");
                        columnMapper.insert(c);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("importTables failed", e);
        }
    }

    @Override
    public void syncColumns(Long tableId) {
        GenTable table = baseMapper.selectById(tableId);
        if (table == null) return;
        columnMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GenTableColumn>().eq("table_id", tableId));
        try (Connection conn = dataSource.getConnection()) {
            int order = 1;
            try (ResultSet rs = conn.getMetaData().getColumns(null, "public", table.getTableName(), "%")) {
                while (rs.next()) {
                    GenTableColumn c = new GenTableColumn();
                    c.setTableId(tableId);
                    c.setColumnName(rs.getString("COLUMN_NAME"));
                    c.setColumnComment(rs.getString("REMARKS"));
                    c.setColumnType(rs.getString("TYPE_NAME"));
                    c.setJavaType(sqlTypeToJava(rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE")));
                    c.setJavaField(toCamelLower(rs.getString("COLUMN_NAME")));
                    c.setIsPk("id".equalsIgnoreCase(rs.getString("COLUMN_NAME")) ? 1 : 0);
                    c.setSort(order++);
                    c.setIsInsert(1);
                    c.setIsEdit(1);
                    c.setIsList(1);
                    c.setIsQuery(1);
                    c.setHtmlType("input");
                    c.setQueryType("EQ");
                    columnMapper.insert(c);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("syncColumns failed", e);
        }
    }

    @Override
    public List<GenTableColumn> listColumns(Long tableId) {
        return columnMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GenTableColumn>()
                .eq("table_id", tableId).orderByAsc("sort"));
    }

    @Override
    public byte[] generateZip(Long tableId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeZip(tableId, out);
        return out.toByteArray();
    }

    @Override
    public void writeZip(Long tableId, ByteArrayOutputStream out) {
        GenTable table = baseMapper.selectById(tableId);
        if (table == null) return;
        List<GenTableColumn> cols = listColumns(tableId);

        VelocityContext ctx = new VelocityContext();
        ctx.put("tableName", table.getTableName());
        ctx.put("className", table.getClassName());
        ctx.put("classNameLower", StrUtil.lowerFirst(table.getClassName()));
        ctx.put("functionName", table.getFunctionName());
        ctx.put("moduleName", table.getModuleName());
        ctx.put("packageName", table.getPackageName());
        ctx.put("columns", cols);
        ctx.put("PkColumn", cols.stream().filter(c -> c.getIsPk() != null && c.getIsPk() == 1).findFirst().orElse(null));
        ctx.put("datetime", java.time.LocalDateTime.now().toString());

        String pkgPath = table.getPackageName().replace('.', '/');

        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            addZipEntry(zos, ctx,
                    "templates/Controller.java.vm",
                    pkgPath + "/controller/" + table.getClassName() + "Controller.java");
            addZipEntry(zos, ctx,
                    "templates/Service.java.vm",
                    pkgPath + "/service/I" + table.getClassName() + "Service.java");
            addZipEntry(zos, ctx,
                    "templates/ServiceImpl.java.vm",
                    pkgPath + "/service/impl/" + table.getClassName() + "ServiceImpl.java");
            addZipEntry(zos, ctx,
                    "templates/Mapper.java.vm",
                    pkgPath + "/mapper/" + table.getClassName() + "Mapper.java");
            addZipEntry(zos, ctx,
                    "templates/Entity.java.vm",
                    pkgPath + "/entity/" + table.getClassName() + ".java");
            addZipEntry(zos, ctx,
                    "templates/Vue.vue.vm",
                    "vue/" + table.getModuleName() + "/" + StrUtil.lowerFirst(table.getClassName()) + "/index.vue");
            addZipEntry(zos, ctx,
                    "templates/Api.ts.vm",
                    "vue/api/" + StrUtil.lowerFirst(table.getClassName()) + ".ts");
        } catch (Exception e) {
            log.error("writeZip failed", e);
        }
    }

    private void addZipEntry(ZipOutputStream zos, VelocityContext ctx, String tmplPath, String outPath) throws Exception {
        Template t;
        try {
            t = velocityEngine.getTemplate(tmplPath);
        } catch (Exception e) {
            log.debug("[gen] template {} missing; skipping", tmplPath);
            return;
        }
        StringWriter sw = new StringWriter();
        t.merge(ctx, sw);
        zos.putNextEntry(new ZipEntry(outPath));
        zos.write(sw.toString().getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String sqlTypeToJava(String type, int size) {
        if (type == null) return "String";
        switch (type.toUpperCase(Locale.ROOT)) {
            case "INT", "INTEGER", "SMALLINT", "TINYINT" -> { return "Integer"; }
            case "BIGINT" -> { return "Long"; }
            case "DECIMAL", "NUMERIC", "DOUBLE", "REAL" -> { return "java.math.BigDecimal"; }
            case "FLOAT" -> { return "Float"; }
            case "BIT", "BOOLEAN" -> { return "Boolean"; }
            case "DATE", "TIMESTAMP" -> { return "java.time.LocalDateTime"; }
            case "TIME" -> { return "java.time.LocalTime"; }
            default -> { return "String"; }
        }
    }

    private String toCamelUpper(String s) {
        StringBuilder sb = new StringBuilder();
        boolean up = true;
        for (char c : s.toCharArray()) {
            if (c == '_') { up = true; continue; }
            sb.append(up ? Character.toUpperCase(c) : c);
            up = false;
        }
        return sb.toString();
    }

    private String toCamelLower(String s) {
        String upper = toCamelUpper(s);
        return StrUtil.lowerFirst(upper);
    }
}