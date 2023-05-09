from pdfminer.high_level import extract_text as extract_text_pdfminer
import PyPDF2
import fitz
import base64
import logging
import json


def dataframes_to_json(dataframes):
    data = []
    """
    This function converts a list of pandas dataframes to a JSON string.

    :param dataframes: a list of pandas dataframes that need to be converted to JSON format
    :return: a JSON string representation of a list of dictionaries, where each dictionary represents a
    row of data from one of the input dataframes.
    """
    data = []
    for df in dataframes:
        data += df.to_dict(orient="records")
    return json.dumps(data)


def extract_text(pdf_path, method="pypdf2"):
    """
    Extract text from a PDF using the specified method.
    :param pdf_path: Path to the PDF file
    :param method: Text extraction method ('pypdf2', 'pdfminer', 'pymupdf')
    :return: Generator yielding the extracted text
    """
    if method == "pypdf2":
        with open(pdf_path, "rb") as f:
            reader = PyPDF2.PdfFileReader(f)
            for page in range(reader.getNumPages()):
                text = reader.getPage(page).extractText()
                yield text.encode("utf-8")
    elif method == "pdfminer":
        with open(pdf_path, "rb") as f:
            for page_text in extract_text_pdfminer(f):
                yield page_text.encode("utf-8")
    elif method == "pymupdf":
        with fitz.open(pdf_path) as doc:
            for page in doc:
                text = page.getText()
                yield text.encode("utf-8")


def extract_images(pdf_path, method="pymupdf"):
    """
    Extract images from a PDF using the specified method.
    :param pdf_path: Path to the PDF file
    :param method: Image extraction method ('pymupdf')
    :return: List of image file paths
    """
    if method == "pymupdf":
        doc = fitz.open(pdf_path)
        img_files = []

        for page_num in range(doc.page_count):
            page = doc.load_page(page_num)
            image_list = page.get_images(full=True)

            for img_index, img in enumerate(image_list):
                xref = img[0]
                base_image = doc.extract_image(xref)
                image_bytes = base_image["image"]

                img_ext = base_image["ext"]
                img_file_path = f"image{page_num}_{img_index}.{img_ext}"
                img_files.append(img_file_path)

                with open(img_file_path, "wb") as f:
                    f.write(image_bytes)

        return img_files
    else:
        return []


def extract_images_base64(pdf_path):
    """
    This function extracts all images from a PDF file and returns them as a list of base64 encoded
    strings.

    :param pdf_path: The file path of the PDF file from which images need to be extracted
    :return: a list of base64 encoded strings representing the images extracted from the PDF file
    located at the path specified by the `pdf_path` parameter.
    """
    images_base64 = []
    doc = fitz.open(pdf_path)

    for page_num in range(doc.page_count):
        page = doc.load_page(page_num)
        image_list = page.get_images(full=True)

        for img_index, img in enumerate(image_list):
            base_image = doc.extract_image(img[0])
            image_data = base_image["image"]
            image_base64 = base64.b64encode(image_data).decode("utf-8")
            images_base64.append(image_base64)

    return images_base64


# def extract_tables_from_pdf(file_path):
#     tables = camelot.read_pdf(file_path, flavor="stream", pages="all", metadata=True)
#     for i, table in enumerate(tables):

#         # Get the metadata associated with the table
#         if "tables" in table.parsing_report:
#             metadata = table.parsing_report["tables"][i]["page"][0][
#                 "extraction_method"
#             ]["metadata"]
#             table_desc = metadata.get("table_description", "No table description found")

#         else:
#             print(f"No tables found on page {i+1}")
#     return [table.df for table in tables]


# def dataframe_to_json(df):
#     data = df.to_dict(orient='records')
#     return json.dumps(data)

# tables = extract_tables_from_pdf("foo.pdf")
# print(dataframes_to_json(tables))
