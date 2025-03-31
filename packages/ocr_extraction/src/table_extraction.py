import logging
from src.llm_tools import LlmToolsInterface
from src.utils import image_to_base64_string, convert_table_to_grid, normalize_bbox


def extract_tables(result, llmTools: LlmToolsInterface):
    table_extraction_dict = {}
    for _idx, table in enumerate(result.document.tables):
        table_ref = table.self_ref
        # Page information
        page_size = result.document.pages[table.prov[0].page_no].size
        # Get the table image
        table_img = table.get_image(result.document)
        table_img = table_img.resize((table_img.width * 2, table_img.height * 2))
        docling_table_html = table.export_to_html()
        table_image_extract = llmTools.enhance_table_extraction(image_to_base64_string(table_img), docling_table_html)
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
                    "l": table_cell.bbox.l,
                    "t": table_cell.bbox.t,
                    "r": table_cell.bbox.r,
                    "b": table_cell.bbox.b,
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
        logging.info("LLM enhanced result: ")
        logging.info(html_table)
    return table_extraction_dict
