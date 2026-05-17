# Documento de Avaliação - Tech Challenge Fase 3
# Aluno: João Victor Torres Araújo - rm369354
# GitHub : https://github.com/torresvictor100/tech-challenge-3

Este documento complementa o [README principal](../README.md) e organiza a entrega conforme os requisitos e fatores de avaliação da fase 3.

O objetivo aqui é facilitar a análise do professor, mostrando o que foi implementado, como cada requisito foi atendido, quais decisões técnicas foram tomadas e quais pontos ficaram como evolução natural do projeto.

## 1. Contexto da Solução

O problema proposto pede um backend hospitalar simplificado, seguro e modular, capaz de:

- controlar o acesso de médicos, enfermeiros e pacientes;
- registrar e consultar agendamentos;
- expor histórico de consultas por GraphQL;
- separar responsabilidades em mais de um serviço;
- usar comunicação assíncrona entre serviços com RabbitMQ ou Kafka;
- permitir que o serviço de notificações processe eventos de consultas criadas ou editadas.

Para atender a esse cenário, o projeto foi dividido em dois microserviços Spring Boot:

| Serviço | Responsabilidade |
| --- | --- |
| `appointment-service` | Gerencia consultas, histórico do paciente, regras de agendamento, segurança de acesso e publicação de eventos |
| `notification-service` | Gerencia notificações, consome eventos de consulta e tenta enviar lembretes aos pacientes |

A persistência usa Cassandra e a comunicação assíncrona usa RabbitMQ.

## 2. Arquitetura Geral

Visão simplificada:

```text
Cliente REST/GraphQL
        |
        v
appointment-service
        |
        +--> Cassandra
        |
        +--> RabbitMQ exchange appointments.exchange
                         |
                         v
                  appointments.queue
                         |
                         v
                notification-service
                         |
                         +--> Cassandra
                         +--> Gmail SMTP quando configurado
```

O `appointment-service` não chama diretamente o `notification-service`. Ele publica um evento no RabbitMQ. O `notification-service` mantém uma conexão com o RabbitMQ, escuta a fila `appointments.queue` e processa as mensagens recebidas.

Essa escolha reduz acoplamento entre os serviços e evita que o agendamento dependa de uma chamada HTTP síncrona para notificação.

## 3. Atendimento aos Requisitos do Sistema

### 3.1 Segurança em Aplicações Java

O projeto implementa autenticação HTTP Basic com Spring Security nos dois serviços.

No `appointment-service`, os usuários são carregados pela porta `UserRepositoryPort`. No profile `docker`, a implementação usa a tabela Cassandra `users_by_email`. No profile padrão, a aplicação pode trabalhar com adapters locais para facilitar desenvolvimento e testes.

No `notification-service`, os usuários seedados ficam em memória porque o serviço também expõe endpoints HTTP próprios. Assim, a porta `8081` não fica aberta sem controle de acesso.

Usuários disponíveis:

| Usuário | Senha | Papel |
| --- | --- | --- |
| `medico@hospital.com` | `123456` | Médico |
| `doctor@hospital.com` | `123456` | Médico |
| `enfermeiro@hospital.com` | `123456` | Enfermeiro |
| `nurse@hospital.com` | `123456` | Enfermeiro |
| `paciente@hospital.com` | `123456` | Paciente |
| `patient@hospital.com` | `123456` | Paciente |

Controle de acesso implementado:

| Perfil | Permissões implementadas |
| --- | --- |
| Médico | Cria, altera, confirma, cancela e consulta agendamentos; acessa histórico e notificações |
| Enfermeiro | Cria, altera, confirma, cancela e consulta agendamentos; acessa histórico e notificações |
| Paciente | Consulta apenas suas próprias consultas e seu próprio histórico |

As permissões foram aplicadas com `@PreAuthorize` nos controllers REST e GraphQL do `appointment-service`. Para pacientes, existe validação por `PatientAccessEvaluator`, garantindo que o paciente autenticado não consulte dados de outro paciente.

Endpoints públicos:

| Endpoint | Motivo |
| --- | --- |
| `/actuator/health` | Healthcheck dos containers |
| `/actuator/info` | Informações operacionais |
| `/internal/ping` | Verificação simples de disponibilidade |

Demais endpoints exigem autenticação.

### 3.2 Consultas e Histórico com GraphQL

