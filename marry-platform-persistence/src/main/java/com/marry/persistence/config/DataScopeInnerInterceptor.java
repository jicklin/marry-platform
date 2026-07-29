package com.marry.persistence.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.marry.common.security.DataScopeContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * MyBatis-Plus inner interceptor that appends WHERE clauses based on the
 * current user's data-scope.
 *
 * <p>Reads from {@link DataScopeContext} (thread-local), populated by the auth
 * filter. Decouples this module from the security module.</p>
 *
 * <p>Allowed strategies:
 * <ul>
 *   <li>1 = ALL              → no change</li>
 *   <li>2 = DEPT             → {@code dept_id = currentDeptId}</li>
 *   <li>3 = DEPT_AND_CHILD   → same as 2 here; full subtree logic is app-side</li>
 *   <li>4 = SELF             → {@code create_by = currentUsername}</li>
 *   <li>5 = CUSTOM           → {@code dept_id IN (custom set)}</li>
 * </ul>
 */
@Slf4j
public class DataScopeInnerInterceptor implements InnerInterceptor {

    /** Tables whose queries are exempt from data-scope filtering (RBAC metadata). */
    private static final Set<String> IGNORE_TABLES = Set.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_user_role", "sys_role_menu", "sys_role_dept");

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            org.apache.ibatis.session.RowBounds rowBounds, ResultHandler resultHandler,
                            BoundSql boundSql) {
        String sql = boundSql.getSql();
        if (sql == null) return;
        try {
            String table = extractTable(sql);
            if (table == null || IGNORE_TABLES.contains(table.toLowerCase())) return;

            Collection<Integer> scopes = DataScopeContext.getScopes();
            if (scopes.isEmpty() || scopes.contains(1)) return; // ALL

            List<net.sf.jsqlparser.expression.Expression> extras = buildClauses(scopes);
            if (extras.isEmpty()) return;

            String combined = appendWhere(sql, extras);
            if (!combined.equals(sql)) {
                setSqlSafely(boundSql, combined);
            }
        } catch (Exception e) {
            log.debug("[DataScope] skip due to parse error: {}", e.getMessage());
        }
    }

    private List<net.sf.jsqlparser.expression.Expression> buildClauses(Collection<Integer> scopes) {
        List<net.sf.jsqlparser.expression.Expression> list = new ArrayList<>();
        Long deptId = DataScopeContext.getDeptId();
        String username = DataScopeContext.getUsername();

        for (Integer s : scopes) {
            switch (s) {
                case 2, 3 -> list.add(makeEq("dept_id", deptId));
                case 4 -> list.add(makeEq("create_by", username == null ? "" : username));
                case 5 -> {
                    Set<Long> custom = DataScopeContext.getCustomDepts();
                    if (custom != null && !custom.isEmpty()) list.add(makeIn("dept_id", custom));
                }
                default -> { /* ignore */ }
            }
        }
        return list;
    }

    /**
     * MyBatis BoundSql.sql is final in older versions. Use reflection to swap it.
     * If reflection fails (signature change), fall back to a no-op (logging only).
     */
    private void setSqlSafely(BoundSql boundSql, String newSql) {
        try {
            java.lang.reflect.Field f = BoundSql.class.getDeclaredField("sql");
            f.setAccessible(true);
            f.set(boundSql, newSql);
        } catch (Exception e) {
            log.warn("[DataScope] cannot modify BoundSql.sql — {}", e.getMessage());
        }
    }

    private net.sf.jsqlparser.expression.Expression makeEq(String col, Object val) {
        EqualsTo eq = new EqualsTo();
        eq.setLeftExpression(new Column(col));
        if (val == null) {
            eq.setRightExpression(new StringValue(""));
        } else if (val instanceof Long l) {
            eq.setRightExpression(new LongValue(l));
        } else {
            eq.setRightExpression(new StringValue(val.toString()));
        }
        return eq;
    }

    private net.sf.jsqlparser.expression.Expression makeIn(String col, Collection<Long> vals) {
        InExpression in = new InExpression();
        in.setLeftExpression(new Column(col));
        ExpressionList list = new ExpressionList();
        List<net.sf.jsqlparser.expression.Expression> items = new ArrayList<>();
        for (Long v : vals) items.add(new LongValue(v));
        list.setExpressions(items);
        in.setRightExpression(list);
        return in;
    }

    /** Best-effort append of AND-clauses via JSqlParser; falls back to string concat. */
    private String appendWhere(String sql, List<net.sf.jsqlparser.expression.Expression> extras) {
        try {
            Select stmt = (Select) CCJSqlParserUtil.parse(sql);
            if (stmt.getSelectBody() instanceof PlainSelect ps) {
                net.sf.jsqlparser.expression.Expression where = ps.getWhere();
                for (net.sf.jsqlparser.expression.Expression ex : extras) {
                    where = (where == null)
                            ? ex
                            : new net.sf.jsqlparser.expression.operators.conditional.AndExpression(where, ex);
                }
                ps.setWhere(where);
                return stmt.toString();
            }
        } catch (JSQLParserException ignored) {
            // fall through to naive append
        }
        StringBuilder sb = new StringBuilder(sql);
        for (net.sf.jsqlparser.expression.Expression ex : extras) {
            sb.append(" AND ").append(ex);
        }
        return sb.toString();
    }

    private String extractTable(String sql) {
        if (sql == null) return null;
        String upper = sql.toUpperCase();
        int idx = upper.indexOf(" FROM ");
        if (idx < 0) idx = upper.indexOf(" JOIN ");
        if (idx < 0) return null;
        String rest = sql.substring(idx + 6).trim();
        int sp = rest.indexOf(' ');
        int br = rest.indexOf('(');
        int end = rest.length();
        if (sp > 0) end = Math.min(end, sp);
        if (br > 0) end = Math.min(end, br);
        String name = rest.substring(0, end).trim();
        if (name.startsWith("`") || name.startsWith("\"")) name = name.substring(1);
        if (name.endsWith("`") || name.endsWith("\"")) name = name.substring(0, name.length() - 1);
        return name.isBlank() ? null : name;
    }
}