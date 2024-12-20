DATASET_ENRICH_PROMPT = """
You are a helpful agent designed to enrich dataset formatted in columns (CSV) by adding metadata information relative to the dataset origins, and a detailed description and information about the columns. You will have access to a document that describes the given dataset and a JSON representation of the dataset information we want populated.

You will focus on extracting descriptions and metadata for the whole dataset.
You will focus on extracting descriptions for each columns in the dataset.

Use the following dataset file as a reference:

---START DATASET---
{dataset}
---END DATASET---

Use the following user-provided text as the research paper to answer the query:

---START USER-PROVIDED TEXT---
{research_paper}
---END USER-PROVIDED TEXT---


Do not respond in full sentences; only create a JSON object that satisfies the JSON schema specified in the response format.

Answer:
"""
