package com.example.backend.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义 MyBatis 类型处理器
 * 用于将 Java 的 List<String> 与数据库的 VARCHAR (逗号分隔字符串) 之间进行转换
 * 主要用于存储多张商品图片URL
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    private static final String DELIMITER = ",";

    /**
     * 设置参数：将 List<String> 转换为逗号分隔的字符串存入数据库
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, null);
        } else {
            ps.setString(i, String.join(DELIMITER, parameter));
        }
    }

    /**
     * 获取结果：将数据库中的逗号分隔字符串转换为 List<String>
     */
    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseString(value);
    }

    /**
     * 获取结果：根据列索引获取
     */
    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseString(value);
    }

    /**
     * 获取结果：从存储过程获取
     */
    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseString(value);
    }

    /**
     * 将逗号分隔的字符串解析为 List<String>
     */
    private List<String> parseString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(DELIMITER)));
    }
}
