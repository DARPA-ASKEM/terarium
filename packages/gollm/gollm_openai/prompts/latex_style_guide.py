LATEXT_STYLE_GUIDE = """
1) Derivatives must be written in Leibniz notation (for example, \\frac{d X}{d t}). 
    a) Derivatives that are written in other notations, like Newton (\dot{X}) or Lagrange (X^\prime or X'), should be converted to Leibniz notation
    b) Partial derivatives of one-variable functions (for example, \\partial_t X or \\frac{\partial X}{\partial t}) should be rewritten as ordinary derivatives (\\frac{d X}{d t})
2) First-order derivative must be on the left of the equal sign
3) Use whitespace to indicate multiplication
    a) "*" is optional but should be avoided
4) "(t)" is optional and should be avoided
5) Rewrite superscripts and LaTeX superscripts "^" that denote indices to subscripts using LaTeX "_"
6) Replace any unicode subscripts with LaTeX subscripts using "_"
    a) Ensure that all characters used in the subscript are surrounded by a pair of curly brackets "{...}"
7) Avoid mathematical constants like pi or Euler's number
    a) Replace them as floats with 3 decimal places of precision
8) Avoid parentheses
9) Avoid capital sigma and pi notations for summation and product
10) Avoid non-ASCII characters when possible
11) Avoid using homoglyphs
12) Avoid words or multi-character names for variables and names
    a) Use camel case to express multi-word or multi-character names
13) Do not use \\cdot for multiplication. Use whitespace instead.
14) Replace \\epsilon with \\varepsilon when representing a parameter or variable
"""
