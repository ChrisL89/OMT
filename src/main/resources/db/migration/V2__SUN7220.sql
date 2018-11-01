/*
Event Schedule to expire offer after offer end date is in the past.
*/
CREATE EVENT ExpireEvent
	ON SCHEDULE
		EVERY 24 HOUR
		DO
			UPDATE offer set status = 'EXPIRED' where status in ('CREATED', 'ACTIVATED') and end_date < NOW();