CREATE OR REPLACE FUNCTION message_trigger_function()
    RETURNS TRIGGER AS
$$
DECLARE
    message        TEXT;
    customer_name  TEXT;
    account_number TEXT;
BEGIN
    -- Get the customer first name and last name into a single variable
    SELECT CONCAT(first_name, ' ', last_name) INTO customer_name FROM customers WHERE id = NEW.customer_id;

    -- Get the account number
    SELECT account INTO account_number FROM customers WHERE id = NEW.customer_id;

    -- Get the transaction type and construct the message
    IF NEW.transaction_type = 'WITHDRAW' THEN
        message := 'Dear ' || customer_name || ' Your WITHDRAW of ' || NEW.amount || ' on your account ' ||
                   account_number || ' has been completed successfully';
        -- when transaction \is SAVING
    ELSIF NEW.transaction_type = 'SAVING' THEN
        message := 'Dear ' || customer_name || ' Your SAVING of ' || NEW.amount || ' on your account ' ||
                   account_number || ' has been completed successfully';
    END IF;

    -- Insert the message into the message table
    INSERT INTO messages (customer_id, message, date_time) VALUES (NEW.customer_id, message, NOW());

    CREATE TRIGGER message_trigger
        AFTER INSERT
        ON banking_transaction
        FOR EACH ROW
    EXECUTE FUNCTION message_trigger_function();

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
