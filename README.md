# Tech Challenge 3

Backend hospitalar simplificado desenvolvido para a fase 3 do Tech Challenge, com foco em Spring Boot, segurança, GraphQL, persistência em Cassandra e comunicação assíncrona com RabbitMQ.

Este README é o guia rápido para subir e validar o projeto. A explicação técnica completa para avaliação está em [docs/avaliacao-fase-3.md](docs/avaliacao-fase-3.md).

## Visão Geral

O projeto simula parte de um sistema hospitalar com dois serviços:

| Serviço | Porta | Responsabilidade |
| --- | ---: | --- |
| `appointment-service` | `8080` | Agendamento, edição, confirmação, cancelamento e histórico de consultas |
| `notification-service` | `8081` | Registro, listagem e envio de notificações, além do consumo dos eventos de consulta |

Também fazem parte do ambiente:

| Componente | Porta | Uso |
| --- | ---: | --- |
| Cassandra | `9042` | Persistência dos dados |
| RabbitMQ | `5672` | Mensageria AMQP entre os serviços |
| RabbitMQ Management | `15672` | Interface administrativa do RabbitMQ |

Fluxo principal:

```text
Cliente -> appointment-service -> Cassandra
                              -> RabbitMQ -> notification-service -> Cassandra/Gmail SMTP
```

Quando uma consulta é criada ou alterada, o `appointment-service` publica um evento no RabbitMQ. O `notification-service` consome esse evento e gera um lembrete para o paciente.

## Tecnologias

- Java 21
- Spring Boot 4.0.5
- Spring Security com Basic Auth
- Spring Web MVC
- Spring GraphQL
- Spring AMQP
- Cassandra
- RabbitMQ
- Docker Compose
- Gradle
- Postman

## Como Subir o Projeto

Pré-requisitos:

- Docker
- Docker Compose
- Java 21, caso queira rodar testes fora do Docker

Suba o ambiente completo:

```bash
cp .env.example .env
docker compose up -d --build
```

Antes de subir, confira o arquivo `.env`. Ele já vem com valores adequados para executar tudo via Docker Compose. Na maioria dos casos, basta copiar o `.env.example` sem alterar nada.

Variáveis principais:

| Variável | Valor recomendado/local | Descrição |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `docker` | Ativa as configurações de Cassandra e RabbitMQ usadas no Docker |
| `SPRING_CASSANDRA_CONTACT_POINTS` | `cassandra` | Nome do serviço Cassandra dentro da rede Docker |
| `SPRING_CASSANDRA_PORT` | `9042` | Porta usada pelo Cassandra |
| `SPRING_CASSANDRA_KEYSPACE` | `hospital` | Keyspace usado pela aplicação |
| `SPRING_CASSANDRA_LOCAL_DATACENTER` | `dc1` | Datacenter configurado no Cassandra |
| `SPRING_RABBITMQ_HOST` | `rabbitmq` | Nome do serviço RabbitMQ dentro da rede Docker |
| `SPRING_RABBITMQ_PORT` | `5672` | Porta AMQP usada pelos serviços para falar com o RabbitMQ |
| `APPOINTMENTS_QUEUE` | `appointments.queue` | Fila consumida pelo `notification-service` |
| `APPOINTMENTS_EXCHANGE` | `appointments.exchange` | Exchange onde o `appointment-service` publica eventos |

Variáveis opcionais para envio real de e-mail:

| Variável | Exemplo | Descrição |
| --- | --- | --- |
| `GMAIL_USERNAME` | `sua-conta@gmail.com` | Conta Gmail usada para enviar e-mails |
| `GMAIL_APP_PASSWORD` | `senha-de-app-do-google` | Senha de app gerada no Google |
| `GMAIL_FROM` | `sua-conta@gmail.com` | Remetente exibido no e-mail |
| `PATIENT_REMINDER_FALLBACK_RECIPIENT` | `paciente-real@example.com` | Destinatário usado quando o paciente não tiver e-mail resolvido |

Se as variáveis do Gmail ficarem vazias, o projeto ainda sobe normalmente. O `notification-service` cria a notificação, mas marca o envio como `FAILED`, informando que o SMTP não foi configurado.

Verifique os containers:

```bash
docker compose ps
```

Healthchecks:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Painel do RabbitMQ:

```text
http://localhost:15672
usuario: guest
senha: guest
```

Para acompanhar logs:

```bash
docker compose logs -f appointment-service
docker compose logs -f notification-service
docker compose logs -f rabbitmq
docker compose logs -f cassandra
```

Para parar:

```bash
docker compose down
```

Para parar e remover os dados persistidos do Cassandra:

```bash
docker compose down -v
```

