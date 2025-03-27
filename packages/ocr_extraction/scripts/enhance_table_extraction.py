import logging
from src.llm_tools import get_llm_tools
from src.utils import image_to_base64_string
from PIL import Image

logging.basicConfig(level=logging.INFO)

# This is the test script to test llm tools without running the entire docling pipeline
# To run the script, from the package root, ocr_extraction/ run `python3 -m scripts.enhance_table_extraction`

llm_tools = get_llm_tools('llama')
# llm_tools = get_llm_tools('openai')
# llm_tools = get_llm_tools('azure')

with open('scripts/test_table_initial_extract.html', 'rb') as table_html:
    docling_table_html = table_html.read().decode('utf-8')

image = Image.open('scripts/test_table_imag.png')

result = llm_tools.enhance_table_extraction(image_to_base64_string(image), docling_table_html)
print(result["table_text"])
