import logging
import json
from fastapi import FastAPI, File, UploadFile, Form
from fastapi.responses import JSONResponse
from io import BytesIO

from docling.datamodel.base_models import DocumentStream, InputFormat
from docling.datamodel.pipeline_options import PdfPipelineOptions, TableFormerMode, RapidOcrOptions, AcceleratorDevice, AcceleratorOptions
from docling.document_converter import DocumentConverter, PdfFormatOption

from docling.datamodel.settings import settings
settings.debug.profile_pipeline_timings = True

from texteller.inference_model import InferenceModel

from src.table_extraction import extract_tables, normalize_bbox
from src.llm_tools import get_llm_tools

logging.basicConfig(level=logging.INFO)

extractor = "docling"

app = FastAPI()
IMAGE_RESOLUTION_SCALE = 2.0
pipeline_options = PdfPipelineOptions()
pipeline_options.images_scale = IMAGE_RESOLUTION_SCALE
pipeline_options.generate_page_images = True
pipeline_options.generate_picture_images = False
pipeline_options.do_code_enrichment = False
pipeline_options.do_formula_enrichment = True

pipeline_options.do_table_structure = True
pipeline_options.table_structure_options.do_cell_matching = True
pipeline_options.table_structure_options.mode = TableFormerMode.ACCURATE
pipeline_options.do_ocr = True
pipeline_options.ocr_options = RapidOcrOptions(force_full_page_ocr=True)

accelerator_options = AcceleratorOptions(
    num_threads=8, device=AcceleratorDevice.AUTO
)
pipeline_options.accelerator_options = accelerator_options


converter = DocumentConverter(
    format_options={
        InputFormat.PDF: PdfFormatOption(pipeline_options=pipeline_options)
    }
)

equation_model = InferenceModel()


@app.get("/health")
async def health_check():
    return JSONResponse(content={"status": "OK"}, status_code=200)


