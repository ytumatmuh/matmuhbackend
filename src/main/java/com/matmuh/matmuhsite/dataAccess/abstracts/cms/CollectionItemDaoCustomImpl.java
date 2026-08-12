package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.core.helpers.CollectionSortParser.CollectionSort;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class CollectionItemDaoCustomImpl implements CollectionItemDaoCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<CollectionItem> searchByFilter(String collectionKey, String filterJson, CollectionSort sort,
                                               boolean archived, String locale, int offset, int limit) {
        var sql = new StringBuilder("""
                SELECT * FROM collection_items
                WHERE collection_key = :collectionKey
                  AND is_archived = :archived
                """);
        if (filterJson != null) {
            sql.append(" AND data @> CAST(:filterJson AS jsonb)");
        }
        sql.append(localeClause(locale));
        sql.append(orderBy(sort == null ? CollectionSort.DEFAULT : sort));
        sql.append(" OFFSET :offset LIMIT :limit");

        var query = entityManager.createNativeQuery(sql.toString(), CollectionItem.class)
                .setParameter("collectionKey", collectionKey)
                .setParameter("archived", archived)
                .setParameter("offset", offset)
                .setParameter("limit", limit);
        if (filterJson != null) {
            query.setParameter("filterJson", filterJson);
        }
        if (sort != null && sort.isDataField()) {
            query.setParameter("sortField", sort.dataField());
        }
        if (locale != null) {
            query.setParameter("locale", locale);
        }
        return query.getResultList();
    }

    @Override
    public long countByFilter(String collectionKey, String filterJson, boolean archived, String locale) {
        var sql = new StringBuilder("""
                SELECT COUNT(*) FROM collection_items
                WHERE collection_key = :collectionKey
                  AND is_archived = :archived
                """);
        if (filterJson != null) {
            sql.append(" AND data @> CAST(:filterJson AS jsonb)");
        }
        sql.append(localeClause(locale));

        var query = entityManager.createNativeQuery(sql.toString())
                .setParameter("collectionKey", collectionKey)
                .setParameter("archived", archived);
        if (filterJson != null) {
            query.setParameter("filterJson", filterJson);
        }
        if (locale != null) {
            query.setParameter("locale", locale);
        }
        return ((Number) query.getSingleResult()).longValue();
    }



    private String localeClause(String locale) {
        return locale == null ? "" : " AND locale = :locale";
    }

    private String orderBy(CollectionSort sort) {
        var direction = sort.descending() ? "DESC" : "ASC";
        if (sort.isDataField()) {
            return " ORDER BY jsonb_extract_path(data, :sortField) IS NULL,"
                    + " jsonb_extract_path(data, :sortField) " + direction + ", slug ASC";
        }
        if ("slug".equals(sort.column())) {
            return " ORDER BY slug " + direction;
        }
        return " ORDER BY " + sort.column() + " " + direction + ", slug ASC";
    }
}
