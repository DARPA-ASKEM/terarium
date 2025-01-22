LATEX_STYLE_GUIDE = """
1) Derivatives must be written in Leibniz notation (for example, "\\frac{{d X}}{{d t}}")
    a) Derivatives that are written in other notations, like Newton ("\\dot{{X}}") or Lagrange ("X^\\prime" or "X'"), should be converted to Leibniz notation
    b) Partial derivatives of one-variable functions (for example, "\\partial_t X" or "\\frac{{\\partial X}}{{\\partial t}}") should be rewritten as ordinary derivatives ("\\frac{{d X}}{{d t}}")
2) First-order derivative must be on the left of the equal sign
3) All variables that have time "t" dependence should be written with an explicit "(t)" (for example, "X" should be written as "X(t)")
4) Rewrite superscripts and LaTeX superscripts "^" that denote indices to subscripts using LaTeX "_"
5) Replace any unicode subscripts with LaTeX subscripts using "_". Ensure that all characters used in the subscript are surrounded by a pair of curly brackets "{{...}}"
6) Do not use square brackets "[ ]", curly braces "{ }", and angle brackets "< >" when grouping algebraic expressions; use parentheses "( )" when necessary
7) Expand algebraic expressions grouped by parentheses according to distributivity property of multiplication; example 1: "S(t) * (a * I(t) + b * D(t) + c * A(t))" -> "a * S(t) * I(t) + b * S(t) * D(t) + c * A(t) * S(t)"; example 2: "(a + b) * R(t)" -> "a * R(t) + b * R(t)"
8) Avoid capital sigma and pi notations for summation and product
9) Avoid non-ASCII characters when possible
10) Avoid using homoglyphs
11) Avoid words or multi-character names for variables and names. Use camel case to express multi-word or multi-character names
12) Use " * " to denote multiplication; for example, "b S(t) I(t)" -> "b * S(t) * I(t)"
13) Replace any variant form of Greek letters to their main form when representing a variable or parameter; "\\varepsilon" -> "\\epsilon", "\\vartheta" -> "\\theta", "\\varpi" -> "\\pi", "\\varrho" -> "\\rho",  "\\varsigma" -> "\\sigma", "\\varphi" -> "\\phi"
14) If equations are separated by punctuation (like comma, period, semicolon), do not include the punctuation in the LaTeX code.
15) Rewrite expressions with negative exponents as explicit fractions; for example, "N^{{-1}}" should be rewritten as "\\frac{{1}}{{N}}"
"""
