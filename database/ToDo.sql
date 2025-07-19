--
-- PostgreSQL database cluster dump
--

-- Started on 2025-07-19 15:30:54

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:cL3iNEii6u171ox+pnSyhQ==$ut5GrRVNhLExj1mr7spGLsU+nU4QgsXTmCjhqZA6Sek=:RzMM/PFtUQMt9infkO5KBrEZKq7I4UvSj1bvE1AefAc=';

--
-- User Configurations
--








--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-07-19 15:30:55

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- Completed on 2025-07-19 15:30:55

--
-- PostgreSQL database dump complete
--

--
-- Database "ToDo" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-07-19 15:30:55

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4949 (class 1262 OID 57582)
-- Name: ToDo; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "ToDo" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'it-IT';


ALTER DATABASE "ToDo" OWNER TO postgres;

\connect "ToDo"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 57706)
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- TOC entry 4950 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 58463)
-- Name: activities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.activities (
    todo_id uuid NOT NULL,
    activity_title character varying(255) NOT NULL,
    completed boolean DEFAULT false
);


ALTER TABLE public.activities OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 58384)
-- Name: boards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.boards (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    color character varying(50),
    user_id uuid NOT NULL
);


ALTER TABLE public.boards OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 58383)
-- Name: boards_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.boards_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.boards_id_seq OWNER TO postgres;

--
-- TOC entry 4951 (class 0 OID 0)
-- Dependencies: 219
-- Name: boards_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.boards_id_seq OWNED BY public.boards.id;


--
-- TOC entry 223 (class 1259 OID 58474)
-- Name: shared_todos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.shared_todos (
    todo_id uuid NOT NULL,
    shared_with_username character varying(255) NOT NULL
);


ALTER TABLE public.shared_todos OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 58443)
-- Name: todos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.todos (
    id uuid NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'To Do'::character varying,
    due_date date,
    created_date date DEFAULT CURRENT_DATE NOT NULL,
    "position" integer DEFAULT 0,
    owner_username character varying(255) NOT NULL,
    board_id integer NOT NULL,
    url character varying(255),
    image character varying(255),
    color character varying(50)
);


ALTER TABLE public.todos OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 58373)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 4770 (class 2604 OID 58387)
-- Name: boards id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boards ALTER COLUMN id SET DEFAULT nextval('public.boards_id_seq'::regclass);


--
-- TOC entry 4942 (class 0 OID 58463)
-- Dependencies: 222
-- Data for Name: activities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.activities (todo_id, activity_title, completed) FROM stdin;
852e21e3-82ca-45e1-8ac8-2d97181690f2	asd	f
f7077273-f2c7-4ae0-92bb-68cf0aa62145	asd	f
b53fa81d-c319-478d-81ac-53973a8e4e27	dey	t
\.


--
-- TOC entry 4940 (class 0 OID 58384)
-- Dependencies: 220
-- Data for Name: boards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.boards (id, name, color, user_id) FROM stdin;
7	Work	Default	e3050c5c-b16f-4a03-9ce8-7deff9bee09b
8	University	Default	e3050c5c-b16f-4a03-9ce8-7deff9bee09b
9	Free Time	Default	e3050c5c-b16f-4a03-9ce8-7deff9bee09b
2	University	Orange	3125afcf-9162-4000-8b3c-6a0fec0e8021
1	Work	Green	3125afcf-9162-4000-8b3c-6a0fec0e8021
3	Free Time	Yellow	3125afcf-9162-4000-8b3c-6a0fec0e8021
5	University	Red	df1878fc-1373-41b2-b118-0416df2fa308
4	Work	Yellow	df1878fc-1373-41b2-b118-0416df2fa308
6	Free Time	Green	df1878fc-1373-41b2-b118-0416df2fa308
\.


--
-- TOC entry 4943 (class 0 OID 58474)
-- Dependencies: 223
-- Data for Name: shared_todos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.shared_todos (todo_id, shared_with_username) FROM stdin;
910f33c8-6783-4c87-a6f2-a9486cfc0824	mary evans
910f33c8-6783-4c87-a6f2-a9486cfc0824	pa
852e21e3-82ca-45e1-8ac8-2d97181690f2	pa
f7077273-f2c7-4ae0-92bb-68cf0aa62145	pa
b53fa81d-c319-478d-81ac-53973a8e4e27	cri
\.


