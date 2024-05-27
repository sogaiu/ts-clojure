# Run Real-World Code Tests

## clojars

The `clojars` tests typically take longer than a minute if the full
set of samples has been fetched.  It is also expected for there to be
a certain number of errors.  Currently, 115 out of somewhat over
150,000 files parse with errors.

Details about the expected errors can be seen in
[here](../data/classify-parse-errors.tsv).

## clojuredart

The `clojuredart` tests don't take very long because there are not
many samples to test against.  There should be no errors.
