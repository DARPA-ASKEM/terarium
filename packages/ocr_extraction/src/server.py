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


app = FastAPI()
IMAGE_RESOLUTION_SCALE = 2.0
pipeline_options = PdfPipelineOptions()
pipeline_options.images_scale = IMAGE_RESOLUTION_SCALE
pipeline_options.generate_page_images = True
pipeline_options.generate_picture_images = False
pipeline_options.do_code_enrichment = True
pipeline_options.do_formula_enrichment = True


converter = DocumentConverter(
    format_options={
        InputFormat.PDF: PdfFormatOption(pipeline_options=pipeline_options)
    }
)


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
    result_dict = result.document.export_to_dict()
    return JSONResponse(content=result_dict)
    # return JSONResponse(content={"status": "TODO"})


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8002)