O `appointment-service` expõe GraphQL em `/graphql`.

Principais queries:

| Query | Finalidade |
| --- | --- |
| `appointments` | Lista consultas, disponível para médico e enfermeiro |
| `appointmentById(id)` | Busca consulta por ID |
| `patientAppointments(patientId)` | Lista consultas de um paciente |
| `upcomingPatientAppointments(patientId)` | Lista próximas consultas de um paciente |
| `patientHistory(patientId)` | Retorna o histórico do paciente |
| `futureAppointments(patientId)` | Retorna apenas consultas futuras |

Principais mutations:

| Mutation | Finalidade |
| --- | --- |
| `scheduleAppointment(input)` | Agenda nova consulta |
| `updateAppointment(id, input)` | Edita consulta existente |
| `confirmAppointment(id)` | Confirma consulta |
| `cancelAppointment(id)` | Cancela consulta |

O contrato documental está em [graphql/appointment-service.graphql](graphql/appointment-service.graphql).

O requisito de histórico foi implementado dentro do `appointment-service`. O enunciado trata um serviço de histórico como opcional; por isso, o projeto manteve o histórico acoplado ao serviço de agendamento, com consulta otimizada em Cassandra por `patient_id`.

### 3.3 Serviço de Agendamento

O `appointment-service` implementa:

- criação de consulta;
- edição de consulta;
- busca por ID;
- listagem geral;
- histórico por paciente;
- próximas consultas por paciente;
- confirmação;
- cancelamento;
- publicação de evento após criação ou alteração.

Endpoints REST principais:

| Método | Endpoint | Uso |
| --- | --- | --- |
| `POST` | `/api/appointments` | Criar consulta |
| `PUT` | `/api/appointments/{appointmentId}` | Atualizar consulta |
| `GET` | `/api/appointments/{appointmentId}` | Buscar consulta |
| `GET` | `/api/appointments` | Listar consultas |
| `GET` | `/api/appointments/patients/{patientId}` | Histórico do paciente |
| `GET` | `/api/appointments/patients/{patientId}/upcoming` | Próximas consultas |
| `POST` | `/api/appointments/{appointmentId}/confirm` | Confirmar consulta |
| `POST` | `/api/appointments/{appointmentId}/cancel` | Cancelar consulta |

Regras de domínio implementadas:

- consulta deve ter paciente, profissional, data, status e datas de controle;
- consulta deve ser agendada para data futura;
- consulta confirmada deve respeitar transições válidas;
- consulta cancelada não pode ser cancelada novamente;
- alteração relevante atualiza `updatedAt`;
- conflito de horário do profissional retorna erro de negócio.

### 3.4 Separação em Mais de um Serviço

A entrega possui dois serviços independentes, cada um com seu próprio projeto Gradle, Dockerfile, configurações, controllers, domínio e casos de uso.

| Serviço | Projeto | Banco | Comunicação |
| --- | --- | --- | --- |
| Agendamento | `appointment-service` | Cassandra | Publica eventos RabbitMQ |
| Notificações | `notification-service` | Cassandra | Consome eventos RabbitMQ |

Essa divisão permite que o serviço de notificações evolua sem alterar diretamente o serviço de consultas.

### 3.5 Comunicação Assíncrona com RabbitMQ

Foi escolhido RabbitMQ.

Configuração principal:

| Item | Valor |
| --- | --- |
| Exchange | `appointments.exchange` |
| Fila | `appointments.queue` |
| Binding | `appointments.*` |
| Routing key de criação | `appointments.created` |
| Routing key de atualização | `appointments.updated` |

Quando uma consulta é criada, o `appointment-service` publica um evento `APPOINTMENT_CREATED`. Quando uma consulta é atualizada, confirmada ou cancelada, publica evento de atualização.

Exemplo de payload:

```json
{
  "eventType": "APPOINTMENT_CREATED",
  "eventId": "uuid-do-evento",
  "appointmentId": "uuid-da-consulta",
  "patientId": "uuid-do-paciente",
  "scheduledAt": "2031-04-14T10:00:00Z",
  "status": "SCHEDULED"
}
```

O `notification-service` consome a fila com `@RabbitListener`, converte o JSON para `AppointmentEvent` e chama o `ReminderService`.

Fluxo implementado:

