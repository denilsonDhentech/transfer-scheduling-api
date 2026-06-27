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

### Listar agendamentos

```
GET /transfers
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

- **`domain`** — regra de cálculo da taxa (`FeeCalculator`) e exceção de domínio (`NoApplicableFeeException`). Sem dependência do Spring — testável em isolamento puro.
- **`application`** — caso de uso (`TransferService`) e DTOs. Orquestra domínio e repositório sem expor entidades JPA na API.
- **`infrastructure`** — entidade JPA (`TransferEntity`) e repositório Spring Data (`TransferRepository`).
- **`presentation`** — controller REST (`TransferController`) e handler global de exceções (`GlobalExceptionHandler`).

### Principais escolhas

- **`BigDecimal` para valores monetários** — evita imprecisão de ponto flutuante inerente a `double`/`float`.
- **Table-driven no `FeeCalculator`** — as faixas de taxa são declaradas como dados (`List<FeeRule>`), eliminando chain de `if` e tornando trivial adicionar ou remover faixas.
- **DTOs desacoplados das entidades** — a entidade JPA nunca é exposta diretamente na API, preservando o contrato REST independente do modelo de persistência.
- **H2 em memória** — banco volátil configurado com `create-drop`, suficiente para o escopo da avaliação e sem necessidade de infraestrutura externa.

### Cobertura de testes

| Camada | Quantidade | Tipo |
|---|---|---|
| Domínio | 13 | Unitário |
| Aplicação | 4 | Unitário (Mockito) |
| Apresentação | 7 | Integração (MockMvc + H2) |
| **Total** | **24** | |
