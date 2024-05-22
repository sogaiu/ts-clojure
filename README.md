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

### Clone tree-sitter-clojure

Clone
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)
to a location such that the `tree-sitter` cli can find the resulting
directory.  Check out an appropriate branch, tag, or commit as
desired.

See [this document](doc/clone-tree-sitter-clojure.md) for more
details.

### Tweak settings

The file `conf.clj` contains a few somewhat configurable things to
tweak such as:

* `abi` - ABI number to use when generating `parser.c` from
  `grammar.js`
* `ts-bin-path` - path to or name of `tree-sitter` cli binary
* `repos` - which set of source samples to test against

See [this document](doc/tweak-settings.md) for more details.

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

## Build and Install Shared Library

To build and install a shared library based on the generated
`parser.c`:

```
bb corpus-test
```

See [this document](doc/build-and-install-shared-library.md) for more
details.

## Run Real-World Code Tests

To test the parser on real-world code:

```
bb parse-samples
```

Which set of samples is tested against is chosen by adjusting the
`repos` value in `conf.clj` appropriately.  Assuming the samples have
been obtained, the value can be one of:

* `clojars`
* `clojuredart`

See [this document](doc/run-real-world-code-tests.md) for more
details.

## Misc

`tree-sitter` cli subcommand backward compatibility does not appear to
be a high priority so at various future points, it may be necessary to
adjust some `tree-sitter` invocations in the various Babashka (`.clj`)
scripts.

As a specific examples of backward incompatibility, at the time of
this writing, there are plans to phase out the `build-wasm`
subcommand.

## Windows Support

Have not tested yet but might work via mingw-w64 / msys2 or similar.
No idea about WSL, not a fan and haven't tested.

