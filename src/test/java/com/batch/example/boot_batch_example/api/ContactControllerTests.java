package com.batch.example.boot_batch_example.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ContactControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearContacts() {
        jdbcTemplate.update("delete from person");
    }

    @Test
    void managesContactsAndFavorites() throws Exception {
        String request = """
                {
                  "firstName": "Test",
                  "lastName": "Contact",
                  "phone": "+1 555 0100",
                  "email": "test@example.com",
                  "company": "Example",
                  "jobTitle": "Engineer",
                  "notes": "Met at event"
                }
                """;
        MvcResult createResult = mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.favorite").value(false))
                .andReturn();
        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/contacts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("Test"));

        mockMvc.perform(get("/api/contacts").param("search", "event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].phone").value("+1 555 0100"));

        mockMvc.perform(patch("/api/contacts/{id}/favorite", id)
                        .contentType("application/json")
                        .content("{\"favorite\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(get("/api/contacts/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(put("/api/contacts/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Updated",
                                  "phone": "+1 555 0101",
                                  "notes": "Updated notes"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(delete("/api/contacts/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/contacts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidContactData() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content("{\"firstName\":\" \",\"phone\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsContactFieldsThatExceedTheirMaximumLengths() throws Exception {
        ContactRequest valid = new ContactRequest(
                "Test", "Last", "555-0100", "test@example.com", "Company", "Title", "Notes");
        ContactRequest[] oversizedRequests = {
                new ContactRequest("x".repeat(101), valid.lastName(), valid.phone(), valid.email(),
                        valid.company(), valid.jobTitle(), valid.notes()),
                new ContactRequest(valid.firstName(), "x".repeat(101), valid.phone(), valid.email(),
                        valid.company(), valid.jobTitle(), valid.notes()),
                new ContactRequest(valid.firstName(), valid.lastName(), "x".repeat(33), valid.email(),
                        valid.company(), valid.jobTitle(), valid.notes()),
                new ContactRequest(valid.firstName(), valid.lastName(), valid.phone(), "x".repeat(255),
                        valid.company(), valid.jobTitle(), valid.notes()),
                new ContactRequest(valid.firstName(), valid.lastName(), valid.phone(), valid.email(),
                        "x".repeat(201), valid.jobTitle(), valid.notes()),
                new ContactRequest(valid.firstName(), valid.lastName(), valid.phone(), valid.email(),
                        valid.company(), "x".repeat(201), valid.notes()),
                new ContactRequest(valid.firstName(), valid.lastName(), valid.phone(), valid.email(),
                        valid.company(), valid.jobTitle(), "x".repeat(4001))
        };

        for (ContactRequest request : oversizedRequests) {
            mockMvc.perform(post("/api/contacts")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void createsContactWithTrimmedValuesAndLocationHeader() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "  Test  ",
                                  "phone": " 555-0100 ",
                                  "lastName": " ",
                                  "company": " Example "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.phone").value("555-0100"))
                .andExpect(jsonPath("$.lastName").doesNotExist())
                .andExpect(jsonPath("$.company").value("Example"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.favorite").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getResponse().getHeader("Location"))
                        .startsWith("/api/contacts/"));
    }

    @Test
    void filtersSortsAndPaginatesContacts() throws Exception {
        createContact("Gamma", "paging-scope first note");
        createContact("Alpha", "paging-scope matching note");
        String betaId = createContact("Beta", "paging-scope another note");
        mockMvc.perform(patch("/api/contacts/{id}/favorite", betaId)
                        .contentType("application/json")
                        .content("{\"favorite\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contacts")
                        .param("search", "MATCHING")
                        .param("favorite", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Alpha"));

        mockMvc.perform(get("/api/contacts")
                        .param("search", "paging-scope")
                        .param("sortBy", "firstName")
                        .param("sortDirection", "desc")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Gamma"))
                .andExpect(jsonPath("$.content[1].firstName").value("Beta"));

        mockMvc.perform(get("/api/contacts")
                        .param("search", "paging-scope")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Gamma"));
    }

    @Test
    void searchesEverySupportedContactFieldCaseInsensitively() throws Exception {
        ContactRequest request = new ContactRequest(
                "SearchableFirst",
                "SearchableLast",
                "SearchablePhone",
                "searchable@example.com",
                "SearchableCompany",
                "SearchableJob",
                "SearchableNotes");
        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        for (String term : new String[] {
                "searchablefirst", "searchablelast", "searchablephone",
                "searchable@example.com", "searchablecompany", "searchablejob", "searchablenotes"
        }) {
            mockMvc.perform(get("/api/contacts").param("search", term))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Test
    void sortsByEachSupportedField() throws Exception {
        createContact("SortB", "sorting-scope");
        createContact("SortA", "sorting-scope");

        for (String field : new String[] {"firstName", "lastName", "createdAt", "updatedAt"}) {
            mockMvc.perform(get("/api/contacts")
                            .param("search", "sorting-scope")
                            .param("sortBy", field)
                            .param("sortDirection", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }
    }

    @Test
    void returnsEmptyPageWhenSearchHasNoMatches() throws Exception {
        mockMvc.perform(get("/api/contacts").param("search", "no-matching-contact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void treatsBlankSearchAsNoFilter() throws Exception {
        createContact("BlankSearch", "blank-search-scope");

        mockMvc.perform(get("/api/contacts").param("search", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("BlankSearch"));
    }

    @Test
    void rejectsInvalidEmailAndFavoriteRequest() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content("""
                                {"firstName":"Test","phone":"555-0100","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/contacts/{id}/favorite", "missing-id")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidListParameters() throws Exception {
        mockMvc.perform(get("/api/contacts").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contacts").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contacts").param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contacts").param("sortBy", "email"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/contacts").param("sortDirection", "sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForOperationsOnMissingContact() throws Exception {
        mockMvc.perform(get("/api/contacts/{id}", "missing-id"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/contacts/{id}", "missing-id")
                        .contentType("application/json")
                        .content("""
                                {"firstName":"Test","phone":"555-0100"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/contacts/{id}/favorite", "missing-id")
                        .contentType("application/json")
                        .content("{\"favorite\":true}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/contacts/{id}", "missing-id"))
                .andExpect(status().isNotFound());
    }

    private String createContact(String firstName, String notes) throws Exception {
        ContactRequest request = new ContactRequest(
                firstName, null, "555-0100", null, "Test Co", null, notes);
        MvcResult result = mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();
    }
}
