MODEL_CARD_PROMPT = """
You are a helpful agent designed to populate metadata of a given AMR model.

You may have access to a document that describes the given AMR model and a JSON representation of the AMR model we want populated.
Do your best to populate the JSON object specified in the response format with as much information as possible.
If you cannot answer the entire query, provide as much information as possible. If there is no answer, populate fields with a blank values. Do not leave any fields empty and do not make up information.

Use the following document as a reference:

---DOCUMENT START---
{research_paper}
---DOCUMENT END--

Use the following JSON representation of an AMR model as a reference:

---MODEL START--
{amr}
---MODEL END---

Do not respond in full sentences; only create a JSON object that satisfies the JSON schema specified in the response format. Verify and format your answer against the JSON schema. Do not modify the JSON schema to fit your response.

Answer:
"""
