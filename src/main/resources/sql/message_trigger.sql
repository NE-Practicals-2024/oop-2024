DELIMITER
//
CREATE TRIGGER message_trigger
    AFTER INSERT
    ON transaction
    FOR EACH ROW
BEGIN
    -- Declare variables
    DECLARE message VARCHAR(255);
    DECLARE customer_name VARCHAR(255);
    DECLARE account_number VARCHAR(255);
    DECLARE amount DECIMAL(10,2);
-- Get the customer name
    SELECT name INTO customer_name FROM customer WHERE id = NEW.customer_id;
-- Get the account number
    SELECT account_number INTO account_number FROM account WHERE id = NEW.account_id;
-- Create message depending on transaction type
    IF NEW.transaction_type = 'WITHDRAW' THEN
        SET message = CONCAT('Dear ', customer_name, ' Your WITHDRAW of ', NEW.amount, ' on your account ', account_number, ' has been completed successfully');
    ELSE
        SET message = CONCAT('Dear ', customer_name, ' Your SAVING of ', NEW.amount, ' on your account ', account_number, ' has been completed successfully');
END IF;
-- Insert the message into the message table
INSERT INTO message (customer_id, message, dateTime)
VALUES (NEW.customer_id, message, NOW());

END
//
DELIMITER ;