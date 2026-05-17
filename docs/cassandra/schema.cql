CREATE KEYSPACE IF NOT EXISTS hospital
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE hospital;

CREATE TABLE IF NOT EXISTS users_by_email (
  email text PRIMARY KEY,
  user_id uuid,
  full_name text,
  password_hash text,
  role text
);

CREATE TABLE IF NOT EXISTS patient_appointments_by_patient (
  patient_id uuid,
  scheduled_at timestamp,
  appointment_id uuid,
  professional_id uuid,
  professional_name text,
  status text,
  notes text,
  created_at timestamp,
  updated_at timestamp,
  PRIMARY KEY ((patient_id), scheduled_at, appointment_id)
) WITH CLUSTERING ORDER BY (scheduled_at DESC, appointment_id ASC);

CREATE TABLE IF NOT EXISTS notifications (
  id uuid PRIMARY KEY,
  appointment_id uuid,
  recipient text,
  subject text,
  message text,
  channel text,
  status text,
  created_at timestamp,
  sent_at timestamp,
  failure_reason text
);

INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('medico@hospital.com', 00000000-0000-0000-0000-000000000701, 'Dra. Marina Costa', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'MEDICO');
INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('doctor@hospital.com', 00000000-0000-0000-0000-000000000701, 'Dra. Marina Costa', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'MEDICO');
INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('enfermeiro@hospital.com', 33333333-3333-3333-3333-333333333333, 'Enf. Paulo Ramos', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'ENFERMEIRO');
INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('nurse@hospital.com', 33333333-3333-3333-3333-333333333333, 'Enf. Paulo Ramos', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'ENFERMEIRO');
INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('paciente@hospital.com', 00000000-0000-0000-0000-000000000601, 'Paciente Exemplo', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'PACIENTE');
INSERT INTO users_by_email (email, user_id, full_name, password_hash, role)
VALUES ('patient@hospital.com', 00000000-0000-0000-0000-000000000601, 'Paciente Exemplo', '$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy', 'PACIENTE');
