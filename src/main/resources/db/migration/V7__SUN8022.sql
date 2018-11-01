/*
SUN-8022 - AFS - More than one offer code gets created when we double click on CREATE button on the OMT tool and on the database
*/
ALTER TABLE offer ADD UNIQUE(`offer_code`);