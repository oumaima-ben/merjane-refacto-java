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
