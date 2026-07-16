-- Dati di esempio, inseriti solo se non gia' presenti (idempotente).
INSERT INTO play (id, available_tickets, date, time, description, city, location, name, price, version)
SELECT nextval('play_seq'), 10, '2026-12-07', '10:15', 'Spettacolo interessante', 'Roma', 'Via Merulana, 244, 00185', 'Macbeth', 20, 0
WHERE NOT EXISTS (SELECT 1 FROM play WHERE name = 'Macbeth');