@app.post("/predict")
async def process_and_predict(file: UploadFile = File(...), llm_model: str = Form('azure')):
    llm_tools = get_llm_tools(llm_model)
    logging.info(f"LLM model set to {llm_tools.name()}")
    logging.info("In predict")
    file_bytes = await file.read()
    logging.info(f"File length = {len(file_bytes)}")

    filename = "document.txt"
    if file.filename is not None:
        filename = file.filename
    docstream = DocumentStream(name=filename, stream=BytesIO(file_bytes))
    result = converter.convert(docstream)

    logging.info("Done docling extraction")
    logging.info("Docling extraction metrics")
    for key, value in result.timings.items():
        try:
            logging.info(f"{key}: {json.dumps(value)}")
        except Exception:
            logging.info(f"{key}={value.scope} avg={value.avg()}")
    logging.info("")


    ################################################################################
    # Do second pass
    # - Extract latext equation using TextTeller
    ################################################################################
    logging.info("Starting LaTeX extraction...")
    latex_extraction_dict = {}
    for _idx, element in enumerate(result.document.texts):
        if element.label == "formula":
            text_ref = element.self_ref
            text_img = element.get_image(result.document)

            text_img_byte_arr = BytesIO()
            text_img.save(text_img_byte_arr, format="PNG")

            latex_str = equation_model.predict(text_img_byte_arr.getvalue())

            # logging.info(f"{text_ref}")
            # logging.info(f"Docling model: {element.text}")
            # logging.info(f"Textteller model: {latex_str}")
            # logging.info("")
            latex_extraction_dict[text_ref] = latex_str

    # - Extract tables using GPT model
    logging.info("Starting table extraction...")
    table_extraction_dict = extract_tables(result, llm_tools)

    ################################################################################
    # Collect and format result
    ################################################################################
    final_result = {}
    final_result["extractedBy"] = extractor
    result_dict = result.document.export_to_dict()


    # 0. Pages
    final_result["pages"] = {}
    for key, value in result_dict["pages"].items():
        final_result["pages"][key] = {
            "page": value["page_no"],
            "size": {
                "width": value["size"]["width"],
                "height": value["size"]["height"]
            }
        }


    # 1. Body
    final_result["body"] = {
        "id": "#/body",
        "children": []
    }
    for child in result_dict["body"]["children"]:
        final_result["body"]["children"].append(
            {"id": child["$ref"]}
        )

    # 2. Groups
    final_result["groups"] = []
    for group in result_dict["groups"]:
        children = []
        for child in group["children"]:
            children.append(
                { "id": child["$ref"] }
            )

        final_result["groups"].append({
            "id": group["self_ref"],
            "children": children
        })


    pages = final_result["pages"]

    # 3. Text
    final_result["extractions"] = []
    for text in result_dict["texts"]:
        item = {}

        if len(text["prov"]) > 0:
            prov = text["prov"][0]
            item["page"] = prov["page_no"]
            item["pageWidth"] = pages[str(item["page"])]["size"]["width"]
            item["pageHeight"] = pages[str(item["page"])]["size"]["height"]
            item["bbox"] = normalize_bbox(prov["bbox"], (item["pageWidth"], item["pageHeight"]))
            item["charspan"] = prov["charspan"]
        else:
            item["page"] = 0
            item["pageWidth"] = 0
            item["pageHeight"] = 0
            item["bbox"] = { "left": 0, "top": 0, "left": 0, "bottom": 0 }
            item["charspan"] = [0]


        item["id"] = text["self_ref"]
        item["type"] = "text"
        item["subType"] = text["label"]
        item["extractedBy"] = extractor


        if item["subType"] == "formula":
            item["rawText"] = latex_extraction_dict[item["id"]]
            item["text"] = latex_extraction_dict[item["id"]]
        else:
            item["rawText"] = text["text"]
            item["text"] = text["text"]

        final_result["extractions"].append(item)

    # 4. Images
    for picture in result_dict["pictures"]:
        item = {}

        if len(picture["prov"]) > 0:
            prov = picture["prov"][0]
            item["page"] = prov["page_no"]
            item["pageWidth"] = pages[str(item["page"])]["size"]["width"]
            item["pageHeight"] = pages[str(item["page"])]["size"]["height"]
            item["bbox"] = normalize_bbox(prov["bbox"], (item["pageWidth"], item["pageHeight"]))
            item["charspan"] = prov["charspan"]
        else:
            item["page"] = 0
            item["pageWidth"] = 0
            item["pageHeight"] = 0
            item["bbox"] = { "left": 0, "top": 0, "left": 0, "bottom": 0 }
            item["charspan"] = [0]

        item["id"] = picture["self_ref"]
        item["type"] = "picture"
        item["subType"] = picture["label"]
        item["extractedBy"] = extractor

        final_result["extractions"].append(item)

    # 5. Tables
    for table in result_dict["tables"]:
        id = table["self_ref"]
        item = {}

        if len(table["prov"]):
            prov = table["prov"][0]
            item["page"] = prov["page_no"]
            item["pageWidth"] = pages[str(item["page"])]["size"]["width"]
            item["pageHeight"] = pages[str(item["page"])]["size"]["height"]
            item["bbox"] = normalize_bbox(prov["bbox"], (item["pageWidth"], item["pageHeight"]))
            item["charspan"] = prov["charspan"]
        else:
            item["page"] = 0
            item["pageWidth"] = 0
            item["pageHeight"] = 0
            item["bbox"] = { "left": 0, "top": 0, "left": 0, "bottom": 0 }
            item["charspan"] = [0]

        item["id"] = id
        item["type"] = "table"
        item["subType"] = table["label"]
        item["extractedBy"] = extractor

        item["rawText"] = ""
        item["text"] = table_extraction_dict[id]["text"]
        item["data"] = table_extraction_dict[id]["data"]
        final_result["extractions"].append(item)

    return JSONResponse(content=final_result)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8002)
