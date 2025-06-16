# UnravelDocs API - Document Management

API endpoints for managing documents in UnravelDocs.

## Base URL

The base URL for all API endpoints is:
`http://localhost:8080/api/v1`

(This is derived from `app.base.url` in your `application.properties` and the `@RequestMapping("/api/v1/documents")` on the controller).

## Authentication

All endpoints under `/api/v1/documents/` require authentication.
The API uses **Bearer Token (JWT)** authentication.
You must include an `Authorization` header with your JWT token:

`Authorization: Bearer <your_jwt_token>`

## Endpoints

### Documents

Endpoints for document management including upload and deletion.

---

#### 1. Upload a document

Allows authenticated users to upload documents to the system. The file is sent as multipart/form-data.

*   **Method:** `POST`
*   **Endpoint:** `/documents/upload`
*   **Operation ID:** `uploadDocument`
*   **Authentication:** Required (Bearer Token)

**Request Body:**

*   `Content-Type: multipart/form-data`
*   **Form Data:**
    *   `file`: (file) The document file to upload. (Required)
        *   Allowed file types are configured in `app.document.allowed-file-types` (e.g., `image/jpeg`, `image/png`, `image/jpg`).
        *   Maximum file size is configured in `spring.servlet.multipart.max-file-size` (e.g., `10MB`).

**Responses:**

*   **`200 OK`**: Document uploaded successfully.
    *   **Content-Type:** `application/json`
    * **Body:** `DocumentUploadResponse`
      ```json
      {
        "statusCode": 200,
        "status": "success",
        "message": "Document uploaded successfully.",
        "data": {
          "documentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
          "originalFileName": "mydocument.pdf",
          "status": "UPLOADED",
          "fileUrl": "https://cloudinary.com/unraveldocs/documents/uuid-mydocument.pdf",
          "fileSize": 1024768
        }
      }
      ```

*   **`400 Bad Request`**: Invalid input, such as an empty file, unsupported file type, or missing file.
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`
        ```json
        {
          "statusCode": 400,
          "error": "Bad Request",
          "message": "File is empty or not provided",
          "timestamp": "2023-01-01T12:00:00Z",
          "path": "/api/v1/documents/upload"
        }
        ```

*   **`401 Unauthorized`**: Authentication token is missing or invalid.
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`

*   **`403 Forbidden`**: User is authenticated but not authorized (e.g., user not found in DB after authentication, or general "You must be logged in..." if authentication principal is null).
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`

*   **`500 Internal Server Error`**: An unexpected error occurred on the server (e.g., failure to upload to Cloudinary).
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`

---

#### 2. Delete a document

Allows authenticated users to delete their uploaded documents by providing the document ID.

*   **Method:** `DELETE`
*   **Endpoint:** `/documents/{documentId}`
*   **Operation ID:** `deleteDocument`
*   **Authentication:** Required (Bearer Token)

**Path Parameters:**

*   `documentId` (string, UUID, required): The unique identifier of the document to delete.
    *   Example: `a1b2c3d4-e5f6-7890-1234-567890abcdef`

**Responses:**

*   **`204 No Content`**: Document deleted successfully. No content returned.

*   **`401 Unauthorized`**: Authentication token is missing or invalid.
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`

*   **`403 Forbidden`**: User is authenticated but not authorized to delete this specific document or user not found.
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`
        ```json
        {
          "statusCode": 403,
          "error": "Forbidden",
          "message": "You are not authorized to delete this document.",
          "timestamp": "2023-01-01T12:00:00Z",
          "path": "/api/v1/documents/a1b2c3d4-e5f6-7890-1234-567890abcdef"
        }
        ```

*   **`404 Not Found`**: The document with the specified ID was not found.
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`
        ```json
        {
          "statusCode": 404,
          "error": "Not Found",
          "message": "Document not found with ID: a1b2c3d4-e5f6-7890-1234-567890abcdef",
          "timestamp": "2023-01-01T12:00:00Z",
          "path": "/api/v1/documents/a1b2c3d4-e5f6-7890-1234-567890abcdef"
        }
        ```

*   **`500 Internal Server Error`**: An unexpected error occurred on the server (e.g., failure to delete from storage).
    *   **Content-Type:** `application/json`
    *   **Body:** `ErrorResponse`

---

## Schemas

### DocumentUploadData
```json
{
  "documentId": "string (uuid)",
  "originalFileName": "string",
  "status": "string (UPLOADED, PROCESSING, PROCESSED, FAILED_OCR, DELETED)",
  "fileUrl": "string (url)",
  "fileSize": "integer (int64)"
}
```
### DocumentUploadResponse
```json
{
  "statusCode": 200,
  "status": "string (success, error)",
  "message": "string",
  "data": {
    "documentId": "string (uuid)",
    "originalFileName": "string",
    "status": "string (UPLOADED, PROCESSING, PROCESSED, FAILED_OCR, DELETED)",
    "fileUrl": "string (url)",
    "fileSize": "integer (int64)"
  }
}
```
### ErrorResponse
```json
{
  "statusCode": 400,
  "error": "string (Bad Request, Unauthorized, Forbidden, Not Found)",
  "message": "string",
  "timestamp": "string (ISO 8601 date-time)",
  "path": "string (request path)"
}
```