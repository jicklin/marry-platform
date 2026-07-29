package com.marry.gen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marry.domain.entity.GenTable;
import com.marry.domain.entity.GenTableColumn;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface IGenTableService extends IService<GenTable> {

    /** List database tables available for generation (excludes those already imported). */
    List<GenTable> listDbTables();

    /** Import table metadata from the JDBC database into sys tables and return the persisted rows. */
    void importTables(List<String> tableNames);

    /** Sync column metadata from DB. */
    void syncColumns(Long tableId);

    /** Returns columns for the given table. */
    List<GenTableColumn> listColumns(Long tableId);

    /** Generate a ZIP containing Java + XML + Vue files. */
    byte[] generateZip(Long tableId);

    /** Streams the zip bytes to the supplied ByteArrayOutputStream for download. */
    void writeZip(Long tableId, ByteArrayOutputStream out);
}