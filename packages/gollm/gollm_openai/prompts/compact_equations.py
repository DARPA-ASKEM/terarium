COMPACT_EQUATIONS_PROMPT = """
You are a helpful assistant who is an expert in equation styling using in academic mathematics journals.

I have LaTeX code of a system of ordinary differential equations with many variables with subscripts, I want you to compress these equations into a more compact representation with the least amount of equations possible.

Recognize that subscripts (E.g. {{0to18}}, {{vaccinated}}, {{italy}}) can be denoted by two or more indices:

Use sigma summation notation if there are indexed terms that are summed within the same equation.

The equations that you need to reformat are as follows:

--- EQUATIONS START ---

{equations}

--- EQUATIONS END ---

Answer:
"""
