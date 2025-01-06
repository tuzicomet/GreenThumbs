-- Insert Unverified User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Unverified', 'User', '01/01/1925', 'unverifieduser@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (1, 'ROLE_USER');

-- Insert Verified User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Verified', 'User', '01/01/1925', 'verifieduser@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (2, 'ROLE_USER_VERIFIED');

-- Insert User with Garden (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Garden', 'User', '01/01/1925', 'userwithgarden@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (3, 'ROLE_USER_VERIFIED');

-- Insert Search User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Search', 'User', '01/01/1925', 'searchuser@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (4, 'ROLE_USER_VERIFIED');

-- Insert Pending Request User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Pending', 'Request User', '01/01/1925', 'pending@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (5, 'ROLE_USER_VERIFIED');

-- Insert Declined Request User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Declined', 'Request User', '01/01/1925', 'declined@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (6, 'ROLE_USER_VERIFIED');

-- Insert Requesting User (User Type: USER)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type)
VALUES ('Requesting', 'User', '01/01/1925', 'requesting@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'USER');
INSERT INTO authority (user_id, role) VALUES (7, 'ROLE_USER_VERIFIED');


INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('123 Main St', 'Apt 4', 'Auckland', 'New Zealand', '123 Main St, Apt 4, Auckland, New Zealand', '123', -36.8485, 174.7633, '1010', 'Auckland', 'Main St', 'Central');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('456 Queen St', NULL, 'Wellington', 'New Zealand', '456 Queen St, Wellington, New Zealand', '456', -41.2865, 174.7762, '6011', 'Wellington', 'Queen St', 'CBD');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('789 King St', NULL, 'Christchurch', 'New Zealand', '789 King St, Christchurch, New Zealand', '789', -43.5321, 172.6362, '8011', 'Canterbury', 'King St', 'Central');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('100 Example St', NULL, 'City 1', 'Country 1', '100 Example St, City 1, Country 1', '100', -36.8485, 174.7633, '1011', 'State 1', 'Example St', 'Suburb 1');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('101 Example St', NULL, 'City 2', 'Country 2', '101 Example St, City 2, Country 2', '101', -36.8486, 174.7634, '1012', 'State 2', 'Example St', 'Suburb 2');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('102 Example St', NULL, 'City 3', 'Country 3', '102 Example St, City 3, Country 3', '102', -36.8487, 174.7635, '1013', 'State 3', 'Example St', 'Suburb 3');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('103 Example St', NULL, 'City 4', 'Country 4', '103 Example St, City 4, Country 4', '103', -36.8488, 174.7636, '1014', 'State 4', 'Example St', 'Suburb 4');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('104 Example St', NULL, 'City 5', 'Country 5', '104 Example St, City 5, Country 5', '104', -36.8489, 174.7637, '1015', 'State 5', 'Example St', 'Suburb 5');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('105 Example St', NULL, 'City 6', 'Country 6', '105 Example St, City 6, Country 6', '105', -36.8490, 174.7638, '1016', 'State 6', 'Example St', 'Suburb 6');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('106 Example St', NULL, 'City 7', 'Country 7', '106 Example St, City 7, Country 7', '106', -36.8491, 174.7639, '1017', 'State 7', 'Example St', 'Suburb 7');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('107 Example St', NULL, 'City 8', 'Country 8', '107 Example St, City 8, Country 8', '107', -36.8492, 174.7640, '1018', 'State 8', 'Example St', 'Suburb 8');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon, postcode, state, street, suburb)
VALUES ('108 Example St', NULL, 'City 9', 'Country 9', '108 Example St, City 9, Country 9', '108', -36.8493, 174.7641, '1019', 'State 9', 'Example St', 'Suburb 9');
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 13', 'Country 13', 'N/A, City 13, Country 13', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 14', 'Country 14', 'N/A, City 14, Country 14', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 15', 'Country 15', 'N/A, City 15, Country 15', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 16', 'Country 16', 'N/A, City 16, Country 16', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 17', 'Country 17', 'N/A, City 17, Country 17', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 18', 'Country 18', 'N/A, City 18, Country 18', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 19', 'Country 19', 'N/A, City 19, Country 19', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 20', 'Country 20', 'N/A, City 20, Country 20', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 21', 'Country 21', 'N/A, City 21, Country 21', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 22', 'Country 22', 'N/A, City 22, Country 22', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 23', 'Country 23', 'N/A, City 23, Country 23', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 24', 'Country 24', 'N/A, City 24, Country 24', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 25', 'Country 25', 'N/A, City 25, Country 25', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 26', 'Country 26', 'N/A, City 26, Country 26', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 27', 'Country 27', 'N/A, City 27, Country 27', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 28', 'Country 28', 'N/A, City 28, Country 28', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 29', 'Country 29', 'N/A, City 29, Country 29', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 30', 'Country 30', 'N/A, City 30, Country 30', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 31', 'Country 31', 'N/A, City 31, Country 31', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 32', 'Country 32', 'N/A, City 32, Country 32', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 33', 'Country 33', 'N/A, City 33, Country 33', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 34', 'Country 34', 'N/A, City 34, Country 34', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 35', 'Country 35', 'N/A, City 35, Country 35', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 36', 'Country 36', 'N/A, City 36, Country 36', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 37', 'Country 37', 'N/A, City 37, Country 37', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 38', 'Country 38', 'N/A, City 38, Country 38', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 39', 'Country 39', 'N/A, City 39, Country 39', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 40', 'Country 40', 'N/A, City 40, Country 40', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 41', 'Country 41', 'N/A, City 41, Country 41', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 42', 'Country 42', 'N/A, City 42, Country 42', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 43', 'Country 43', 'N/A, City 43, Country 43', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 44', 'Country 44', 'N/A, City 44, Country 44', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 45', 'Country 45', 'N/A, City 45, Country 45', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 46', 'Country 46', 'N/A, City 46, Country 46', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 47', 'Country 47', 'N/A, City 47, Country 47', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 48', 'Country 48', 'N/A, City 48, Country 48', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 49', 'Country 49', 'N/A, City 49, Country 49', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 50', 'Country 50', 'N/A, City 50, Country 50', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 51', 'Country 51', 'N/A, City 51, Country 51', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 52', 'Country 52', 'N/A, City 52, Country 52', 'N/A', 0.0, 0.0);
INSERT INTO location (address_line1, address_line2, city, country, formatted, house_number, lat, lon)
VALUES ('N/A', NULL, 'City 53', 'Country 53', 'N/A, City 53, Country 53', 'N/A', 0.0, 0.0);

