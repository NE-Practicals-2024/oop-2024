DELIMITER //
-- Create a trigger to register message sent to each customer based on each transaction
-- save these (customer_id, message, dateTime)
-- The message should be in the format "Dear <Customer Names> Your <WITHDRAW/SAVING> of <AMOUNT> on your account <ACCOUNT> has been completed successfully"
CREATE TRIGGER message_trigger
AFTER INSERT ON transaction
FOR EACH ROW
BEGIN
    DECLARE message VARCHAR(255);
    DECLARE customer_name VARCHAR(255);
    DECLARE account_number VARCHAR(255);
    DECLARE amount DECIMAL(10,2);
-- Get the customer name
    SELECT name INTO customer_name FROM customer WHERE id = NEW.customer_id;
-- Get the account number
    SELECT account_number INTO account_number FROM account WHERE id = NEW.account_id;
-- Get the transaction type
    IF NEW.transaction_type = 'WITHDRAW' THEN
        SET message = CONCAT('Dear ', customer_name, ' Your WITHDRAW of ', NEW.amount, ' on your account ', account_number, ' has been completed successfully');
    ELSE
        SET message = CONCAT('Dear ', customer_name, ' Your SAVING of ', NEW.amount, ' on your account ', account_number, ' has been completed successfully');
    END IF;
-- Insert the message into the message table
    INSERT INTO message (customer_id, message, dateTime) VALUES (NEW.customer_id, message, NOW());
END //
DELIMITER ;