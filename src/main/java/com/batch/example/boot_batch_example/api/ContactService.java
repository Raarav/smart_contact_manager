package com.batch.example.boot_batch_example.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactService {
    private static final RowMapper<ContactResponse> CONTACT_ROW_MAPPER = ContactService::mapContact;

    private final JdbcTemplate jdbcTemplate;

    public ContactService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ContactResponse create(ContactRequest request) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                insert into person
                    (personId, firstName, lastName, contact, email, company, jobTitle, notes, favorite)
                values (?, ?, ?, ?, ?, ?, ?, ?, false)
                """,
                id,
                clean(request.firstName()),
                clean(request.lastName()),
                clean(request.phone()),
                clean(request.email()),
                clean(request.company()),
                clean(request.jobTitle()),
                clean(request.notes()));
        return get(id);
    }

    public ContactPage list(String search, Boolean favorite, int page, int size,
            String sortBy, String sortDirection) {
        StringBuilder where = new StringBuilder(" where 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append("""
                     and (lower(firstName) like ? or lower(lastName) like ?
                          or lower(contact) like ? or lower(email) like ?
                          or lower(company) like ? or lower(notes) like ?)
                    """);
            for (int i = 0; i < 6; i++) {
                parameters.add(pattern);
            }
        }
        if (favorite != null) {
            where.append(" and favorite = ?");
            parameters.add(favorite);
        }

        long totalElements = jdbcTemplate.queryForObject(
                "select count(*) from person" + where, Long.class, parameters.toArray());
        String sortColumn = switch (sortBy) {
            case "firstName" -> "firstName";
            case "lastName" -> "lastName";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort field");
        };
        String direction = switch (sortDirection.toLowerCase(Locale.ROOT)) {
            case "asc" -> "asc";
            case "desc" -> "desc";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort direction must be asc or desc");
        };

        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add((long) page * size);
        List<ContactResponse> contacts = jdbcTemplate.query(
                "select * from person" + where + " order by " + sortColumn + " " + direction
                        + " limit ? offset ?",
                CONTACT_ROW_MAPPER,
                pageParameters.toArray());
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
        return new ContactPage(contacts, page, size, totalElements, totalPages);
    }

    public ContactResponse get(String id) {
        List<ContactResponse> matches = jdbcTemplate.query(
                "select * from person where personId = ?",
                CONTACT_ROW_MAPPER,
                id);
        if (matches.isEmpty()) {
            throw notFound(id);
        }
        return matches.get(0);
    }

    public ContactResponse update(String id, ContactRequest request) {
        int updated = jdbcTemplate.update("""
                update person
                set firstName = ?, lastName = ?, contact = ?, email = ?, company = ?,
                    jobTitle = ?, notes = ?, updatedAt = current_timestamp
                where personId = ?
                """,
                clean(request.firstName()),
                clean(request.lastName()),
                clean(request.phone()),
                clean(request.email()),
                clean(request.company()),
                clean(request.jobTitle()),
                clean(request.notes()),
                id);
        if (updated == 0) {
            throw notFound(id);
        }
        return get(id);
    }

    public ContactResponse setFavorite(String id, boolean favorite) {
        int updated = jdbcTemplate.update(
                "update person set favorite = ?, updatedAt = current_timestamp where personId = ?",
                favorite,
                id);
        if (updated == 0) {
            throw notFound(id);
        }
        return get(id);
    }

    public void delete(String id) {
        if (jdbcTemplate.update("delete from person where personId = ?", id) == 0) {
            throw notFound(id);
        }
    }

    private static ContactResponse mapContact(ResultSet resultSet, int rowNumber) throws SQLException {
        return new ContactResponse(
                resultSet.getString("personId"),
                resultSet.getString("firstName"),
                resultSet.getString("lastName"),
                resultSet.getString("contact"),
                resultSet.getString("email"),
                resultSet.getString("company"),
                resultSet.getString("jobTitle"),
                resultSet.getString("notes"),
                resultSet.getBoolean("favorite"),
                resultSet.getTimestamp("createdAt").toLocalDateTime(),
                resultSet.getTimestamp("updatedAt").toLocalDateTime());
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + id);
    }
}
