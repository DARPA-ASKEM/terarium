import base64
from io import BytesIO
from bs4 import BeautifulSoup
from PIL import Image

def convert_table_to_grid(html_table: str):
    """
    Convert a HTML string of a table to a 2D grid of string value of the cells.
    """
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


def resize_image(image_base64: str, target_size=(1120, 1120)) -> str:
    """
    Resize an image in base64 format to a given size.
    """
    img = Image.open(BytesIO(base64.b64decode(image_base64)))
    img = img.resize(target_size) # max image size for the model
    buffered = BytesIO()
    img.save(buffered, format="PNG")
    img_base64 = base64.b64encode(buffered.getvalue()).decode("utf-8")
    return img_base64


def image_to_base64_string(img: Image.Image) -> str:
    format = 'PNG'
    buffered = BytesIO()
    img.save(buffered, format=format)
    img_base64 = base64.b64encode(buffered.getvalue()).decode("utf-8")
    return f"data:image/{format.lower()};base64,{img_base64}"  # Return Data URI


def normalize_bbox(bbox: dict, page_size: tuple[float, float]):
    """
    Normalize a docling bounding box to the range [0, 1].
    """
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
