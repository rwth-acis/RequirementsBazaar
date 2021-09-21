# File Upload

## Implications

Uploads and references in the database are separate operations. This may lead to incomplete and unreferenced uploads.
This is a tradeoff made in favour of having multipart API endpoints.

A way to resolve this is to regularly synchronize the file index with the references in database.

## Workflows

### Upload with requirement creation

1. Upload the file to the file service and receive or generate an identifier.
2. Include reference to the uploaded file as a regular attachment object in the attachment array. Then send this to the
   create requirement endpoint.

### Add attachments to an existing requirement

1. Upload the file to the file service and receive or generate an identifier.
2. Add reference to the uploaded file as a regular attachment object to the attachment array and use the requirements
   update endpoint.

### Remove attachments from an existing requirement

1. Remove the reference in the attachments array and use the update requirement endpoint.
