# Payments Matching Tool

A full-stack Payments Matching Tool for uploading System and Provider CSV files, matching payment records, viewing mismatches, and resolving each result by accepting either the System or Provider side.

## Tech Stack

- Frontend: Angular
- Backend: Spring Boot Java
- Database: MySQL
- API Docs: Swagger / OpenAPI

## Features

- Upload System CSV and Provider CSV files
- Match rows using `orderId + currency`
- Classify results as `MATCHED`, `ONLYSYSTEM`, `ONLYPROVIDER`, or `AMOUNTMISMATCH`
- Store match results in MySQL
- View results in Angular UI
- Filter by `UNRESOLVED`, `RESOLVED`, and `ALL`
- Resolve each row by choosing `Accept System` or `Accept Provider`

## CSV Format

Both files must be UTF-8 CSV files with a single header row:

```csv
orderId,amount,currency
ORD-1,100,INR
ORD-2,200,INR
ORD-3,150,USD
```

Supported currencies:

```text
USD, EUR, INR, GBP
```

## Sample Files

Sample CSV files are included:

```text
sample-data/system.csv
sample-data/provider.csv
```

Use these files to test the application quickly.

## Matching Rules

The matching key is:

```text
orderId + currency
```

| Condition | Status |
|---|---|
| Present in both files with same amount | `MATCHED` |
| Present only in System CSV | `ONLYSYSTEM` |
| Present only in Provider CSV | `ONLYPROVIDER` |
| Present in both files with different amount | `AMOUNTMISMATCH` |

## Database Setup

Create or use a local MySQL database.

Default backend configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/payment_matching_tool?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=toor
```

If your MySQL username or password is different, update:

```text
src/main/resources/application.properties
```

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

So the `match_results` table is created automatically.

## Run Backend

From the project root:

```powershell
cd C:\Users\user\IdeaProjects\payment-matching-tool
.\mvnw.cmd spring-boot:run
```

Backend runs on:

```text
http://localhost:8081
```

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

## Run Frontend

Open a new terminal and go to the Angular folder:

```powershell
cd C:\Users\user\IdeaProjects\payment-matching-tool\frontend
npm install
npm start
```

Frontend runs on:

```text
http://localhost:4200
```

## How To Use

1. Start MySQL.
2. Start the Spring Boot backend.
3. Start the Angular frontend.
4. Open `http://localhost:4200`.
5. Upload `sample-data/system.csv` in the System CSV field.
6. Upload `sample-data/provider.csv` in the Provider CSV field.
7. Click `Run Match`.
8. Review the summary and result table.
9. Use `Accept System` or `Accept Provider` to resolve rows.
10. Use the dropdown to filter by unresolved, resolved, or all records.

## API Endpoints

### Run Match

```http
POST /api/matches/run
```

Multipart form fields:

```text
systemFile
providerFile
```

### Get Results

```http
GET /api/matches?filter=UNRESOLVED
GET /api/matches?filter=RESOLVED
GET /api/matches?filter=ALL
```

### Resolve Match Result

```http
PATCH /api/matches/{id}/resolve
```

Request body:

```json
{
  "resolutionSide": "SYSTEM"
}
```

or:

```json
{
  "resolutionSide": "PROVIDER"
}
```

## DB Schema

Main table:

```text
match_results
```

Columns:

```text
id
order_id
system_amount
provider_amount
currency
status
resolved
resolution_side
created_at
```

## Assumptions

- CSV files are comma-separated and UTF-8 encoded.
- CSV files contain a single header row.
- Amount is a valid number.
- Currency is one of `USD`, `EUR`, `INR`, or `GBP`.
- Each `orderId + currency` combination is unique within each uploaded file.
- A new match run replaces the previous match results.
- File type validation is based on `.csv` extension.
- UI validates that the System field receives a file name containing `system` and the Provider field receives a file name containing `provider`.