-- Insert Contractor (User Type: CONTRACTOR)
INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id, num_ratings, rating_total)
VALUES ('Contractor', NULL, '01/01/1925', 'contractor@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'CONTRACTOR', 'I am a contractor!', 2, 7, 29);
INSERT INTO authority (user_id, role) VALUES (8, 'ROLE_USER_VERIFIED');

INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id)
VALUES ('Applicant1', NULL, '01/01/1925', 'applicant1@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'CONTRACTOR', 'I am a contractor!', 3);
INSERT INTO authority (user_id, role) VALUES (9, 'ROLE_USER_VERIFIED');

INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id)
VALUES ('AcceptME', NULL, '01/01/1925', 'accept@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/ksnip_20240923-135135.png', 'CONTRACTOR', 'I am a contractor!', 4);
INSERT INTO authority (user_id, role) VALUES (10, 'ROLE_USER_VERIFIED');

INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id)
VALUES ('RejectMe', NULL, '01/01/1925', 'applicant@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'CONTRACTOR', 'I am a contractor!', 5);
INSERT INTO authority (user_id, role) VALUES (11, 'ROLE_USER_VERIFIED');

INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id)
VALUES ('Applicant2', NULL, '01/01/1925', 'applican2t@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'CONTRACTOR', 'I am a contractor!', 6);
INSERT INTO authority (user_id, role) VALUES (12, 'ROLE_USER_VERIFIED');

INSERT INTO app_users (first_name, last_name, date_of_birth, email, password, profile_picture, user_type, about_me, location_location_id)
VALUES ('Applicant3', NULL, '01/01/1925', 'applicant3@gmail.com', '$2a$10$FLrXAq7a..PfhdD3u03PlO7eF4.f7dr0Xa.ugBr3voJjYzCw4qhlG', '/images/default.jpg', 'CONTRACTOR', 'I am a contractor!', 7);
INSERT INTO authority (user_id, role) VALUES (13, 'ROLE_USER_VERIFIED');


INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden With Plants', '2.5', 'Cool garden with many plants', true, false, 1);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.0', 'Backyard garden', true, false, 2);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '1.0', 'Front yard garden', true, false, 3);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '0.5', 'Small balcony garden', true, false, 53);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.0', 'Large greenhouse', true, false, 4);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '10.0', 'Community gardening plot', true, false, 5);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.0', 'Rooftop garden', true, false, 6);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '0.3', 'Herb garden', true, false, 7);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.0', 'Vegetable patch', true, false, 8);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '2.0', 'Beautiful flower bed', true, false, 9);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '1.5', 'Peaceful zen garden', true, false, 10);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '8.0', 'Fruit orchard', true, false, 11);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '2.6', 'Another cool garden', true, false, 12);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.1', 'Another backyard garden', true, false, 13);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '2.7', 'Extra garden 1', true, false, 14);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.2', 'Extra garden 4', true, false, 15);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '2.8', 'Extra garden 2', true, false, 16);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.3', 'Extra garden 5', true, false, 17);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '2.9', 'Extra garden 3', true, false, 18);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.4', 'Extra garden 6', true, false, 19);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.5', 'Extra garden 7', true, false, 20);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.6', 'Extra garden 8', true, false, 21);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.7', 'Extra garden 9', true, false, 22);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.8', 'Extra garden 10', true, false, 23);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '3.9', 'Extra garden 11', true, false, 24);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.1', 'Extra garden 12', true, false, 25);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.2', 'Extra garden 13', true, false, 26);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.3', 'Extra garden 14', true, false, 27);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.4', 'Extra garden 15', true, false, 28);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.5', 'Extra garden 16', true, false, 29);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.6', 'Extra garden 17', true, false, 30);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.7', 'Extra garden 18', true, false, 31);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.8', 'Extra garden 19', true, false, 32);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '4.9', 'Extra garden 20', true, false, 33);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.1', 'Extra garden 21', true, false, 34);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.2', 'Extra garden 22', true, false, 35);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.3', 'Extra garden 23', true, false, 36);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.4', 'Extra garden 24', true, false, 37);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.5', 'Extra garden 25', true, false, 38);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.6', 'Extra garden 26', true, false, 39);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.7', 'Extra garden 27', true, false, 40);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.8', 'Extra garden 28', true, false, 41);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '5.9', 'Extra garden 29', true, false, 42);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.1', 'Extra garden 30', true, false, 43);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.2', 'Extra garden 31', true, false, 44);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.3', 'Extra garden 32', true, false, 45);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.4', 'Extra garden 33', true, false, 46);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.5', 'Extra garden 34', true, false, 47);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.6', 'Extra garden 35', true, false, 48);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.7', 'Extra garden 36', true, false, 49);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.8', 'Extra garden 37', true, false, 50);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '6.9', 'Extra garden 38', true, false, 51);
INSERT INTO garden (user_id, name, size, description, publicised, has_rained, location_location_id) VALUES (3, 'Garden', '7.1', 'Extra garden 39', true, false, 52);

INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-01', 10, 'This plant low key chill', '/images/PlantPlaceholder.jpg', 'Test Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-02', 1, 'Monstera Deliciosa', '/images/PlantPlaceholder.jpg', 'Big fan of this one');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-03', 4, 'Schefflera', '/images/PlantPlaceholder.jpg', 'I dont love it');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-04', 2, 'Phalaenopsis', '/images/PlantPlaceholder.jpg', 'aka Moth Orchid');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-05', 3, 'Apple Tree', '/images/PlantPlaceholder.jpg', 'An apple a day keeps the doctor away');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-06', 6, 'Silver Fern', '/images/PlantPlaceholder.jpg', 'sample description');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-07', 8, 'Golden Pothos', '/images/PlantPlaceholder.jpg', 'I love all kinds of pothos');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-08', 2, 'Jade Tree', '/images/PlantPlaceholder.jpg', 'These are cool');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-09', 3, 'Heartleaf Philodendron', '/images/PlantPlaceholder.jpg', 'Good beginner plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (1, '2024-01-10', 1, 'Aloe Vera', '/images/PlantPlaceholder.jpg', 'yummy');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (2, '2024-01-01', 5, 'This plant high key chill', '/images/PlantPlaceholder.jpg', 'Test Plant Two');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (3, '2024-01-01', 7, 'Lovely plant', '/images/PlantPlaceholder.jpg', 'Lovely Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (4, '2024-01-01', 12, 'Balcony plant', '/images/PlantPlaceholder.jpg', 'Balcony Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (5, '2024-01-01', 20, 'Greenhouse plant', '/images/PlantPlaceholder.jpg', 'Greenhouse Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (6, '2024-01-01', 15, 'Community plant', '/images/PlantPlaceholder.jpg', 'Community Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (7, '2024-01-01', 8, 'Rooftop plant', '/images/PlantPlaceholder.jpg', 'Rooftop Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (8, '2024-01-01', 9, 'Herb plant', '/images/PlantPlaceholder.jpg', 'Herb Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (9, '2024-01-01', 10, 'Vegetable plant', '/images/PlantPlaceholder.jpg', 'Vegetable Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (10, '2024-01-01', 5, 'Flower plant', '/images/PlantPlaceholder.jpg', 'Flower Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (11, '2024-01-01', 3, 'Zen plant', '/images/PlantPlaceholder.jpg', 'Zen Plant');
INSERT INTO plant (garden_id, date, count, description, image_path, name) VALUES (12, '2024-01-01', 25, 'Orchard plant', '/images/PlantPlaceholder.jpg', 'Orchard Plant');


INSERT INTO friendships (user1_id, user2_id) VALUES (2, 3);


INSERT INTO friend_requests (receiver_id, sender_id, status) VALUES (5, 2, 'PENDING');
INSERT INTO friend_requests (receiver_id, sender_id, status) VALUES (6, 2, 'DECLINED');
INSERT INTO friend_requests (receiver_id, sender_id, status) VALUES (2, 7, 'PENDING');

INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (1, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (2, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (3, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (4, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (5, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (6, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (7, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (8, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (9, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (10, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (11, true, true, true, true);
INSERT INTO widget_preferences (user_id, welcome, recent_gardens, recent_plants, friends) VALUES (12, true, true, true, true);

INSERT INTO tag (content, verified) VALUES ('cool', true);
INSERT INTO tag (content, verified) VALUES ('garden', true);
INSERT INTO tag (content, verified) VALUES ('floral', true);
INSERT INTO tag (content, verified) VALUES ('vegetable', true);
INSERT INTO tag (content, verified) VALUES ('tomato', true);
INSERT INTO tag (content, verified) VALUES ('orange', true);
INSERT INTO tag (content, verified) VALUES ('flowers', true);
INSERT INTO tag (content, verified) VALUES ('berries', true);
INSERT INTO tag (content, verified) VALUES ('bamboo', true);
INSERT INTO tag (content, verified) VALUES ('cactus', true);
INSERT INTO tag (content, verified) VALUES ('snake plant', true);
INSERT INTO tag (content, verified) VALUES ('orchid', true);
INSERT INTO tag (content, verified) VALUES ('Iris', true);
INSERT INTO tag (content, verified) VALUES ('Daisy', true);
INSERT INTO tag (content, verified) VALUES ('grass', true);
INSERT INTO tag (content, verified) VALUES ('cabbage', true);
INSERT INTO tag (content, verified) VALUES ('daffodil', true);
INSERT INTO tag (content, verified) VALUES ('basil', true);
INSERT INTO tag (content, verified) VALUES ('Bellflower', true);
INSERT INTO tag (content, verified) VALUES ('apple', true);
INSERT INTO tag (content, verified) VALUES ('lily', true);
INSERT INTO tag (content, verified) VALUES ('pine tree', true);
INSERT INTO tag (content, verified) VALUES ('maple', true);
INSERT INTO tag (content, verified) VALUES ('sunflower', true);
INSERT INTO tag (content, verified) VALUES ('palm tree', true);
INSERT INTO tag (content, verified) VALUES ('garlic', true);
INSERT INTO tag (content, verified) VALUES ('chives', true);
INSERT INTO tag (content, verified) VALUES ('gerber', true);
INSERT INTO tag (content, verified) VALUES ('kiwifruit', true);
INSERT INTO tag (content, verified) VALUES ('capsicum', true);
INSERT INTO tag (content, verified) VALUES ('jalapeno', true);
INSERT INTO tag (content, verified) VALUES ('roses', true);
INSERT INTO tag (content, verified) VALUES ('ferns', true);
INSERT INTO tag (content, verified) VALUES ('aloe', true);
INSERT INTO tag (content, verified) VALUES ('nuts', true);
INSERT INTO tag (content, verified) VALUES ('cherry blossoms', true);
INSERT INTO tag (content, verified) VALUES ('plum', true);
INSERT INTO tag (content, verified) VALUES ('onion', true);
INSERT INTO tag (content, verified) VALUES ('okra', true);
INSERT INTO tag (content, verified) VALUES ('panda plant', true);
INSERT INTO tag (content, verified) VALUES ('radish', true);
INSERT INTO tag (content, verified) VALUES ('mandarin', true);
INSERT INTO tag (content, verified) VALUES ('venus fly trap', true);
INSERT INTO tag (content, verified) VALUES ('watermelon', true);
INSERT INTO tag (content, verified) VALUES ('cherry tomatoes', true);
INSERT INTO tag (content, verified) VALUES ('cucumber', true);

INSERT INTO attached_tag (id, tag_id) VALUES (1, 46);
INSERT INTO attached_tag (id, tag_id) VALUES (2, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (2, 2);
INSERT INTO attached_tag (id, tag_id) VALUES (2, 3);
INSERT INTO attached_tag (id, tag_id) VALUES (2, 4);
INSERT INTO attached_tag (id, tag_id) VALUES (2, 5);
INSERT INTO attached_tag (id, tag_id) VALUES (3, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (3, 6);
INSERT INTO attached_tag (id, tag_id) VALUES (3, 7);
INSERT INTO attached_tag (id, tag_id) VALUES (3, 8);
INSERT INTO attached_tag (id, tag_id) VALUES (4, 9);
INSERT INTO attached_tag (id, tag_id) VALUES (4, 10);
INSERT INTO attached_tag (id, tag_id) VALUES (4, 11);
INSERT INTO attached_tag (id, tag_id) VALUES (4, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (5, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (5, 14);
INSERT INTO attached_tag (id, tag_id) VALUES (5, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (6, 16);
INSERT INTO attached_tag (id, tag_id) VALUES (7, 17);
INSERT INTO attached_tag (id, tag_id) VALUES (7, 18);
INSERT INTO attached_tag (id, tag_id) VALUES (7, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (7, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 21);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 22);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 23);
INSERT INTO attached_tag (id, tag_id) VALUES (8, 24);
INSERT INTO attached_tag (id, tag_id) VALUES (9, 25);
INSERT INTO attached_tag (id, tag_id) VALUES (9, 26);
INSERT INTO attached_tag (id, tag_id) VALUES (9, 27);
INSERT INTO attached_tag (id, tag_id) VALUES (9, 28);
INSERT INTO attached_tag (id, tag_id) VALUES (10, 29);
INSERT INTO attached_tag (id, tag_id) VALUES (11, 30);
INSERT INTO attached_tag (id, tag_id) VALUES (11, 31);
INSERT INTO attached_tag (id, tag_id) VALUES (12, 31);
INSERT INTO attached_tag (id, tag_id) VALUES (13, 32);
INSERT INTO attached_tag (id, tag_id) VALUES (13, 33);
INSERT INTO attached_tag (id, tag_id) VALUES (13, 34);
INSERT INTO attached_tag (id, tag_id) VALUES (14, 35);
INSERT INTO attached_tag (id, tag_id) VALUES (14, 36);
INSERT INTO attached_tag (id, tag_id) VALUES (14, 37);
INSERT INTO attached_tag (id, tag_id) VALUES (14, 38);
INSERT INTO attached_tag (id, tag_id) VALUES (15, 38);
INSERT INTO attached_tag (id, tag_id) VALUES (15, 39);
INSERT INTO attached_tag (id, tag_id) VALUES (16, 40);
INSERT INTO attached_tag (id, tag_id) VALUES (16, 41);
INSERT INTO attached_tag (id, tag_id) VALUES (16, 42);
INSERT INTO attached_tag (id, tag_id) VALUES (17, 43);
INSERT INTO attached_tag (id, tag_id) VALUES (17, 44);
INSERT INTO attached_tag (id, tag_id) VALUES (18, 45);
INSERT INTO attached_tag (id, tag_id) VALUES (18, 46);
INSERT INTO attached_tag (id, tag_id) VALUES (18, 22);
INSERT INTO attached_tag (id, tag_id) VALUES (18, 23);
INSERT INTO attached_tag (id, tag_id) VALUES (18, 24);
INSERT INTO attached_tag (id, tag_id) VALUES (19, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (19, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (19, 14);
INSERT INTO attached_tag (id, tag_id) VALUES (19, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (20, 16);
INSERT INTO attached_tag (id, tag_id) VALUES (20, 17);
INSERT INTO attached_tag (id, tag_id) VALUES (21, 18);
INSERT INTO attached_tag (id, tag_id) VALUES (21, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (21, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (21, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (22, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (22, 21);
INSERT INTO attached_tag (id, tag_id) VALUES (22, 22);
INSERT INTO attached_tag (id, tag_id) VALUES (23, 23);
INSERT INTO attached_tag (id, tag_id) VALUES (24, 24);
INSERT INTO attached_tag (id, tag_id) VALUES (25, 2);
INSERT INTO attached_tag (id, tag_id) VALUES (25, 3);
INSERT INTO attached_tag (id, tag_id) VALUES (25, 4);
INSERT INTO attached_tag (id, tag_id) VALUES (25, 4);
INSERT INTO attached_tag (id, tag_id) VALUES (26, 5);
INSERT INTO attached_tag (id, tag_id) VALUES (26, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (26, 6);
INSERT INTO attached_tag (id, tag_id) VALUES (26, 7);
INSERT INTO attached_tag (id, tag_id) VALUES (26, 8);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 9);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 10);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 11);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (27, 14);
INSERT INTO attached_tag (id, tag_id) VALUES (28, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (28, 16);
INSERT INTO attached_tag (id, tag_id) VALUES (28, 17);
INSERT INTO attached_tag (id, tag_id) VALUES (28, 18);
INSERT INTO attached_tag (id, tag_id) VALUES (28, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (29, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (30, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (30, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (31, 21);
INSERT INTO attached_tag (id, tag_id) VALUES (31, 22);
INSERT INTO attached_tag (id, tag_id) VALUES (31, 23);
INSERT INTO attached_tag (id, tag_id) VALUES (32, 24);
INSERT INTO attached_tag (id, tag_id) VALUES (33, 46);
INSERT INTO attached_tag (id, tag_id) VALUES (33, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (33, 2);
INSERT INTO attached_tag (id, tag_id) VALUES (34, 3);
INSERT INTO attached_tag (id, tag_id) VALUES (34, 4);
INSERT INTO attached_tag (id, tag_id) VALUES (34, 5);
INSERT INTO attached_tag (id, tag_id) VALUES (34, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (35, 6);
INSERT INTO attached_tag (id, tag_id) VALUES (35, 7);
INSERT INTO attached_tag (id, tag_id) VALUES (35, 8);
INSERT INTO attached_tag (id, tag_id) VALUES (36, 9);
INSERT INTO attached_tag (id, tag_id) VALUES (36, 10);
INSERT INTO attached_tag (id, tag_id) VALUES (36, 11);
INSERT INTO attached_tag (id, tag_id) VALUES (36, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (37, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (37, 14);
INSERT INTO attached_tag (id, tag_id) VALUES (38, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (38, 16);
INSERT INTO attached_tag (id, tag_id) VALUES (39, 17);
INSERT INTO attached_tag (id, tag_id) VALUES (40, 18);
INSERT INTO attached_tag (id, tag_id) VALUES (40, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (41, 38);
INSERT INTO attached_tag (id, tag_id) VALUES (41, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (41, 20);
INSERT INTO attached_tag (id, tag_id) VALUES (41, 37);
INSERT INTO attached_tag (id, tag_id) VALUES (41, 22);
INSERT INTO attached_tag (id, tag_id) VALUES (42, 23);
INSERT INTO attached_tag (id, tag_id) VALUES (42, 24);
INSERT INTO attached_tag (id, tag_id) VALUES (43, 45);
INSERT INTO attached_tag (id, tag_id) VALUES (44, 46);
INSERT INTO attached_tag (id, tag_id) VALUES (45, 1);
INSERT INTO attached_tag (id, tag_id) VALUES (45, 2);
INSERT INTO attached_tag (id, tag_id) VALUES (45, 3);
INSERT INTO attached_tag (id, tag_id) VALUES (46, 4);
INSERT INTO attached_tag (id, tag_id) VALUES (46, 5);
INSERT INTO attached_tag (id, tag_id) VALUES (46, 29);
INSERT INTO attached_tag (id, tag_id) VALUES (46, 27);
INSERT INTO attached_tag (id, tag_id) VALUES (46, 19);
INSERT INTO attached_tag (id, tag_id) VALUES (47, 8);
INSERT INTO attached_tag (id, tag_id) VALUES (47, 39);
INSERT INTO attached_tag (id, tag_id) VALUES (48, 41);
INSERT INTO attached_tag (id, tag_id) VALUES (48, 11);
INSERT INTO attached_tag (id, tag_id) VALUES (48, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (49, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (49, 14);
INSERT INTO attached_tag (id, tag_id) VALUES (49, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (50, 42);
INSERT INTO attached_tag (id, tag_id) VALUES (50, 17);
INSERT INTO attached_tag (id, tag_id) VALUES (50, 9);
INSERT INTO attached_tag (id, tag_id) VALUES (50, 10);
INSERT INTO attached_tag (id, tag_id) VALUES (51, 11);
INSERT INTO attached_tag (id, tag_id) VALUES (51, 12);
INSERT INTO attached_tag (id, tag_id) VALUES (51, 13);
INSERT INTO attached_tag (id, tag_id) VALUES (52, 43);
INSERT INTO attached_tag (id, tag_id) VALUES (53, 15);
INSERT INTO attached_tag (id, tag_id) VALUES (53, 16);
INSERT INTO attached_tag (id, tag_id) VALUES (53, 17);


INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:01.123', 'Pruning', 'I need someone to prune my trees', 10, 20, 1, '2024-12-02', '2024-12-09', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:02.234', 'Weeding', 'Neglected vege garden - needs about 4 hours of weeding', 15, 25, 2, '2024-12-05', '2024-12-12', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:03.345', 'Lawn Mowing', '200 square metre lawn, please focus on edges.', 20, 30, 1, '2024-11-20', '2024-11-25', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:04.456', 'Lemon Tree Planting', 'Planting 10 lemon trees in my backyard', 25, 50, 2, '2024-10-15', '2024-10-20', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:05.567', 'Garden Cleanup', 'Need full garden cleanup before winter', 30, 60, 51, '2024-11-01', '2024-11-07', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:06.678', 'Irrigation Setup', 'Set up a drip irrigation system for my garden', 50, 100, 51, '2024-09-15', '2024-09-22', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:07.789', 'Tree Removal', 'Need a large oak tree removed from my yard', 100, 200, 51, '2024-10-05', '2024-10-10', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:08.890', 'Fence Repair', 'Repair garden fence that was damaged by wind', 40, 80, 51, '2024-09-20', '2024-09-27', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:09.901', 'Composting', 'Need someone to create a composting setup', 20, 50, 1, '2024-11-12', '2024-11-18', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:10.012', 'Paving Stone Installation', 'Lay down paving stones for a garden path', 60, 120, 1, '2024-10-01', '2024-10-07', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:11.123', 'Greenhouse Setup', 'Help me set up a small greenhouse', 150, 300, 51, '2024-09-25', '2024-10-05', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:12.234', 'Garden Replanting', 'Replant all flower beds with native plants', 80, 150, 51, '2024-11-10', '2024-11-20', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:13.345', 'Pool Landscaping', 'Landscape the area around my pool', 200, 400, 51, '2024-12-01', '2024-12-15', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:14.456', 'Shed Construction', 'Build a small garden shed', 250, 500, 51, '2024-10-20', '2024-10-30', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-08-31T12:45:15.567', 'Rock Garden', 'Create a small rock garden with succulents', 100, 250, 51, '2024-09-05', '2024-09-12', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:16.678', 'Outdoor Lighting Installation', 'Install lighting along garden pathways', 75, 150, 51, '2025-11-22', '2025-11-28', 3, 8, false, 100, '2025-11-25', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:17.789', 'Retaining Wall', 'Build a retaining wall for garden landscaping', 300, 600, 1, '2024-09-10', '2024-09-12', 3, 8, true, 600, '2024-09-11', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:17.789', 'Sycamore Tree', 'Cut it down please', 300, 600, 1, '2025-09-10', '2025-09-12', 3, 8, true, 600, '2025-09-11', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Pergola Installation', 'Build a pergola in my backyard garden', 500, 1000, 1, '2025-10-01', '2025-10-10', 3, 8, false, 700, '2025-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Assigned Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, false, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, contractor_id, completed, agreed_price, agreed_date, image_path)
VALUES('2024-08-31T12:45:18.890', 'Completed Service Request', 'description', 500, 1000, 1, '2024-10-01', '2024-10-10', 3, 8, true, 700, '2024-10-10', '/images/PlantPlaceholder.jpg');

INSERT INTO service_request (release_date_time, title, description, price_min, price_max, garden_id, date_min, date_max, user_id, completed, image_path)
VALUES('2024-09-15T12:45:18.890', 'Applications and Questions', 'description', 1, 1000, 2, '2024-10-01', '2025-07-29', 3, false, '/images/PlantPlaceholder.jpg');

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',8 ,31);

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',9 ,31);

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',10 ,31);

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',11 ,31);

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',12 ,31);

INSERT INTO job_application (DATE, PRICE, STATUS, CONTRACTOR_ID, JOB_ID)
VALUES ('2024-10-01', 500, 'PENDING',13 ,31);

INSERT INTO CONTRACTOR_PICTURES (USER_ID, WORK_PICTURES) VALUES (8,'/images/default.jpg');
INSERT INTO CONTRACTOR_PICTURES (USER_ID, WORK_PICTURES) VALUES (8,'/images/pfp1.png');
INSERT INTO CONTRACTOR_PICTURES (USER_ID, WORK_PICTURES) VALUES (8,'/images/pfp2.png');
INSERT INTO CONTRACTOR_PICTURES (USER_ID, WORK_PICTURES) VALUES (8,'/images/PlantPlaceholder.jpg');
INSERT INTO CONTRACTOR_PICTURES (USER_ID, WORK_PICTURES) VALUES (8,'/images/tomato_image.jpeg');


INSERT INTO QUESTIONANSWER (QUESTION_ID, ANSWER, ANSWER_DATE, QUESTION, QUESTION_DATE, CONTRACTOR_ID, OWNER_ID, REQUEST_ID)
VALUES (1, 'No, I have one for you', '2024-10-03' ,  'do I need a shovel?', '2024-10-02', 8, 3, 31);

INSERT INTO QUESTIONANSWER (QUESTION_ID, ANSWER, ANSWER_DATE, QUESTION, QUESTION_DATE, CONTRACTOR_ID, OWNER_ID, REQUEST_ID)
VALUES (2, null, null, 'Would be able to extend the dates?', '2024-10-02', 8, 3, 31);

INSERT INTO QUESTIONANSWER (QUESTION_ID, ANSWER, ANSWER_DATE, QUESTION, QUESTION_DATE, CONTRACTOR_ID, OWNER_ID, REQUEST_ID)
VALUES (3, null, null, 'This is a rip off!', '2024-10-02', 8, 3, 31);

