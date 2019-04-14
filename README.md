# maximos3integration
Maximo Document Attachment S3 Integration


mxe.s3.accesskey = S3 Access key
mxe.s3.bucket = S3 Bucket Name where files get stored
mxe.s3.pseudo.pathprefix = Path prefix like /home/doclinks, same should be in path01 property
mxe.s3.region = S3 region like us-east-1
mxe.s3.secretkey = S3 secret key

mxe.attachmentstorage = fully qualified class name (com.aniruddh.s3.doclinks.S3AttachmentStorage) extending com.ibm.tivoli.maximo.oslc.provider.AttachmentStorage

S3AttachmentStorage class is called whenever an attachment is added, viewed or removed.

This class is tasked with adding attachments and serving them from S3 bucket.
