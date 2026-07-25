package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class CollectionItemDaoCustomImpl implements CollectionItemDaoCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<CollectionItem> searchByFilter(String collectionKey, String filterJson, int offset, int limit) {
        var sql = new StringBuilder("""
                SELECT * FROM collection_items
                WHERE collection_key = :collectionKey
                  AND is_archived = false
                """);
        if (filterJson != null) {
            sql.append(" AND data @> CAST(:filterJson AS jsonb)");
        }
        sql.append(" ORDER BY slug OFFSET :offset LIMIT :limit");

        var query = entityManager.createNativeQuery(sql.toString(), CollectionItem.class)
                .setParameter("collectionKey", collectionKey)
                .setParameter("offset", offset)
                .setParameter("limit", limit);
        if (filterJson != null) {
            query.setParameter("filterJson", filterJson);
        }
        return query.getResultList();
    }

    @Override
    public long countByFilter(String collectionKey, String filterJson) {
        var sql = new StringBuilder("""
                SELECT COUNT(*) FROM collection_items
                WHERE collection_key = :collectionKey
                  AND is_archived = false
                """);
        if (filterJson != null) {
            sql.append(" AND data @> CAST(:filterJson AS jsonb)");
        }

        var query = entityManager.createNativeQuery(sql.toString())
                .setParameter("collectionKey", collectionKey);
        if (filterJson != null) {
            query.setParameter("filterJson", filterJson);
        }
        return ((Number) query.getSingleResult()).longValue();
    }
}
