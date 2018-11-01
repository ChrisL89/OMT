CREATE TABLE offer(
	id BIGINT AUTO_INCREMENT,
	offer_code VARCHAR(255),
	description VARCHAR(255),
	trigger_type VARCHAR(50),
	reward_type VARCHAR(50),
	opt_in BOOLEAN,
	all_players BOOLEAN,
	create_date DATETIME,
	start_date DATETIME,
	end_date DATETIME,
	status VARCHAR(50),
	currency VARCHAR(5),
	min_deposit FLOAT NOT NULL,
	max_deposit FLOAT NOT NULL,
	first_deposit BOOLEAN,
	register_channel VARCHAR(255),
	deactivated_date DATETIME,
	PRIMARY KEY (id)
);


CREATE TABLE player(
	id BIGINT AUTO_INCREMENT,
	status VARCHAR(50) NOT NULL,
	viewed BOOLEAN NOT NULL,
	customer_id BIGINT NOT NULL,
	offer_id BIGINT NOT NULL,
	customer_name VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);


CREATE TABLE rewards(
	type VARCHAR(31) NOT NULL,
	id BIGINT AUTO_INCREMENT,
	allow_all_games BOOLEAN,
	games BLOB,
	provider_name VARCHAR(50) NOT NULL,
	coin_level INTEGER NOT NULL,
	num_of_free_spins INTEGER NOT NULL,
	offer_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE activation_history(
    id BIGINT AUTO_INCREMENT,
    activation_message VARCHAR(255) NOT NULL,
    customer_id BIGINT NOT NULL,
    offer_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE game(
    id BIGINT AUTO_INCREMENT,
    game_id VARCHAR(255) NOT NULL,
    game_name VARCHAR(255) NOT NULL,
    reward_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);