--
-- TOC entry 4941 (class 0 OID 58443)
-- Dependencies: 221
-- Data for Name: todos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.todos (id, title, description, status, due_date, created_date, "position", owner_username, board_id, url, image, color) FROM stdin;
910f33c8-6783-4c87-a6f2-a9486cfc0824	prova	prova	Not Started	2025-07-18	2025-07-17	1	cri	6		happybirthday.jpg	Yellow
852e21e3-82ca-45e1-8ac8-2d97181690f2	asd	asd	Incomplete	2025-07-18	2025-07-17	1	mary evans	2		happynewyear.jpg	Green
f7077273-f2c7-4ae0-92bb-68cf0aa62145	prova 2	asd	Incomplete	2025-07-18	2025-07-17	2	cri	6		read.jpg	Yellow
b53fa81d-c319-478d-81ac-53973a8e4e27	mary	asd	Complete	2025-07-19	2025-07-19	2	mary evans	3	https://it.pinterest.com/	filo.jpg	Orange
\.


--
-- TOC entry 4938 (class 0 OID 58373)
-- Dependencies: 218
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash) FROM stdin;
3125afcf-9162-4000-8b3c-6a0fec0e8021	mary evans	59713396
df1878fc-1373-41b2-b118-0416df2fa308	cri	181da
e3050c5c-b16f-4a03-9ce8-7deff9bee09b	pa	df1
\.


--
-- TOC entry 4952 (class 0 OID 0)
-- Dependencies: 219
-- Name: boards_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.boards_id_seq', 9, true);


--
-- TOC entry 4784 (class 2606 OID 58468)
-- Name: activities activities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_pkey PRIMARY KEY (todo_id, activity_title);


--
-- TOC entry 4780 (class 2606 OID 58389)
-- Name: boards boards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boards
    ADD CONSTRAINT boards_pkey PRIMARY KEY (id);


--
-- TOC entry 4786 (class 2606 OID 58478)
-- Name: shared_todos shared_todos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shared_todos
    ADD CONSTRAINT shared_todos_pkey PRIMARY KEY (todo_id, shared_with_username);


--
-- TOC entry 4782 (class 2606 OID 58452)
-- Name: todos todos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_pkey PRIMARY KEY (id);


--
-- TOC entry 4776 (class 2606 OID 58380)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4778 (class 2606 OID 58382)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4790 (class 2606 OID 58469)
-- Name: activities activities_todo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.activities
    ADD CONSTRAINT activities_todo_id_fkey FOREIGN KEY (todo_id) REFERENCES public.todos(id) ON DELETE CASCADE;


--
-- TOC entry 4787 (class 2606 OID 58390)
-- Name: boards boards_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boards
    ADD CONSTRAINT boards_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 4791 (class 2606 OID 58484)
-- Name: shared_todos shared_todos_shared_with_username_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shared_todos
    ADD CONSTRAINT shared_todos_shared_with_username_fkey FOREIGN KEY (shared_with_username) REFERENCES public.users(username) ON DELETE CASCADE;


--
-- TOC entry 4792 (class 2606 OID 58479)
-- Name: shared_todos shared_todos_todo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.shared_todos
    ADD CONSTRAINT shared_todos_todo_id_fkey FOREIGN KEY (todo_id) REFERENCES public.todos(id) ON DELETE CASCADE;


--
-- TOC entry 4788 (class 2606 OID 58458)
-- Name: todos todos_board_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_board_id_fkey FOREIGN KEY (board_id) REFERENCES public.boards(id) ON DELETE CASCADE;


--
-- TOC entry 4789 (class 2606 OID 58453)
-- Name: todos todos_owner_username_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_owner_username_fkey FOREIGN KEY (owner_username) REFERENCES public.users(username) ON DELETE CASCADE;


-- Completed on 2025-07-19 15:30:57

--
-- PostgreSQL database dump complete
--

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-07-19 15:30:57

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 7 (class 2615 OID 16387)
-- Name: pgagent; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pgagent;


ALTER SCHEMA pgagent OWNER TO postgres;

