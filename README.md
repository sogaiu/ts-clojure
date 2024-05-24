# ts-clojure

Testing and development bits for
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)

This repository houses bits to aid in performing tests on real-world
Clojure code along with some associated instructions.

## One-time (Mostly) Setup

There is some one-time (mostly) setup necessary before tests can be
executed.  This includes:

* Verifying / installing prerequisites
* Verifying tree-sitter setup
* Cloning this repository
* Cloning tree-sitter-clojure
* Fetching source code samples
* Tweaking ts-clojure's settings

Consider working with this repository in a new user account.  This is
not required, but you might want to read near the end of the document
for potential consequences of not doing so.

### Prerequisites

The prerequisites are what you typically need to work with
tree-sitter, with the exception of Babashka which is used for task
automation:

* git
* [tree-sitter](https://github.com/tree-sitter/tree-sitter) cli
* [tree-sitter dependencies](https://tree-sitter.github.io/tree-sitter/creating-parsers#dependencies)
* [babashka](https://github.com/babashka/babashka)

See [here](doc/prerequisites.md) for more details.

### Verify tree-sitter setup

Verify what version of `tree-sitter` you have installed and confirm
that you know where it looks to find parser repositories.

See [here](doc/verify-tree-sitter-setup.md) for more details.

### Clone ts-clojure

Clone [ts-clojure](https://github.com/sogaiu/ts-clojure) (this
repository) somewhere local.

### Clone tree-sitter-clojure

Clone
[tree-sitter-clojure](https://github.com/sogaiu/tree-sitter-clojure)
to a location such that the `tree-sitter` cli can find the resulting
directory.  Check out an appropriate branch, tag, or commit as
desired.

Note that the `grammar-dir` setting in `conf/conf.clj` should be a
filesystem path that resolves to or is the tree-sitter-clojure
directory.  So either change the setting or make an appropriate
symlink.

See [here](doc/clone-tree-sitter-clojure.md) for more
details.

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

### Tweak settings

ts-clojure's scripts can be configured a bit via the file
`conf/conf.clj`.

Some included settings are:

* `abi` - ABI number to use when generating `parser.c` from
  `grammar.js`
* `grammar-dir` - path to cloned tree-sitter-clojure directory
* `repos` - which set of source samples to test against
* `ts-bin-path` - path to or name of `tree-sitter` cli binary

See [here](doc/tweak-settings.md) for more details.

## Things You Can Do

### Generate `parser.c`

To generate tree-sitter-clojure's `src/parser.c` file:

```
bb generate-parser
```

This will invoke `tree-sitter`'s `generate` subcommand:

* using the ABI number specified in `conf/conf.clj` via `abi` and
* using the `--no-bindings` argument to avoid generating binding code

### Build and Install Shared Library

To build and install a shared library based on the generated
`parser.c`:

```
bb corpus-test
```

This will run tree-sitter-clojure's corpus tests via `tree-sitter
test`, which has a side-effect that achieves the desired aim.

See [here](doc/build-and-install-shared-library.md) for more
details.

### Run Real-World Code Tests

To test the parser on real-world code:

```
bb parse-samples
```

This will invoke `tree-sitter`'s `parse` command across a set of
source code samples.

Which set of samples is tested against is chosen by adjusting the
`repos` value in `conf/conf.clj` appropriately.  Assuming the samples
have been obtained, the value can be one of:

* `clojars`
* `clojuredart`

See [here](doc/run-real-world-code-tests.md) for more
details.

## Misc Notes

### About Being Future-Proof

At this time, `tree-sitter` cli subcommand backward compatibility does
not appear to be a high priority so at various future points, it may
be necessary to adjust some `tree-sitter` invocations in the various
Babashka (`.clj`) scripts.

As a specific example of a potential backward incompatibility, at the
time of this writing, there are plans to phase out the `build-wasm`
subcommand.

### Windows Support

Have not tested yet but might work via mingw-w64 / msys2 or similar.
No idea about WSL, not a fan and haven't tested.

### Isolation

I had trouble at various points with `tree-sitter` using things that I
was unaware of.  I believe this was the result of a combination of
testing with different versions of `tree-sitter` (various
configuration and operation file paths changed between versions),
having multiple versions of grammars under my home directory, along
with [tree-sitter's automatic
compilation](https://github.com/tree-sitter/tree-sitter/issues/2017)
(and my not understanding how it works in detail).  If you want to
avoid some of that kind of thing, you might want to consider some kind
of isolation / simplification mechanism such as using a fresh user
account when working with this repository.