## Credenciais de Teste

Todos os usuários seedados usam a senha `123456`.

| Usuário | Papel |
| --- | --- |
| `medico@hospital.com` | Médico |
| `doctor@hospital.com` | Médico |
| `enfermeiro@hospital.com` | Enfermeiro |
| `nurse@hospital.com` | Enfermeiro |
| `paciente@hospital.com` | Paciente |
| `patient@hospital.com` | Paciente |

Paciente seedado usado nos testes:

```text
00000000-0000-0000-0000-000000000601
```

Profissional seedado usado nos testes:

```text
00000000-0000-0000-0000-000000000701
```

## Teste Rápido

Criar uma consulta:

```bash
curl -u medico@hospital.com:123456 \
  -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "00000000-0000-0000-0000-000000000601",
    "professionalId": "00000000-0000-0000-0000-000000000701",
    "professionalName": "Dra. Marina Costa",
    "scheduledAt": "2031-04-14T10:00:00Z",
    "status": "SCHEDULED",
    "notes": "Consulta criada para validação da fase 3"
  }'
```

Listar histórico do paciente:

```bash
curl -u paciente@hospital.com:123456 \
  http://localhost:8080/api/appointments/patients/00000000-0000-0000-0000-000000000601
```

Listar notificações geradas:

```bash
curl -u medico@hospital.com:123456 \
  http://localhost:8081/notifications
```

Se o Gmail SMTP não estiver configurado, a notificação será criada, mas o envio ficará como `FAILED` com o motivo registrado. Isso permite avaliar o fluxo assíncrono mesmo sem credenciais reais de e-mail.

## APIs Principais

### appointment-service

Base local:

```text
http://localhost:8080
```

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/appointments` | Cria consulta |
| `PUT` | `/api/appointments/{appointmentId}` | Atualiza consulta |
| `GET` | `/api/appointments/{appointmentId}` | Busca consulta por ID |
| `GET` | `/api/appointments` | Lista consultas |
| `GET` | `/api/appointments/patients/{patientId}` | Lista histórico do paciente |
| `GET` | `/api/appointments/patients/{patientId}/upcoming` | Lista próximas consultas do paciente |
| `POST` | `/api/appointments/{appointmentId}/confirm` | Confirma consulta |
| `POST` | `/api/appointments/{appointmentId}/cancel` | Cancela consulta |

### notification-service

Base local:

```text
http://localhost:8081
```

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/notifications` | Cria notificação manual |
| `GET` | `/notifications/{notificationId}` | Busca notificação |
| `GET` | `/notifications` | Lista notificações |
| `POST` | `/notifications/{notificationId}/send` | Envia notificação |

## GraphQL

Os dois serviços expõem `/graphql`.

No `appointment-service`, as principais operações são:

| Operação | Descrição |
| --- | --- |
| `patientHistory(patientId)` | Histórico do paciente |
| `futureAppointments(patientId)` | Consultas futuras |
| `scheduleAppointment(input)` | Agenda consulta |
| `updateAppointment(id, input)` | Atualiza consulta |
| `confirmAppointment(id)` | Confirma consulta |
| `cancelAppointment(id)` | Cancela consulta |

O contrato documental do GraphQL de consultas está em [docs/graphql/appointment-service.graphql](docs/graphql/appointment-service.graphql).

## Postman

A collection e o environment ficam em:

- [postman/Tech-Challenge.postman_collection.json](postman/Tech-Challenge.postman_collection.json)
- [postman/Tech-Challenge.postman_environment.json](postman/Tech-Challenge.postman_environment.json)

Como usar:

1. Importe os dois arquivos no Postman.
2. Selecione o environment `Tech Challenge - Local`.
3. Execute a collection `Tech Challenge - Collection Unica`.

A collection cobre healthchecks, autenticação, permissões por perfil, REST, GraphQL, erros HTTP e notification-service.

## Testes Automatizados

Rodar os testes do `appointment-service`:

```bash
cd appointment-service
./gradlew test --no-daemon
```

Rodar os testes do `notification-service`:

```bash
cd notification-service
./gradlew test --no-daemon
```

## Documentação de Avaliação

Para a correção da atividade, consulte:

- [docs/avaliacao-fase-3.md](docs/avaliacao-fase-3.md): explicação da arquitetura, requisitos atendidos, segurança, GraphQL, RabbitMQ, qualidade de código, testes, limitações e considerações finais.
- [docs/cassandra/schema.cql](docs/cassandra/schema.cql): schema Cassandra e usuários seedados.
- [docs/graphql/appointment-service.graphql](docs/graphql/appointment-service.graphql): contrato GraphQL documental do serviço de consultas.
