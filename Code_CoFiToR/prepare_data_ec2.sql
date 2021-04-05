LOAD DATA INFILE '/var/lib/mysql-files/ratings.csv' INTO TABLE topn.ratings FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n'  IGNORE 1 LINES
(user_id, movie_id, rating, when_rated) SET rand_val=rand();


update ratings, (SELECT user_id,movie_id,rating,when_rated FROM ratings order by rand_val limit 100000) to_update
set ratings.dataset_id = 1
where ratings.movie_id=to_update.movie_id and
ratings.user_id=to_update.user_id;

SELECT user_id,movie_id,rating,when_rated FROM ratings where dataset_id=1 INTO OUTFILE '/var/lib/mysql-files/set1.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

update ratings, (SELECT user_id,movie_id,rating,when_rated FROM ratings where dataset_id is null order by rand_val limit 100000) to_update
set ratings.dataset_id = 2
where ratings.movie_id=to_update.movie_id and
ratings.user_id=to_update.user_id;

SELECT user_id,movie_id,rating,when_rated FROM ratings where dataset_id=2 and rating=5 INTO OUTFILE '/var/lib/mysql-files/set2.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

CREATE TABLE `set1` (
  `user_id` int NOT NULL,
  `movie_id` int NOT NULL,
  `rating` float DEFAULT NULL,
  `when_rated`  bigint(19) unsigned,
  `rand_val` double, 
  PRIMARY KEY (`user_id`,`movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO set1 (user_id,movie_id,rating,when_rated,rand_val) SELECT user_id,movie_id,rating,when_rated,rand_val from ratings where dataset_id = 1;

INSERT INTO set2 (user_id,movie_id,rating,when_rated,rand_val) SELECT user_id,movie_id,rating,when_rated,rand_val from ratings where dataset_id = 2 and rating = 5;


CREATE TABLE `users` (
  `id` int not null primary key AUTO_INCREMENT,
  `user_id` int
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `movies` (
  `id` int not null primary key AUTO_INCREMENT,
  `movie_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX movie_id_idx ON movies(movie_id); 

insert into users (user_id) select set1.user_id as user_id from set1 union select set2.user_id as user_id from set2  order by user_id;

insert into movies (movie_id) select set1.movie_id as movie_id from set1 union select set2.movie_id as movie_id from set2  order by movie_id;

select users.id,movies.id,set1.rating,set1.when_rated from set1 inner join users 
on users.user_id = set1.user_id inner join movies
on movies.movie_id = set1.movie_id INTO OUTFILE '/var/lib/mysql-files/set1.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

select users.id,movies.id,set2.rating,set2.when_rated from set2 inner join users 
on users.user_id = set2.user_id inner join movies
on movies.movie_id = set2.movie_id 
INTO OUTFILE '/var/lib/mysql-files/set2.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

select max(id) from movies;

select max(id) from users;
select * from set1;
select min(when_rated) from set1;
select max(when_rated) from set1;
update set1 s set norm_when_rated=((s.when_rated - 825010460)/(1574289928-825010460));
select min(when_rated) from set2;
select max(when_rated) from set2;
update set2 s set norm_when_rated=((s.when_rated - 827943433)/(1574119691-827943433));

select users.id,movies.id,set1.rating,set1.when_rated,set1.norm_when_rated from set1 inner join users 
on users.user_id = set1.user_id inner join movies
on movies.movie_id = set1.movie_id INTO OUTFILE '/var/lib/mysql-files/set1.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

select users.id,movies.id,set2.rating,set2.when_rated,set2.norm_when_rated from set2 inner join users 
on users.user_id = set2.user_id inner join movies
on movies.movie_id = set2.movie_id 
INTO OUTFILE '/var/lib/mysql-files/set2.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

 