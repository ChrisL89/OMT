/*
SUN-7494 - AFS Tool | Add the Deposit options and fields to the Register bonus template: Back End Part
*/
ALTER TABLE offer ADD deposit_required BOOLEAN NOT NULL DEFAULT false;