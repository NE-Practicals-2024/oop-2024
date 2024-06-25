-- Ensure the uuid-ossp extension is installed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the trigger function
CREATE OR REPLACE FUNCTION message_trigger_function()
    RETURNS TRIGGER AS
$$
DECLARE
    message        TEXT;
    customer_name  TEXT;
    account_number TEXT;
    message_uuid   UUID;
    receiver       customers%ROWTYPE;
BEGIN
    -- Generate a new UUID
    message_uuid := uuid_generate_v4();

    -- Get the customer first name and last name into a single variable
    SELECT CONCAT(first_name, ' ', last_name) INTO customer_name FROM customers WHERE id = NEW.customer_id;
    -- Get Receiver
    SELECT * INTO receiver FROM customers WHERE id = NEW.sent_to_id;

    -- Get the account number
    SELECT account INTO account_number FROM customers WHERE id = NEW.customer_id;

    -- Get the transaction type and construct the message
    IF NEW.type = 'WITHDRAW' THEN
        message := 'Dear ' || customer_name || ' Your WITHDRAW of ' || NEW.amount || ' on your account ' ||
                   account_number || ' has been completed successfully. Transaction ID: ' || message_uuid;
    ELSIF NEW.type = 'SAVING' THEN
        message := 'Dear ' || customer_name || ' Your SAVING of ' || NEW.amount || ' on your account ' ||
                   account_number || ' has been completed successfully. Transaction ID: ' || message_uuid;
    ELSEIF NEW.type = 'TRANSFER' THEN
        message := 'Dear ' || customer_name || ' Your TRANSFER of ' || NEW.amount || ' to ' || receiver.first_name ||
                   ' ' || receiver.last_name ||
                   ' has been completed successfully. Transaction ID: ' || message_uuid;
    END IF;

    -- Insert the message into the message table
    INSERT INTO messages(id, customer_id, message, date_time) VALUES (message_uuid, NEW.customer_id, message, NOW());

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger to use the function
CREATE TRIGGER message_trigger
    AFTER INSERT
    ON banking_transactions
    FOR EACH ROW
EXECUTE FUNCTION message_trigger_function();
