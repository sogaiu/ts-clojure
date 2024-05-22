# ts-clojure

Testing and development bits for
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)

This repository houses bits to aid in performing tests on real-world
Clojure code as well as some tree-sitter-clojure development details.

## Prerequisites

Apart from the
[tree-sitter](https://github.com/tree-sitter/tree-sitter) cli and its
[dependencies](https://tree-sitter.github.io/tree-sitter/creating-parsers#dependencies),
[babashka](https://github.com/babashka/babashka) is required for its
task automation capabilities.

See [this document](doc/prerequisites.md) for more details.

## Get Started

### Putting a tree-sitter-clojure directory in place

Clone
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)
to a location such that the `tree-sitter` cli can find the resulting
directory.  Check out an appropriate branch, tag, or commit as
desired.  Verification that `tree-sitter` knows about the cloned
directory can be performed by running `tree-sitter dump-languages` and
examining its output.

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

It's a long story that I won't go into here (^^;

The important thing is that the value associated with `parser` is
appropriate.

For example, in the above output, it is `"./tree-sitter-clojure"`,
which would match a setup where:

* `~/.config/tree-sitter/config.json` had the content mentioned above
* `tree-sitter dump-languages` was invoked from within this project's
  root directory
* tree-sitter-clojure was cloned to be a subdirectory of this project 

I suspect most people don't set things up this way so YMMV.

### Tweak settings

The file `conf.clj` contains a few somewhat configurable things to
tweak such as:

* `abi` - ABI number to use when generating `parser.c` from
  `grammar.js`
* `ts-bin-path` - path to or name of `tree-sitter` cli binary
* `repos` - which set of source samples to test against

What number one can usefully specify for `abi` may depend on the
version of the `tree-sitter` cli in use.  At the time of this writing
(2024-05), it's likey that `14` is a good choice as it has been the
default for a few years (so across a fair number of `tree-sitter` cli
versions).

The value associated with `ts-bin-path` can be adjusted to point at
different versions of the `tree-sitter` cli.

Which set of source samples to be tested against can be specificed via
`repos`.  Note that this repository does not come with any samples.
Instructions on fetching samples is provided below.

### Retrieving source samples

This repository does not contain source code samples.  To fetch some
source code samples (so that tests can be performed across them):

* Clone the
  [clojars-samples](https://github.com/sogaiu/clojars-samples) and/or
  [clojuredart-samples](https://github.com/sogaiu/clojuredart-samples)
  repositories as subdirectories of the root of this project.

* Examine the Babashka tasks (via `bb tasks`) in the cloned
  subdirectories and execute the necessary tasks to obtain and prepare
  the samples.  (Sorry, this is vague at the moment.)  Note that this
  can take quite some time in the case of `clojars-samples` if all of
  the samples are fetched.

## Generate `parser.c`

To generate tree-sitter-clojure's `src/parser.c` file:

```
bb generate-parser
```

## Build and Install Shared Library for tree-sitter-clojure

To build and install a shared library based on the generated
`parser.c`:

```
bb corpus-test
```

Yes, it's a bit of an odd method, but the `tree-sitter` cli doesn't
appear to provide a direct way of building AND installing an
appropriate library at the time of writing.

N.B. if there is already a shared object in place from before the
generation of `parser.c`, it may need to be moved out of the way or
deleted first.

On a Linux system, the shared object might be found at
`~/.cache/tree-sitter/lib/clojure.so` for relatively recent versions
of `tree-sitter`.

## Run Real-World Code Test

To test the parser on real-world code:

```
bb parse-samples
```

Which set of samples is tested against is chosen by adjusting the
`repos` value in `conf.clj` appropriately.  Assuming the samples have
been obtained, the value can be one of:

* `clojars`
* `clojuredart`

### clojars

The `clojars` tests typically takes longer than a minute if
the full set of samples has been fetched.  It is also expected for
there to be a certain number of errors.  Currently, 131 out of
somewhat over 150,000 files parse with errors.

Details about the expected errors can be seen in
[here](data/classify-parse-errors-summary.txt) and
[here](data/classify-parse-errors.tsv).

### clojuredart

The `clojuredart` tests don't take very long because there are not
many samples to test against.  There should be no errors.

## Misc

`tree-sitter` cli subcommand backward compatibility does not appear to
be a high priority so at various future points, it may be necessary to
adjust some `tree-sitter` invocations (e.g. at the time of this
writing, there are plans to phase out the `build-wasm` subcommand).

## Windows Support

Have not tested yet but might work via mingw-w64 / msys2 or similar.
No idea about WSL, not a fan and haven't tested.

