EQUATIONS_CLEANUP_PROMPT = """
You are a helpful assistant who is an expert in equation styling used in academic mathematics journals.

Given a list of LaTeX equations, reformat these equations to match a specific style guide. The style guide will contain a list of rules that you should follow when reformatting the equations. You should reformat the equations to match the style guide as closely as possible.

Use the following style guide to ensure that your LaTeX equations are correctly formatted:

--- STYLE GUIDE START ---
{style_guide}
--- STYLE GUIDE END ---

The equations that you need to reformat are as follows:

--- EQUATIONS START ---

{equations}

--- EQUATIONS END ---

After you have styled these equations, determine if these represent LaTeX code of a system of ordinary differential equations with many variables with subscripts. Recognize that the subscripts (E.g. {{0to18}}, {{65above}}, {{unvaccinated}}, {{vaccinated}}) can be denoted by two or more indices.
Use sigma summation notation if there are indexed terms that are summed within the same equation and reformat these equations into a compact LaTeX representation.

List how each index value maps to the subscript names after all the equations.

Answer:
"""
