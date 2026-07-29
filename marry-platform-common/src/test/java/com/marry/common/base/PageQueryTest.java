package com.marry.common.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageQueryTest {

    @Test
    void defaults_areApplied() {
        PageQuery q = new PageQuery();
        assertEquals(1, q.getPageNum());
        assertEquals(10, q.getPageSize());
        assertEquals("desc", q.getIsAsc());
    }

    @Test
    void toPage_returnsPageObject() {
        PageQuery q = new PageQuery();
        q.setPageNum(2);
        q.setPageSize(25);
        Page<?> p = q.toPage();
        assertNotNull(p);
        assertEquals(2L, p.getCurrent());
        assertEquals(25L, p.getSize());
    }
}