```text
Consulta criada ou editada
        |
        v
Evento publicado no RabbitMQ
        |
        v
notification-service consome a mensagem
        |
        v
ReminderService cria notificação EMAIL
        |
        v
Envio é tentado via Gmail SMTP quando configurado
```

## 4. Persistência com Cassandra

O Cassandra foi usado no profile `docker`.

Principais tabelas:

| Tabela | Responsabilidade |
| --- | --- |
| `users_by_email` | Usuários seedados para autenticação |
| `patient_appointments_by_patient` | Consultas organizadas por paciente e data |
| `notifications` | Notificações criadas manualmente ou por evento RabbitMQ |

A tabela de consultas foi modelada a partir do acesso principal do sistema: histórico de consultas por paciente.

Chave da tabela:

```sql
PRIMARY KEY ((patient_id), scheduled_at, appointment_id)
```

Essa modelagem permite buscar as consultas de um paciente de forma direta, sem depender de junções relacionais.

O schema completo está em [cassandra/schema.cql](cassandra/schema.cql).

## 5. Qualidade do Código

O código foi organizado com separação entre domínio, aplicação, portas e adaptadores.

Estrutura lógica:

| Camada | Responsabilidade |
| --- | --- |
| Domain | Entidades, estados e regras centrais |
| Application | Casos de uso e orquestração |
| Ports | Contratos de entrada e saída |
| REST/GraphQL/RabbitMQ | Adaptadores de entrada |
| Cassandra/RabbitMQ/Gmail | Adaptadores de saída |

Exemplos:

| Conceito | Implementação |
| --- | --- |
| Entidade de consulta | `Appointment` |
| Entidade de notificação | `Notification` |
| Caso de uso de consultas | `AppointmentUseCaseService` |
| Caso de uso de notificações | `NotificationUseCaseService` |
| Porta de repositório | `AppointmentRepositoryPort`, `NotificationRepositoryPort` |
| Publicação de eventos | `AppointmentEventPublisherPort` |
| Envio de notificação | `NotificationSenderPort` |

Pontos de qualidade aplicados:

- controllers sem regra de negócio pesada;
- regras centrais no domínio;
- validação de payload com Bean Validation no serviço de consultas;
- tratamento padronizado de erros HTTP;
- autenticação e autorização separadas da regra de negócio;
- adapters substituíveis por profile;
- testes de unidade para domínio, aplicação, segurança e mensageria;
- nomes de classes alinhados às responsabilidades.

## 6. Funcionalidades Entregues

### Agendamento

Entregue:

- criar consulta;
- editar consulta;
- confirmar consulta;
- cancelar consulta;
- listar consultas;
- buscar consulta por ID;
- consultar histórico por paciente;
- consultar consultas futuras.

### Histórico

Entregue por REST e GraphQL.

O histórico usa a tabela `patient_appointments_by_patient`, orientada ao acesso por paciente.

### Notificações

Entregue:

- criação manual de notificação;
- listagem;
- busca por ID;
- envio manual;
- criação automática de lembrete a partir de evento RabbitMQ;
- envio por Gmail SMTP quando configurado;
- falha controlada quando SMTP não está configurado.

### Segurança

Entregue:

- Basic Auth;
- usuários seedados;
- perfis médico, enfermeiro e paciente;
- restrição de paciente ao próprio histórico;
- proteção dos endpoints de notificação.

## 7. Collections para Teste

Foram incluídos arquivos Postman:

| Arquivo | Descrição |
| --- | --- |
| [../postman/Tech-Challenge.postman_collection.json](../postman/Tech-Challenge.postman_collection.json) | Collection com cenários REST, GraphQL, autenticação, permissões e notificações |
| [../postman/Tech-Challenge.postman_environment.json](../postman/Tech-Challenge.postman_environment.json) | Environment local com variáveis usadas pela collection |

Como validar:

1. Subir o ambiente com `docker compose up -d --build`.
2. Importar a collection e o environment no Postman.
3. Selecionar o environment `Tech Challenge - Local`.
4. Rodar a collection na ordem original.

A collection salva alguns IDs durante a execução, então a ordem dos requests deve ser mantida.

## 8. Testes Automatizados

O projeto possui testes nos dois serviços.

`appointment-service`:

