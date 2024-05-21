# ts-clojure

Testing and development bits for
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)

## Prerequisites

There are specific versions listed below for reference.  It's possible
that earlier / later versions may also work.

* Node.js (tested with 12.x, 14.x, 16.x, 18.x)
* Recent C compiler (tested with gcc 11.3.0, 12.2.0 clang 14.0.0)
* Babashka (tested with 1.2.174, 1.3.182, 1.3.189)

Note that an appropriate version of Node.js is available as part of
emsdk and can be used instead of separately installing one.  See the
[ts-questions](https://github.com/sogaiu/ts-questions) question about
which version of emscripten should be used for the playground for more
details on appropriate versions and emsdk setup instructions.

Node.js is currently required as part of `tree-sitter`'s `parser.c`
generation process.  IIUC, some work is underway to make it possible
to use some other JS option, but at the time of this writing, that has
not come to pass.  Even if it did at some point, if it's important
to use older versions of `tree-sitter`, those would require some
version of Node.js...

The C compiler is necessary to build the shared libary from
`parser.c`.

Babashka tasks are used to execute some common tasks.  The idea with
using Babashka is that most people who might take an interest in
tree-sitter-clojure and might consider joining in the maintenance fun
(hah!) would likely be at least somewhat Clojure-proficient...so why
choose Node, shell, or other things, right?

## Get Started

### Putting the tree-sitter-clojure directory in place

Clone
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)
to a location such that the `tree-sitter` cli can find the resulting
directory.  Verification of this fact can be performed by running
`tree-sitter dump-languages` and examining its output.

Sample output:

```
scope: source.clojure
parser: "./tree-sitter-clojure/"
highlights: None
file_types: ["bb", "clj", "cljc", "cljs"]
content_regex: None
injection_regex: None
```

The above output is somewhat atypical as far as the value of `parser`
is concerned because my `~/.config/tree-sitter/config.json` is:

```
{
  "parser-directories": [
    "."
  ]
}
```

It's a long story that I won't go into here (^^;  The important thing
is that the value associated with `parser` is pointed at an
appropriate directory that matches your setup.

### Tweak settings

The file `conf.clj` contains a few somewhat configurable things to
tweak such as:

* `abi` - ABI number to use when generating `parser.c` from
  `grammar.js`
* `tree-sitter` - path to or name of `tree-sitter` cli binary

## Generate `parser.c`

Use the `generate-parser` Babashka task.

## Install Shared Library for tree-sitter-clojure

To install a tree-sitter-clojure's shared library based on the
generated `parser.c`, the `corpus-test` Babashka task can be used.
(Yes, it's a bit of an odd method, but the `tree-sitter` cli doesn't
appear to provide a direct way of building AND installing an
appropriate library at the time of writing.)

N.B. if there is already a shared object in place from before the
generation of `parser.c`, it may need to be moved out of the way or
deleted first.

On a Linux system, the shared object might be found at
`~/.cache/tree-sitter/lib/clojure.so` for relatively recent versions
of `tree-sitter`.

## Run Non-trivial Test

To test the parser on real-world code, one can use the `parse-samples`
Babashka task.  However, first it's necessary to get some samples.

* Clone the
  [clojars-samples](https://github.com/sogaiu/clojars-samples) or
  [clojuredart-samples](https://github.com/sogaiu/clojuredart-samples)
  repositories as subdirectories.
* Edit the `repos` value in `conf.clj` appropriately, choosing
  either `clojars` or `clojuredart` for the value.
* Examine the Babashka tasks in the cloned subdirectory and execute
  the necessary tasks to obtain and prepare the samples.  (Sorry, this
  is vague at the moment.)  Note that this can take quite some time.

## Misc

If for some reason building `tree-sitter` becomes necessary, the
following info might be good to know:

* Rust Tooling (tested with rustc 1.67, 1.72.1 and cargo 1.67, 1.72.1)
* Recent C compiler (tested with gcc 11.3.0, 12.2.0 clang 14.0.0)

To get a version of `tree-sitter` that can build `.wasm` files, emsdk
is necessary.  Before running `cargo build`, it's important to run
`bash script/build-wasm --debug`, but before that, an appropriate
emsdk version needs to be activated.  More info about that is
available at the aforementioned
[ts-questions](https://github.com/sogaiu/ts-questions).

## Windows Support

Have not tested yet but might work via mingw-w64 / msys2 or similar.
No idea about WSL, not a fan and haven't tested.