--
-- TOC entry 5002 (class 0 OID 0)
-- Dependencies: 7
-- Name: SCHEMA pgagent; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA pgagent IS 'pgAgent system tables';


--
-- TOC entry 2 (class 3079 OID 16388)
-- Name: pgagent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgagent WITH SCHEMA pgagent;


--
-- TOC entry 5003 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgagent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgagent IS 'A PostgreSQL job scheduler';


--
-- TOC entry 4780 (class 0 OID 16389)
-- Dependencies: 223
-- Data for Name: pga_jobagent; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_jobagent (jagpid, jaglogintime, jagstation) FROM stdin;
6780	2025-07-09 13:52:10.415037+02	LAPTOP-KHFMPB7S
\.


--
-- TOC entry 4781 (class 0 OID 16398)
-- Dependencies: 225
-- Data for Name: pga_jobclass; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_jobclass (jclid, jclname) FROM stdin;
\.


--
-- TOC entry 4782 (class 0 OID 16408)
-- Dependencies: 227
-- Data for Name: pga_job; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_job (jobid, jobjclid, jobname, jobdesc, jobhostagent, jobenabled, jobcreated, jobchanged, jobagentid, jobnextrun, joblastrun) FROM stdin;
\.


--
-- TOC entry 4784 (class 0 OID 16456)
-- Dependencies: 231
-- Data for Name: pga_schedule; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_schedule (jscid, jscjobid, jscname, jscdesc, jscenabled, jscstart, jscend, jscminutes, jschours, jscweekdays, jscmonthdays, jscmonths) FROM stdin;
\.


--
-- TOC entry 4785 (class 0 OID 16484)
-- Dependencies: 233
-- Data for Name: pga_exception; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_exception (jexid, jexscid, jexdate, jextime) FROM stdin;
\.


--
-- TOC entry 4786 (class 0 OID 16498)
-- Dependencies: 235
-- Data for Name: pga_joblog; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_joblog (jlgid, jlgjobid, jlgstatus, jlgstart, jlgduration) FROM stdin;
\.


--
-- TOC entry 4783 (class 0 OID 16432)
-- Dependencies: 229
-- Data for Name: pga_jobstep; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_jobstep (jstid, jstjobid, jstname, jstdesc, jstenabled, jstkind, jstcode, jstconnstr, jstdbname, jstonerror, jscnextrun) FROM stdin;
\.


--
-- TOC entry 4787 (class 0 OID 16514)
-- Dependencies: 237
-- Data for Name: pga_jobsteplog; Type: TABLE DATA; Schema: pgagent; Owner: postgres
--

COPY pgagent.pga_jobsteplog (jslid, jsljlgid, jsljstid, jslstatus, jslresult, jslstart, jslduration, jsloutput) FROM stdin;
\.


-- Completed on 2025-07-19 15:30:59

--
-- PostgreSQL database dump complete
--

--
-- Database "rubrica" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-07-19 15:31:00

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4896 (class 1262 OID 16596)
-- Name: rubrica; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE rubrica WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'it-IT';


ALTER DATABASE rubrica OWNER TO postgres;

\connect rubrica

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 32990)
-- Name: rubrica; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rubrica (
    name character varying(20) NOT NULL,
    email character varying(50),
    phone character varying(10)
);


ALTER TABLE public.rubrica OWNER TO postgres;

--
-- TOC entry 4890 (class 0 OID 32990)
-- Dependencies: 217
-- Data for Name: rubrica; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rubrica (name, email, phone) FROM stdin;
Mary	mary@it	333333
Cora	asd@	123
\.


--
-- TOC entry 4742 (class 2606 OID 32996)
-- Name: rubrica rubrica_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rubrica
    ADD CONSTRAINT rubrica_email_key UNIQUE (email);


--
-- TOC entry 4744 (class 2606 OID 32998)
-- Name: rubrica rubrica_phone_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rubrica
    ADD CONSTRAINT rubrica_phone_key UNIQUE (phone);


-- Completed on 2025-07-19 15:31:01

--
-- PostgreSQL database dump complete
--

-- Completed on 2025-07-19 15:31:01

--
-- PostgreSQL database cluster dump complete
--

