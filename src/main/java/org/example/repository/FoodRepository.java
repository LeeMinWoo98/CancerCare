package org.example.repository;

import org.example.domain.CancerType;
import org.example.domain.FoodItem;
import org.example.domain.FoodTableType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class FoodRepository {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<FoodItem> foodItemRowMapper;
    
    public FoodRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.foodItemRowMapper = new FoodItemRowMapper();
    }
    
    public List<FoodItem> findRandomFoodsByType(FoodTableType tableType, CancerType cancerType, 
                                               int count, List<Long> excludeIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, ")
           .append(tableType.getNameColumn())
           .append(" AS name, info FROM ")
           .append(tableType.getTableName())
           .append(" WHERE ")
           .append(cancerType.getColumnName())
           .append(" = 1");
        
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("count", count);
        
        if (excludeIds != null && !excludeIds.isEmpty()) {
            sql.append(" AND id NOT IN (:excludeIds)");
            params.addValue("excludeIds", excludeIds);
        }
        
        sql.append(" ORDER BY RAND() LIMIT :count");
        
        try {
            return namedParameterJdbcTemplate.query(sql.toString(), params, foodItemRowMapper);
        } catch (Exception e) {
            System.out.println("Query error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private static class FoodItemRowMapper implements RowMapper<FoodItem> {
        @Override
        public FoodItem mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            FoodItem item = new FoodItem();
            item.setId(rs.getLong("id"));
            item.setName(rs.getString("name"));
            item.setInfo(rs.getString("info"));
            return item;
        }
    }
}
