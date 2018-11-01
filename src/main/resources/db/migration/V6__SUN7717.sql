/*
Set ExpireEvent event to run every hour
*/
ALTER EVENT ExpireEvent
	ON SCHEDULE
		EVERY 1 HOUR
	STARTS '2018-03-05 00:05:00';