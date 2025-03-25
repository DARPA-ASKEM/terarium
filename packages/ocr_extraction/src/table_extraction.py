import os
import json
import base64
import logging
from io import BytesIO
from bs4 import BeautifulSoup
from PIL import Image
from openai import OpenAI

TABLE_EXTRACTION_PROMPT = """Here is the extracted table in html from the provided image.

The extracted result isn't perfect, but it's a good start. You know that the structure of the table is correct, but the extracted content of the each cells may not be accurate or be missing.

Your job is to verify the extracted table content with the provided image and correct any inaccuracies or missing data. Do not alter the structure, colspan, or rowspan of the table, only replace the cell text value with the correct data. Some cells may span multiple columns or rows which is normal and should be preserved. Also it's normal to have multiple header rows or columns. Do not remove any rows or columns from the table, just correct the cell text values. Number of rows and columns should be preserved.
Ensure that symbols, subscripts, superscripts, and greek characters are preserved, "α" should not be swapped to "a". Make sure symbols, greek characters, and mathematical expressions are preserved and represented correctly.

You will structure your response as a JSON object with the following schema:

'table_text': a XHTML formatted table string.
'score': A score from 0 to 10 indicating the quality of the corrected table. 0 indicates that the image does not contain a table, 10 indicates a high-quality extraction.

Begin:
"""


def convert_table_to_grid(html_table):
    soup = BeautifulSoup(html_table, 'html.parser')
    table = soup.find('table')

    # Determine the size of the table
    num_rows = len(table.find_all('tr'))
    num_cols = max(len(row.find_all(['td', 'th'])) for row in table.find_all('tr'))

    # Initialize the grid with empty strings
    grid = [['' for _ in range(num_cols)] for _ in range(num_rows)]

    for row_idx, row in enumerate(table.find_all('tr')):
        col_idx = 0
        for cell in row.find_all(['td', 'th']):
            while grid[row_idx][col_idx]:
                col_idx += 1

            rowspan = int(cell.get('rowspan', 1))
            colspan = int(cell.get('colspan', 1))
            cell_text = cell.get_text(strip=True)

            for i in range(rowspan):
                for j in range(colspan):
                    grid[row_idx + i][col_idx + j] = cell_text

            col_idx += colspan

    return grid


def image_to_base64_string(img: Image.Image) -> str:
    format = 'PNG'
    buffered = BytesIO()
    img.save(buffered, format=format)
    img_base64 = base64.b64encode(buffered.getvalue()).decode("utf-8")
    return f"data:image/{format.lower()};base64,{img_base64}"  # Return Data URI


def process_table_image(image_uri, table_html):
    openai_api_key = os.getenv("ASKEM_DOC_AI_API_KEY")
    if (openai_api_key is None):
        raise ValueError("ASKEM_DOC_AI_API_KEY not found in environment variables. Please set 'ASKEM_DOC_AI_API_KEY'.")

    client = OpenAI(api_key=openai_api_key)

    logging.info("Processing table image with GPT...")
    response = client.chat.completions.create(
        model="gpt-4o-2024-08-06",
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "image_url",
                        "image_url": {"url": image_uri},
                    },
                    {
                        "type": "text",
                        "text": table_html,
                    },
                    {"type": "text", "text": TABLE_EXTRACTION_PROMPT},
                ],
            }
        ],
        response_format={"type": "json_object"},
    )
    message_content = json.loads(response.choices[0].message.content)
    return message_content


def normalize_bbox(bbox, page_size: tuple[float, float]):
    (width, height) = page_size
    if bbox["coord_origin"] == "TOPLEFT":
        return {
            "left": bbox["l"] / width,
            "top": bbox["t"] / height,
            "right": bbox["r"] / width,
            "bottom": bbox["b"] / height
        }
    # Else, BottomLeft
    return {
        "left": bbox["l"] / width,
        "top": 1 - bbox["t"] / height,
        "right": bbox["r"] / width,
        "bottom": 1 - bbox["b"] / height
    }


def extract_tables(result):
    table_extraction_dict = {}
    for _idx, table in enumerate(result.document.tables):
        table_ref = table.self_ref
        # Page information
        page_size = result.document.pages[table.prov[0].page_no].size
        # Get the table image
        table_img = table.get_image(result.document)
        table_img = table_img.resize((table_img.width * 2, table_img.height * 2))
        docling_table_html = table.export_to_html()
        table_image_extract = process_table_image(image_to_base64_string(table_img), docling_table_html)
        html_table = table_image_extract["table_text"]

        num_rows = table.data.num_rows
        num_cols = table.data.num_cols
        table_grid = convert_table_to_grid(html_table)
        table_cells = []
        # If original table and gpt extracted table has same dimension, update each cell value with the extracted table data
        if (num_rows == len(table_grid) and num_cols == len(table_grid[0])):
            # update table cell values
            for table_cell in table.data.table_cells:
                row_idx = table_cell.start_row_offset_idx
                col_idx = table_cell.start_col_offset_idx
                cell_id = table_ref + ":" + str(row_idx) + "_" + str(col_idx)
                cell_val = str(table_grid[row_idx][col_idx])
                bbox = normalize_bbox({
                    "left": table_cell.bbox.l,
                    "top": table_cell.bbox.t,
                    "right": table_cell.bbox.r,
                    "bottom": table_cell.bbox.b,
                    "coord_origin": table_cell.bbox.coord_origin
                }, (page_size.width, page_size.height))
                cell_dict = vars(table_cell)
                cell_dict["id"] = cell_id  # Add table cell id
                cell_dict["bbox"] = bbox
                cell_dict["text"] = cell_val
                table_cells.append(cell_dict)

        table_extraction_dict[table_ref] = {
            "text": html_table,
            "data": {
                "num_rows": num_rows,
                "num_cols": num_cols,
                "table_cells": table_cells
            }
        }
        logging.info("Docling initial result: ")
        logging.info(docling_table_html)
        logging.info("GPT post processed result: ")
        logging.info(html_table)
    return table_extraction_dict