| Teste | O que valida |
| --- | --- |
| `AppointmentServiceApplicationTests` | Contexto Spring |
| `AppointmentUseCaseServiceTest` | Agendamento, atualização, histórico e publicação de evento |
| `InMemoryAppointmentRepositoryAdapterTest` | Ordenação de histórico e filtro de futuras consultas |
| `PatientAppointmentRowTest` | Conversão entre domínio e Cassandra |
| `RabbitMqAppointmentEventPublisherAdapterTest` | Serialização e publicação de evento |
| `PatientAppointmentRestControllerTest` | Permissão do paciente sobre o próprio histórico |
| `AppointmentGraphQlControllerTest` | Operações GraphQL do serviço de consultas |

`notification-service`:

| Teste | O que valida |
| --- | --- |
| `NotificationServiceApplicationTests` | Contexto Spring |
| `NotificationUseCaseServiceTest` | Criação, listagem, busca e envio de notificação |
| `ReminderServiceTest` | Processamento de evento e criação de lembrete |
| `GmailNotificationSenderAdapterTest` | Comportamento do sender de e-mail |
| `AppointmentNotificationRabbitMqAdapterTest` | Consumo e parsing de mensagem RabbitMQ |
| `NotificationSecurityTest` | Regras de autenticação/autorização no serviço de notificações |

Comandos:

```bash
cd appointment-service
./gradlew test --no-daemon
```

```bash
cd notification-service
./gradlew test --no-daemon
```

## 9. Como o Professor Pode Validar Rapidamente

Subir o projeto:

```bash
cp .env.example .env
docker compose up -d --build
```

Validar saúde dos serviços:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Criar consulta:

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
    "notes": "Consulta para validação"
  }'
```

Consultar histórico como paciente:

```bash
curl -u paciente@hospital.com:123456 \
  http://localhost:8080/api/appointments/patients/00000000-0000-0000-0000-000000000601
```

Consultar notificações como médico:

```bash
curl -u medico@hospital.com:123456 \
  http://localhost:8081/notifications
```

Validar RabbitMQ:

```text
http://localhost:15672
usuario: guest
senha: guest
```

No painel do RabbitMQ, a fila `appointments.queue` deve aparecer quando os serviços estiverem em execução com o profile `docker`.

## 10. Trade-offs e Limitações

Algumas decisões foram tomadas para manter o escopo adequado a uma atividade de pós-graduação:

| Ponto | Decisão atual | Possível evolução |
| --- | --- | --- |
| Autenticação | Basic Auth com usuários seedados | JWT, OAuth2 ou integração com provedor de identidade |
| Serviço de histórico | Histórico dentro do `appointment-service` | Serviço independente caso o domínio cresça |
| Entrega de eventos | Publicação direta no RabbitMQ | Outbox transacional para maior garantia em falhas intermediárias |
| E-mail do paciente | Resolver local simplificado com fallback configurável | Cadastro real de pacientes e contatos |
| Migração Cassandra | Schema criado automaticamente/adaptado ao compose | Migrações versionadas com ferramenta própria para Cassandra |
| Observabilidade | Actuator health/info/prometheus | Tracing distribuído e dashboards |

Essas limitações não impedem a validação dos requisitos centrais da fase 3. Elas indicam caminhos naturais para endurecer a solução em um cenário produtivo.

## 11. Considerações Finais

O projeto entrega uma base funcional para o problema proposto: um backend hospitalar com controle de acesso, consultas, histórico, GraphQL, separação em serviços e comunicação assíncrona.

Para a fase 3, a solução demonstra os pontos principais esperados:

- uso de Spring Security para autenticação e autorização;
- exposição de operações REST e GraphQL;
- organização modular do código;
- persistência em Cassandra;
- comunicação assíncrona com RabbitMQ;
- serviço de notificações reagindo a eventos de agendamento;
- collection Postman para validação manual;
- testes automatizados cobrindo partes relevantes do fluxo.

A implementação não tenta representar um sistema hospitalar completo de produção. Ela foca no recorte pedido pela atividade e deixa explícitas as escolhas feitas para manter o escopo controlado. Ainda assim, a estrutura adotada permite evolução para autenticação mais robusta, serviço de histórico dedicado, eventos versionados, outbox transacional e cadastro real de pacientes.

Assim, a entrega atende ao objetivo acadêmico da fase: demonstrar uma solução backend modular, segura e integrada por mensageria assíncrona, com documentação suficiente para execução, avaliação e manutenção.
