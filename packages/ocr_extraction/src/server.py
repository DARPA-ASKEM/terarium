from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import logging
from io import BytesIO

logging.basicConfig(level=logging.DEBUG)

from docling.datamodel.base_models import DocumentStream
from docling_core.types.doc import ImageRefMode, PictureItem, TableItem, TextItem
from docling.datamodel.base_models import FigureElement, InputFormat, Table
from docling.datamodel.pipeline_options import PdfPipelineOptions
from docling.document_converter import DocumentConverter, PdfFormatOption

from texteller.inference_model import InferenceModel


app = FastAPI()
IMAGE_RESOLUTION_SCALE = 1.0
pipeline_options = PdfPipelineOptions()
pipeline_options.images_scale = IMAGE_RESOLUTION_SCALE
pipeline_options.generate_page_images = True
pipeline_options.generate_picture_images = False
pipeline_options.do_code_enrichment = False
pipeline_options.do_formula_enrichment = True

converter = DocumentConverter(
    format_options={
        InputFormat.PDF: PdfFormatOption(pipeline_options=pipeline_options)
    }
)


# Textteller equation image => latex
equation_model = InferenceModel()


@app.get("/health")
async def health_check():
    return JSONResponse(content={"status": "OK"}, status_code=200)


@app.post("/predict")
async def process_and_predict(file: UploadFile = File(...)):
    logging.info("In predict")
    file_bytes = await file.read()
    logging.info(f"Len = {len(file_bytes)}")
    docstream = DocumentStream(name="test", stream=BytesIO(file_bytes))
    # result = converter.convert(docstream, max_num_pages=100, max_file_size=20971520)
    result = converter.convert(docstream)

    ################################################################################
    # Do second pass
    # - Extract latext equation using TextTeller
    ################################################################################
    logging.info("Starting LaTeX extraction...")
    latex_extraction_dict = {}
    for _idx, element in enumerate(result.document.texts):
        if element.label == "formula":
            # logging.info(f"{element.label} =>  {element.text}")
            text_ref = element.self_ref
            text_img = element.get_image(result.document)

            text_img_byte_arr = BytesIO()
            text_img.save(text_img_byte_arr, format="PNG")

            latex_str = equation_model.predict(text_img_byte_arr.getvalue())

            logging.info(f"{text_ref}")
            logging.info(f"Docling model: {element.text}")
            logging.info(f"Textteller model: {latex_str}")
            logging.info("")
            latex_extraction_dict[text_ref] = latex_str


    ################################################################################
    # Collect and format result
    ################################################################################
    final_result = {}
    result_dict = result.document.export_to_dict()

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
            children.append(child["$ref"])

        final_result["groups"].append({
            "id": group["self_ref"],
            "children": children
        })

    # 3. Text
    final_result["texts"] = []
    for text in result_dict["texts"]:
        item = {}
        prov = text["prov"][0]

        item["id"] = text["self_ref"]
        item["label"] = text["label"]
        item["provenance"] = {
            "page": prov["page_no"],
            "bbox": {
                "left": prov["bbox"]["l"],
                "top": prov["bbox"]["t"],
                "right": prov["bbox"]["r"],
                "bottom": prov["bbox"]["b"]
            },
            "charspan": prov["charspan"]
        }

        if item["label"] == "formula":
            item["text"] = latex_extraction_dict[item["id"]]
        else:
            item["text"] = text["text"]

        final_result["texts"].append(item)

    # 4. Images
    final_result["pictures"] = []
    for picture in result_dict["pictures"]:
        item = {}
        prov = picture["prov"][0]
        item["id"] = picture["self_ref"]
        item["label"] = picture["label"]
        item["provenance"] = {
            "page": prov["page_no"],
            "bbox": {
                "left": prov["bbox"]["l"],
                "top": prov["bbox"]["t"],
                "right": prov["bbox"]["r"],
                "bottom": prov["bbox"]["b"]
            },
            "charspan": prov["charspan"]
        }
        final_result["pictures"].append(item)



    # 5. Tables ... TODO


    # result_dict = result.document.export_to_dict()
    # return JSONResponse(content=result_dict)
    # return JSONResponse(content={"status": "TODO"})
    return JSONResponse(content=final_result)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8002)
