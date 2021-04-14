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

SELECT user_id,movie_id,rating,when_rated FROM ratings where dataset_id=1 INTO OUTFILE '/var/lib/mysql-files/set1.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';
SELECT user_id,movie_id,rating,when_rated FROM ratings where dataset_id=2 and rating=5 INTO OUTFILE '/var/lib/mysql-files/set2.csv' FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

select movies.id, avg(set1.norm_when_rated) from set1 
inner join movies
on  set1.movie_id=movies.movie_id
group by movies.id
INTO OUTFILE '/var/lib/mysql-files/rateTimeData.csv'
FIELDS TERMINATED BY ','
ESCAPED BY '"'
LINES TERMINATED BY '\n';

select movies.id, min(set1.norm_when_rated)-0.5 from set1 
inner join movies
on  set1.movie_id=movies.movie_id
group by movies.id
INTO OUTFILE '/var/lib/mysql-files/ItemERTData.csv'
FIELDS TERMINATED BY ','
ESCAPED BY '"'
LINES TERMINATED BY '\n';

select users.id,avg(set1.norm_when_rated)-0.5 from set1 
inner join users 
on users.user_id = set1.user_id
group by users.id
INTO OUTFILE '/var/lib/mysql-files/UserERTData.csv' 
FIELDS TERMINATED BY ',' ESCAPED BY '"' LINES TERMINATED BY '\n';

select * from set1 where  user_id = 3;
select * from movies where id=1;
select * from set1 where movie_id = 1;

LOAD DATA INFILE '/var/lib/mysql-files/movies.csv' INTO TABLE topn.movie_info FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n'  IGNORE 1 LINES
( movie_id, title, genres) SET rand_val=rand();

CREATE TABLE `movie_info2` (
  `movie_id` int DEFAULT NULL,
  `movie_name` varchar(245) DEFAULT NULL,
  `movie_year` int DEFAULT NULL,
  `movies_id` int DEFAULT NULL,
  `genre` varchar(45) DEFAULT NULL,
  KEY `movie_info_movies_fk_idx2` (`movies_id`),
  CONSTRAINT `movie_info_movies_fk2` FOREIGN KEY (`movies_id`) REFERENCES `movies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `set1_set2_movie_info` (
  `movie_id` int DEFAULT NULL,
  `movie_name` varchar(245) DEFAULT NULL,
  `movie_year` int DEFAULT NULL,
  `movies_id` int DEFAULT NULL,
  `genre` varchar(45) DEFAULT NULL,
  KEY `movie_info_movies_fk_idx3` (`movies_id`),
  CONSTRAINT `movie_info_movies_fk3` FOREIGN KEY (`movies_id`) REFERENCES `movies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `genre` (
  `genre_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`genre_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



LOAD DATA INFILE '/var/lib/mysql-files/movie_info.csv' INTO TABLE topn.movie_info FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n'  
(movie_id, movie_name, movie_year,genre);
LOAD DATA INFILE '/var/lib/mysql-files/genre.csv' INTO TABLE topn.genre FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n'  
(name);


INSERT INTO set1_set2_movie_info(`movie_id`,
`movie_name`,
`movie_year`,
`genre`,
`movies_id`,
`genre_id`)
    select mi.movie_id,mi.movie_name,mi.movie_year,mi.genre,m.id,g.genre_id
    from movie_info mi INNER JOIN movies m 
    ON mi.movie_id = m.movie_id INNER JOIN genre g ON mi.genre = g.name;

    


CREATE TABLE `topn`.`step1_output` (
  `user_id` INT NOT NULL,
  `movie_id` INT NOT NULL,
  `rating` FLOAT NULL,
  PRIMARY KEY (`user_id`, `movie_id`));
  

LOAD DATA LOCAL INFILE '/var/lib/mysql-files/step1_output.csv'
INTO TABLE step1_output
FIELDS TERMINATED by ','
LINES TERMINATED BY '\n';

select max(count(s.genre_id)) as genre_count,s.genre
from set1_set2_movie_info s INNER JOIN step1_output o
ON s.movies_id = o.movie_id
where o.user_id=1
group by s.genre_id
order by genre_count desc ;

ERROR 1055 (42000): Expression #2 of SELECT list is not in GROUP BY clause and contains nonaggregated column
sudo vim /etc/mysql/conf.d/mysql.cnf;
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION;
sudo service mysql restart;

SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
