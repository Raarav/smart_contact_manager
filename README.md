# Smart Contact Manager

A Spring Boot REST API for storing and organizing contacts. Contact data is stored in a local, file-backed H2 database by default; the existing Spring Batch CSV import is available at `POST /api/import`.

## Run

```bash
bash mvnw spring-boot:run
```

The default H2 database is stored under `./data/contacts` and persists when the application stops. Override `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` to use a different H2 location or another JDBC database.

When the application is running, open the H2 console at `http://localhost:8080/h2-console` and connect with JDBC URL `jdbc:h2:file:./data/contacts;AUTO_SERVER=TRUE`, user `sa`, and a blank password. Replace the console's default `jdbc:h2:~/test` URL; otherwise, it opens a different database. `AUTO_SERVER=TRUE` allows other H2 processes to connect to the same file-backed database while the app is running. The table may appear as `PERSON` in the console because H2 uppercases unquoted table names.

## Tests and coverage

Run the full test suite and enforce 100% instruction, branch, and line coverage:

```bash
bash mvnw verify
```

JaCoCo writes the HTML report to `target/site/jacoco/index.html`.
In IntelliJ, run `FullCoverageSuite` with **Run with Coverage** to collect coverage for every test class. Running an individual test class (for example `BootBatchExampleApplicationTests`) reports coverage for only that class's test run.
To focus IntelliJ coverage on the API and model packages, run `ApiModelCoverageSuite` with **Run with Coverage**.

## Contact API

All endpoints are under `/api/contacts`. Create/update requests accept JSON:

```json
{
  "firstName": "Sample",
  "lastName": "One",
  "phone": "+1-202-555-0101",
  "email": "sample1@example.com",
  "company": "Example Inc.",
  "jobTitle": "Designer",
  "notes": "Met at the conference"
}
```

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/contacts` | Add a contact |
| `GET` | `/api/contacts` | List/search contacts |
| `GET` | `/api/contacts/{id}` | Get one contact |
| `PUT` | `/api/contacts/{id}` | Edit a contact |
| `PATCH` | `/api/contacts/{id}/favorite` | Set favorite, with `{"favorite": true}` |
| `GET` | `/api/contacts/favorites` | List favorite contacts |
| `DELETE` | `/api/contacts/{id}` | Delete a contact |

List queries support `search`, `favorite`, `page` (zero-based), `size` (1–100), `sortBy` (`firstName`, `lastName`, `createdAt`, or `updatedAt`), and `sortDirection` (`asc` or `desc`). Search checks names, phone, email, company, and notes. Phone numbers are returned in contact responses so clients can provide click-to-call links using `tel:` URLs.
