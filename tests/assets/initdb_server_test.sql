-- Copyright (c) 2016 by Niklaus Giger niklaus.giger@member.fsf.org
--
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http:#www.eclipse.org/legal/epl-v10.html
--
-- Small helper to setup a small test database for the elexis server
-- we assume that this script is run inside a mysql (as root)
--
create database if not exists elexis_server_test;
-- next statement for mysql
connect elexis_server_test;
SET autocommit=0;
source /docker-entrypoint-initdb.d/createDB.script
source /docker-entrypoint-initdb.d/dbScripts/BillingVKPreise.sql
source /docker-entrypoint-initdb.d/dbScripts/ArzttarifePhysio.sql
source /docker-entrypoint-initdb.d/dbScripts/User.sql
source /docker-entrypoint-initdb.d/dbScripts/Role.sql
source /docker-entrypoint-initdb.d/dbScripts/LaborTarif2009.sql
source /docker-entrypoint-initdb.d/dbScripts/Tarmed.sql
source /docker-entrypoint-initdb.d/dbScripts/sampleContacts.sql
source /docker-entrypoint-initdb.d/dbScripts/LaborItemsWerteResults.sql
source /docker-entrypoint-initdb.d/dbScripts/ArtikelstammItem.sql
source /docker-entrypoint-initdb.d/dbScripts/TarmedKumulation.sql
