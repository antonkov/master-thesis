(TeX-add-style-hook
 "thesis"
 (lambda ()
   (TeX-add-to-alist 'LaTeX-provided-package-options
                     '(("fontenc" "T2A") ("inputenc" "utf8x") ("babel" "english" "russian") ("graphicx" "pdftex")))
   (add-to-list 'LaTeX-verbatim-environments-local "lstlisting")
   (add-to-list 'LaTeX-verbatim-macros-with-braces-local "lstinline")
   (add-to-list 'LaTeX-verbatim-macros-with-delims-local "lstinline")
   (TeX-run-style-hooks
    "latex2e"
    "header"
    "chapters/intro"
    "chapters/chapter1"
    "chapters/chapter2"
    "chapters/chapter3"
    "chapters/chapter4"
    "chapters/outro"
    "appendix"
    "report"
    "rep10"
    "cmap"
    "fontenc"
    "inputenc"
    "babel"
    "expdlist"
    "graphicx"
    "amsmath"
    "amssymb"
    "amsthm"
    "amsfonts"
    "amsxtra"
    "array"
    "wrapfig"
    "sty/dbl12"
    "srcltx"
    "epsfig"
    "verbatim"
    "sty/rac"
    "listings"
    "placeins"
    "caption"
    "subfigure"
    "easytable")
   (TeX-add-symbols
    '("todo" 1)
    "p"
    "t"
    "b"
    "tb"
    "cln"
    "pcn"
    "putImgx"
    "putImg"
    "drawfigure"
    "drawfigurex")
   (LaTeX-add-labels
    "#1"
    "#3"
    "appendix")
   (LaTeX-add-environments
    "theorem"
    "prop"
    "corollary"
    "lemma"
    "question"
    "conjecture"
    "assumption"
    "definition"
    "notation"
    "condition"
    "example"
    "algorithm"
    "remark")
   (LaTeX-add-bibliographies)))

