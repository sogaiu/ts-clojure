# Tweak settings

## abi

What number one can usefully specify for `abi` may depend on the
version of the `tree-sitter` cli in use.  At the time of this writing
(2024-05), it's likey that `14` is a good choice as it has been the
default for a few years (so across a fair number of `tree-sitter` cli
versions).

## ts-bin-path

The value associated with `ts-bin-path` can be adjusted to point at
different versions of the `tree-sitter` cli.

## repos

Which set of source samples to be tested against can be specificed via
`repos`.  Note that this repository does not come with any samples.
Instructions on fetching samples is provided elsewhere.
