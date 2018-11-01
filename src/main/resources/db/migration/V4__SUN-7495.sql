/*
SUN-7495 - AFS Tool | Add the Promo Code and the select All players options in the Deposit template
*/
ALTER TABLE offer CHANGE opt_in promo_code_required BOOLEAN NOT NULL DEFAULT false;