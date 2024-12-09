LATEX_STYLE_GUIDE = """
1) Derivatives must be written in Leibniz notation (for example, "\\frac{d X}{d t}")
    a) Derivatives that are written in other notations, like Newton ("\dot{X}") or Lagrange ("X^\prime" or "X'"), should be converted to Leibniz notation
    b) Partial derivatives of one-variable functions (for example, "\\partial_t X" or "\\frac{\partial X}{\partial t}") should be rewritten as ordinary derivatives ("\\frac{d X}{d t}")
2) First-order derivative must be on the left of the equal sign
3) All variables have time "t" dependence should be explicitly with "(t)" (for example, "X" should be written as "X(t)")
4) Rewrite superscripts and LaTeX superscripts "^" that denote indices to subscripts using LaTeX "_"
5) Replace any unicode subscripts with LaTeX subscripts using "_". Ensure that all characters used in the subscript are surrounded by a pair of curly brackets "{...}"
6) Avoid parentheses
7) Avoid capital sigma and pi notations for summation and product
8) Avoid non-ASCII characters when possible
9) Avoid using homoglyphs
10) Avoid words or multi-character names for variables and names. Use camel case to express multi-word or multi-character names
11) Do not use "\\cdot" or "*" to indicate multiplication. Use whitespace instead.
12) Replace "\\epsilon" with "\\varepsilon" when representing a parameter or variable
13) If equations are separated by commas, do not include commas in the LaTeX code.
"""
