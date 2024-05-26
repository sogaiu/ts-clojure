# Background

As Clojure lacks an official specification (like the sort of thing you
can find for Scheme or Common Lisp or other languages that have such
things), writing tree-sitter corpus tests (not to mention authoring
`grammar.js`) was not seen to be sufficient because determining
precisely what to express in such tests was and remains challenging.

As an additional layer of testing, running `tree-sitter parse` over
real-world code was attempted [1].  We found that this turned up many
issues as we developed the grammar and increased the sample size.

Thus, we recommend using a large selection of samples.  In Clojure's
case, there is a significant amount of syntactically correct code
available within "latest release" jars on Clojars and we retrieve
about as much of it as it seems to be sensible to do (currently
somewhat over 20,000 jars).

[1] Other approaches were also tried, but these are not covered here.
