-- Tabla de estados de préstamo
CREATE TABLE loan_states (
    state_id SERIAL PRIMARY KEY,
    state_name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Tabla de tipos de préstamo
CREATE TABLE loan_types (
    loan_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    min_amount NUMERIC(15,2) NOT NULL,
    max_amount NUMERIC(15,2) NOT NULL,
    interest_rate NUMERIC(5,2) NOT NULL,
    automatic_validation BOOLEAN DEFAULT FALSE
);

-- Tabla de solicitudes de préstamo
CREATE TABLE loan_applications (
    application_id SERIAL PRIMARY KEY,
    amount NUMERIC(15,2) NOT NULL,
    term_months INT NOT NULL,
    email VARCHAR(150) NOT NULL,
    document VARCHAR(50) NOT NULL,
    state_id INT NOT NULL,
    loan_type_id INT NOT NULL,
    CONSTRAINT fk_state FOREIGN KEY (state_id) REFERENCES loan_states(state_id),
    CONSTRAINT fk_loan_type FOREIGN KEY (loan_type_id) REFERENCES loan_types(loan_type_id)
);


INSERT INTO loan_states (state_name, description) VALUES
('PENDING', 'Application submitted and awaiting review'),
('MANUAL_REVIEW', 'Application is being evaluated manually'),
('APPROVED', 'Application has been approved'),
('REJECTED', 'Application has been rejected'),
('CANCELLED', 'Application was cancelled by user');


INSERT INTO loan_types (type_name, min_amount, max_amount, interest_rate, automatic_validation) VALUES
('PERSONAL_LOAN', 1000.00, 50000.00, 12.50, TRUE),
('BUSINESS_LOAN', 5000.00, 200000.00, 9.75, TRUE),
('EMERGENCY_LOAN', 500.00, 5000.00, 15.00, FALSE),
('EDUCATION_LOAN', 1000.00, 30000.00, 8.25, FALSE);