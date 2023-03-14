# ts-clojure

Testing and development bits for
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)

## Prerequisites

There are specific versions listed below for reference.  It's possible
that earlier / later versions may also work.

### Tree-sitter CLI Building and Capabilities

* Rust Tooling (tested with rustc 1.67 and cargo 1.67)
* Node.js (tested with 12.x, 14.x, 16.x, 18.x)
* Recent C compiler (tested with gcc 11.3.0, clang 14.0.0)

### Repository and Code Sample Fetching

* git (tested with 2.34.1)

### Code Sample Fetching

* curl (tested with 7.81.0)

### Miscellaneous Tasks

* gzip (tested with 1.10)
* sort (tested with 8.32 from coreutils)

### Task Automation

* Babashka (tested with 1.2.174)

## Getting Started

Once prerequisites have been installed and this repository cloned, try
`bb tasks` to get a list of tasks.  Looking through `bb.edn` might
also be helpful.

The file `conf/conj.clj` contains various somewhat configurable things
such as:

* `ts-ref` - commit ref for which version of the `tree-sitter` cli to
  use (can be SHA, branch name, tag name)
* `abi` - ABI number to use when generating `parser.c` from
  `grammar.js`
* `c-compiler` - name of C compiler to invoke (e.g. `cc`, `gcc`,
  `clang`)

Inside the maps `ts-clj` and `ts-clj-def`, the values for the keys
`:repo-url` and `:ref` might be of interest too.

The value of `grammar` can be changed to select which grammar is the
"current" one.  It can be set to `ts-clj` or `ts-clj-def`.

## Windows Support

Have not tested yet but might work via mingw-w64 / msys2 or similar.
No idea about WSL, not a fan and haven't tested.

