# tree-sitter-clojure Not Parent Directory

This document briefly touches on using `ts-clojure` without it being a
subdirectory of `tree-sitter-clojure`.

## Setup

The steps that differ from the default arrangement include:

* Ensure this repository is available
* Ensure tree-sitter-clojure is available

### Ensure ts-clojure is Available

Clone [ts-clojure](https://github.com/sogaiu/ts-clojure) somewhere
local.

### Ensure tree-sitter-clojure is Available

Clone
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)
such that the `tree-sitter` cli can find the resulting directory.  The
setup should be one of:

1. tree-sitter-clojure is a subdirectory of ts-clojure
2. no particular parent-child relationship

Check out an appropriate branch, tag, or commit as desired.

Note that the `grammar-dir` setting in `conf/conf.clj` should be a
filesystem path that resolves to or is the tree-sitter-clojure
directory.  So either change the setting or make an appropriate
symlink.

See [here](clone-tree-sitter-clojure.md) for more
details.

