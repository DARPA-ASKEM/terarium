LATEX_STYLE_GUIDE = """
1) Derivatives must be written in Leibniz notation (for example, "\\frac{{d X}}{{d t}}")
    a) Derivatives that are written in other notations, like Newton ("\\dot{{X}}") or Lagrange ("X^\\prime" or "X'"), should be converted to Leibniz notation
    b) Partial derivatives of one-variable functions (for example, "\\partial_t X" or "\\frac{{\\partial X}}{{\\partial t}}") should be rewritten as ordinary derivatives ("\\frac{{d X}}{{d t}}")
2) First-order derivative must be on the left of the equal sign
3) All variables that have time "t" dependence should be written with an explicit "(t)" (for example, "X" should be written as "X(t)")
4) Rewrite superscripts and LaTeX superscripts "^" that denote indices to subscripts using LaTeX "_"
5) Replace any unicode subscripts with LaTeX subscripts using "_". Ensure that all characters used in the subscript are surrounded by a pair of curly brackets "{{...}}"
6) Use " * " to denote multiplication
    a) replace "b S(t) I(t)" with "b * S(t) * I(t)"
    b) replace "b \\ast S(t) \ast I(t)" with "b * S(t)"
    c) replace "b \\star S(t)" with "b * S(t)"
    d) replace "b \times S(t)" with "b * S(t)"
    e) replace "(a b)S(t) with "a * b * S(t)"
7) Avoid capital sigma and pi notations for summation and product
8) Avoid non-ASCII characters when possible
9) Avoid using homoglyphs
10) Avoid words or multi-character names for variables and names. Use camel case to express multi-word or multi-character names
11) Replace any variant form of Greek letters to their main form when representing a variable or parameter 
    a) replace "\\varepsilon" with "\\epsilon"
    b) replace "\\vartheta" with "\\theta"
    c) replace "\\varpi" with "\\pi"
    d) replace "\\varrho" with "\\rho"
    e) replace "\\varsigma" with "\\sigma"
    f) replace "\\varphi" with "\\phi"
12) If equations are separated by punctuation (like comma, period, semicolon), do not include the punctuation in the LaTeX code.
13) Rewrite divisions to use "\\frac{{ }}{{ }}"
    a) replace "x \div y" with "\\frac{{x}}{{y}}"
    b) replace "x ÷ y" with "\\frac{{x}}{{y}}"
    c) replace "x / y" with "\\frac{{x}}{{y}}"
14) Rewrite expressions with negative exponents as explicit fractions
    a) replace "N^{{-1}}" with "\\frac{{1}}{{N}}"
15) Do not use square brackets "[ ]", curly braces "{ }", and angle brackets "< >" when grouping expressions
16) Expand expressions surrounded by parentheses using the order of mathematical operations
    a) for example, replace "x(t) (\\alpha y(t) + \\beta z(t))" with "\\alpha * x(t) * y(t) + \\beta * x(t) * z(t)"
    b) for example, replace "-(\\alpha + \\beta + \\gamma) x(t)" with "- \\alpha * x(t) - \\beta * x(t) - \\gamma * x(t)"
    c) for example, replace "(x(t) + y(t)) (z(t) + w(t))" with "x(t) * z(t) + x(t) * w(t) + y(t) * z(t) + y(t) * w(t)"
17) Do not expand expressions of the form "(1 - a * b)" where "a" and "b" are some constant parameters
    a) these expressions represent branching ratios
    b) for example, preserve "(1 - \\alpha) \\gamma I(t)" as "(1 - \\alpha) * \\gamma * I(t)"
    c) for example, preserve "(1 - \\gamma_1 - \\gamma_2) I(t)" as "(1 - \\gamma_1 - \\gamma_2) * I(t)"
18) Write named mathematical functions and operators using their corresponding LaTeX code
    a) replace "sin(\\omega t)" with "\\sin{{(\\omega * t)}}"
    b) replace "cos", "tan", "csc", "sec", "cot, sinh, cosh, tanh, csch, sech, coth" with "\\cos", "\\tan", "\\csc", "\\cot", "\\csc", "\\sinh", "\\cosh", "\\tanh", "\\csch", "\\coth", "\\csc" similarly
    c) replace "ln(a x + b)" or "log(a x + b)" with "\\log {{(a * x + b)}}"
    d) replace "log_2 (a x + b)" with "\\log_2 {{(a * x + b)}}"
    e) replace "exp(-a t)", "e^(-a t)", or "\\mathrm{e}^(-a t)" with "\\exp{{(-a * t)}}"
    f) replace "max(x(t))", "min(x(t))" with "\\max{{(x(t))}}", "\\min{{(x(t))}}"
    g) for other named functions like "CustomFunc(a * x)", write "\operatorname{{CustomFunc}}{{(a * x)}}"
"""
