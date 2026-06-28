# Transfer Scheduling API

REST API para agendamento de transferências financeiras, desenvolvida como avaliação técnica.

## Tecnologias

| Item | Versão |
|---|---|
| Java | 11 |
| Spring Boot | 2.7.18 |
| Spring Data JPA | 2.7.18 |
| Spring Validation | 2.7.18 |
| H2 Database | em memória |
| springdoc-openapi-ui | 1.7.0 |
| JUnit 5 | 5.8.x |
| Mockito | 4.x |
| Maven | 3.x |

## Como executar

**Pré-requisitos:** Java 11 e Maven instalados.

```bash
# clonar o repositório
git clone https://github.com/denilsonDhentech/transfer-scheduling-api.git
cd transfer-scheduling-api

# compilar e subir a aplicação
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

### Documentação interativa (Swagger UI)

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

### Console do H2

O console do H2 pode ser acessado em `http://localhost:8080/h2-console` com as credenciais:
- **JDBC URL:** `jdbc:h2:mem:transferdb`
- **User:** `sa`
- **Password:** *(vazio)*

### Executar os testes

```bash
mvn test
```

## Endpoints

### Agendar transferência

```
POST /transfers
Content-Type: application/json
```

```json
{
  "sourceAccount": "1234567890",
  "destinationAccount": "0987654321",
  "amount": 1000.00,
  "transferDate": "2026-07-10"
}
```

Regras de validação:
- `sourceAccount` e `destinationAccount`: exatamente 10 dígitos
- `amount`: valor positivo
- `transferDate`: formato ISO 8601 (`yyyy-MM-dd`), deve estar entre hoje e 50 dias à frente

Retorna `201 Created` com o agendamento criado, incluindo `fee` calculada e `status: PENDING`.

### Simular taxa

```
POST /transfers/simulate
Content-Type: application/json
```

Mesmo body do agendamento. Calcula e retorna a taxa **sem persistir** nenhum dado.

```json
{
  "fee": 12.00,
  "days": 7
}
```

Retorna `400` se a data for inválida (passado ou acima de 50 dias).

### Listar agendamentos

```
GET /transfers
```

Retorna todos os agendamentos com o campo `status` derivado em tempo de resposta:

| Status | Condição |
|--------|----------|
| `PENDING` | `transferDate` ainda no futuro |
| `EXECUTED` | `transferDate <= hoje` |
| `CANCELLED` | cancelado via PATCH |

### Buscar agendamento por ID

```
GET /transfers/{id}
```

Retorna `200` com o agendamento ou `404` se não encontrado.

### Cancelar agendamento

```
PATCH /transfers/{id}/cancel
```

Sem body. Retorna `204 No Content` em caso de sucesso.

| Situação | Resposta |
|----------|----------|
| ID não existe | `404 Not Found` |
| Já cancelado | `422 Unprocessable Entity` |
| `transferDate <= hoje` | `422 Unprocessable Entity` |
| Válido | `204 No Content` |

### Exportar extrato em CSV

```
GET /transfers/export
```

Retorna `200` com o arquivo CSV para download.

- `Content-Type: text/csv; charset=UTF-8`
- `Content-Disposition: attachment; filename="agendamentos.csv"`

```csv
id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status
1,1234567890,0987654321,1000.00,12.00,2026-07-10,2026-06-28,PENDING
```

## Tabela de taxas

| Dias até a transferência | Taxa fixa (R$) | Taxa % |
|---|---|---|
| 0 (mesmo dia) | R$ 3,00 | 2,5% |
| 1 a 10 | R$ 12,00 | 0,0% |
| 11 a 20 | R$ 0,00 | 8,2% |
| 21 a 30 | R$ 0,00 | 6,9% |
| 31 a 40 | R$ 0,00 | 4,7% |
| 41 a 50 | R$ 0,00 | 1,7% |

Transferências com data no passado ou acima de 50 dias são rejeitadas com HTTP 400.

## Decisões arquiteturais

### Clean Architecture em camadas

O projeto é dividido em quatro camadas com dependências unidirecionais:

```
presentation → application → domain
                           ↘ infrastructure
```

- **`domain`** — regras de negócio (`FeeCalculator`, `TransferStatus`) e exceções de domínio (`NoApplicableFeeException`, `TransferNotFoundException`, `TransferCancellationException`). Sem dependência do Spring — testável em isolamento puro.
- **`application`** — casos de uso (`TransferService`: agendar, cancelar, simular, listar, buscar por ID) e DTOs. Orquestra domínio e repositório sem expor entidades JPA na API.
- **`infrastructure`** — entidade JPA (`TransferEntity`), repositório Spring Data (`TransferRepository`) e serialização (`TransferCsvExporter`).
- **`presentation`** — controller REST (`TransferController`) e handler global de exceções (`GlobalExceptionHandler`).

### Principais escolhas

- **`BigDecimal` para valores monetários** — evita imprecisão de ponto flutuante inerente a `double`/`float`.
- **Table-driven no `FeeCalculator`** — as faixas de taxa são declaradas como dados (`List<FeeRule>`), eliminando chain de `if` e tornando trivial adicionar ou remover faixas.
- **DTOs desacoplados das entidades** — a entidade JPA nunca é exposta diretamente na API, preservando o contrato REST independente do modelo de persistência.
- **`status` derivado em tempo de resposta** — `EXECUTED` não é persistido; é calculado no `TransferResponseDto` com base em `transferDate <= hoje`. Apenas `PENDING` e `CANCELLED` são gravados no banco, evitando a necessidade de job de atualização em background.
- **Cancelamento via PATCH, não DELETE** — segue o padrão de APIs financeiras (Stripe, Open Finance Brasil): o registro é preservado com `status = CANCELLED` para fins de auditoria. HTTP DELETE implicaria remoção do recurso.
- **`TransferCsvExporter` em infraestrutura** — serialização CSV extraída do `TransferService` por SRP. O service conhece apenas casos de uso; o exporter conhece apenas formato de saída.
- **H2 em memória** — banco volátil configurado com `create-drop`, suficiente para o escopo da avaliação e sem necessidade de infraestrutura externa.

### Cobertura de testes

| Camada | Quantidade | Tipo |
|---|---|---|
| Domínio (`FeeCalculatorTest`) | 13 | Unitário |
| Aplicação (`TransferServiceTest`) | 16 | Unitário (Mockito) |
| Infraestrutura (`TransferCsvExporterTest`) | 3 | Unitário |
| Apresentação (`TransferControllerTest`) | 19 | Integração (MockMvc + H2) |
| **Total** | **51** | |
