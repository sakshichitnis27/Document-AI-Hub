-- Migration script to add user_id to existing documents
-- Run this script after starting the application (which will add the user_id column)

-- Step 1: Check existing documents without user_id
SELECT id, original_file_name, uploaded_at
FROM documents
WHERE user_id IS NULL;

-- Step 2: Check existing users and their IDs
SELECT id, email, created_at
FROM users
ORDER BY created_at;

-- Step 3: Assign existing documents to a specific user
-- REPLACE <USER_ID> with the ID of the user who should own these documents
-- For example, if sakshi.chitnis23@gmail.com has id=1, use:
-- UPDATE documents SET user_id = 1 WHERE user_id IS NULL;

-- Uncomment and modify the line below:
-- UPDATE documents SET user_id = <USER_ID> WHERE user_id IS NULL;

-- Step 4: Verify all documents have a user_id
SELECT
    COUNT(*) as total_documents,
    COUNT(user_id) as documents_with_user,
    COUNT(*) - COUNT(user_id) as documents_without_user
FROM documents;

-- Step 5 (Optional): Make user_id NOT NULL after all documents are assigned
-- This step should only be done after confirming all documents have a user_id
-- ALTER TABLE documents MODIFY COLUMN user_id BIGINT NOT NULL;
