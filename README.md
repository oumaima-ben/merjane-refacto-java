# Merjane — Order processing refactor

Pulled the per-product business logic out of the controller, split it
by `ProductType` using a Strategy pattern, and added structured HTTP
error handling. The happy path stays the same; error responses are
proper status codes with a JSON body instead of stack traces.

## Layout

```
controllers/OrderController            HTTP layer only
services/OrderService(Impl)            @Transactional orchestrator
services/ProductTypeHandler            strategy interface
  └── handlers/                        one impl per ProductType + registry
exceptions/                            custom exception + ErrorResponse advice
```

## Trying the API manually

Run with the `dev` profile to seed sample products and an order with id `1`:

```
cd api
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Then either curl it:

```
curl -X POST http://localhost:8080/api/orders/1/processOrder
```

…or open [`api/requests.http`](api/requests.http) in IntelliJ / VS Code REST Client
to try the happy path and every error case in one click.

### What I got back

Ran every request from `requests.http` against the running app — each one
returned the expected status with a structured JSON body:

| Scenario                       | Method | Path                            | Status |
|--------------------------------|--------|---------------------------------|--------|
| Happy path (seeded order)      | POST   | `/api/orders/1/processOrder`    | 200    |
| Order not found                | POST   | `/api/orders/999/processOrder`  | 404    |
| Negative id (validation)       | POST   | `/api/orders/-1/processOrder`   | 400    |
| Non-numeric id (type mismatch) | POST   | `/api/orders/abc/processOrder`  | 400    |
| Wrong HTTP method              | GET    | `/api/orders/1/processOrder`    | 405    |
| Product availability check     | GET    | `/api/products/{id}/availability` | 200  |

Example response body for the type-mismatch case:

```json
{
  "timestamp": "2026-05-08T16:36:50.746768Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid value for parameter 'orderId': abc",
  "path": "/api/orders/abc/processOrder"
}
```

## Notes

- Classes marked `// WARN` were left untouched as required.
- `Product.type` kept as a string in the DB via `@Enumerated(EnumType.STRING)`.
- Tested with H2 + MockMvc for the controller, plain Mockito for the rest.

---

### Consignes

* Ignorez les migrations BDD
* Ne pas modifier les classes qui ont un commentaire: `// WARN: Should not be changed during the exercise`
* Pour lancer les tests (depuis le sous-répertoire `api`) :
  * unitaires: `mvnw test`
  * integration: `mvnw integration-test`
  * tous: `mvnw verify`
