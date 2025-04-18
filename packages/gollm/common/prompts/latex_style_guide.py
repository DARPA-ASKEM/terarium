LATEX_STYLE_GUIDE = """
1) Derivatives must be written in Leibniz notation (for example, "\\frac{{d X}}{{d t}}")
    a) Derivatives that are written in other notations, like Newton ("\\dot{{X}}") or Lagrange ("X^\\prime" or "X'"), should be converted to Leibniz notation
    b) Partial derivatives of one-variable functions (for example, "\\partial_t X" or "\\frac{{\\partial X}}{{\\partial t}}") should be rewritten as ordinary derivatives ("\\frac{{d X}}{{d t}}")
2) First-order derivative must be on the left of the equal sign
3) All variables that have time "t" dependence should be written with an explicit "(t)" (for example, "X" should be written as "X(t)")
4) Rewrite superscripts and LaTeX superscripts "^" that denote indices to subscripts using LaTeX "_"
    a) assume "a", "i", "k" represents some variable or parameter
    b) replace "a^k" with "a_k"
    c) replace "a_i^(k)" with "a_i_k"
5) Replace any unicode subscripts with LaTeX subscripts using "_". Ensure that all characters used in the subscript are surrounded by a pair of curly brackets "{{...}}"
6) Use " * " to denote multiplication
    a) assume "a", "b", "S", "I" represents some algebraic expressions of parameters and variables
    b) replace "b S(t) I(t)" with "b * S(t) * I(t)"
    c) replace "b \\ast S(t) \ast I(t)" with "b * S(t)"
    d) replace "b \\star S(t)" with "b * S(t)"
    e) replace "b \times S(t)" with "b * S(t)"
    f) replace "(a b)S(t) with "a * b * S(t)"
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
    a) assume "x", "y" represent some algebraic expressions of parameters and variables
    a) replace "x \div y" with "\\frac{{x}}{{y}}"
    b) replace "x ÷ y" with "\\frac{{x}}{{y}}"
    c) replace "x / y" with "\\frac{{x}}{{y}}"
14) Rewrite expressions with negative exponents as explicit fractions
    a) replace "N^{{-1}}" with "\\frac{{1}}{{N}}" where "N" represents some algebraic expressions of parameters and variables
15) Do not use square brackets "[ ]", curly braces "{ }", and angle brackets "< >" when grouping expressions
16) Expand expressions with parentheses using the order of mathematical operations
    a) assume "a", "b", "c", "d" represent some algebraic expressions of parameters and variables
    b) replace "a (b + c + ... + d)" with "a * b + a * c + ... + a * d"
    c) replace "(a + ... + b) (c + ... + d)" with "a * c + b * c + a * d + b * d"
17) Do not expand expressions of the form "(1 - a)" where "a" is some constant parameter
    a) these expressions represent branching processes where one compartment's outflow splits into two branches, one at a rate of "a" and the other at "(1 - a)".
    b) for example, preserve "(1 - a) b I(t)" as "(1 - b) * b * I(t)" where "a" and "b" are some constant parameters and "I(t)" is some time-dependent variable.
18) Write named mathematical functions and operators using their corresponding LaTeX code
    a) assume "x" represents some algebraic expression of parameters and variables
    b) replace "sin(x)" with "\\sin{{(x)}}"
    c) replace "cos", "tan", "csc", "sec", "cot, sinh, cosh, tanh, csch, sech, coth" with "\\cos", "\\tan", "\\csc", "\\cot", "\\csc", "\\sinh", "\\cosh", "\\tanh", "\\csch", "\\coth", "\\csc" similarly
    d) replace "ln(x)" or "log(x)" with "\\log {{(x)}}"
    e) replace "log_2 (x)" with "\\log_2 {{(x)}}"
    f) replace "exp(-x)", "e^(-x)", or "\\mathrm{e}^(-x)" with "\\exp{{(-x)}}"
    g) replace "max(x)", "min(x)" with "\\max{{(x)}}", "\\min{{(x(t))}}"
    h) for other named functions like "CustomFunc(x)", write "\operatorname{{CustomFunc}}{{(x)}}"
19) Replace asterisk "*" by "s" when it does not represent multiplication
    a) assume "x is some variable or parameter
    b) replace "x^*" or "x^(*)" with "x_s"
"""
