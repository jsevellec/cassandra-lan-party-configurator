CREATE KEYSPACE "lanparty" WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', '1' : 3, '2' : 3, '3' : 3};
use lanparty;

CREATE TABLE attendee (
  email text primary key,
  first_name text,
  last_name text
);
